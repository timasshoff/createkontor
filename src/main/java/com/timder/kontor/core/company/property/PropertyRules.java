package com.timder.kontor.core.company.property;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyParams;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import java.util.*;

public final class PropertyRules {

    public enum Mode {
        BUY,
        RENT
    }

    public record AcquireResult(Status status, Money price, Money dailyRent) {
        public AcquireResult {
            Objects.requireNonNull(status, "status must not be null.");
            Objects.requireNonNull(price, "price must not be null.");
            Objects.requireNonNull(dailyRent, "dailyRent must not be null.");
        }

        public boolean succeeded() {
            return status == Status.BOUGHT
                    || status == Status.RENTED
                    || status == Status.CONVERTED
                    || status == Status.REACTIVATED;
        }

        public enum Status {
            /** The chunk is bought and paid. */
            BOUGHT,
            /** The chunk is rented. */
            RENTED,
            /** A rented chunk is bought now and paid in full */
            CONVERTED,
            /** A cancelled rent runs on. */
            REACTIVATED,
            /** The company already holds the chunk this way. */
            ALREADY_HELD,
            /** Another company holds the chunk. */
            OCCUPIED_BY_OTHER,
            /** The legal form allows no more chunks. */
            LIMIT_REACHED,
            /** The chunk has no price */
            UNPRICEABLE,
            /** The company is illiquid. */
            NOT_OPERATIONAL,
            /** The company cannot pay the price. */
            CANNOT_AFFORD
        }
    }

    public record SellResult(Status status, Money proceeds) {
        public SellResult {
            Objects.requireNonNull(status, "status must not be null.");
            Objects.requireNonNull(proceeds, "proceeds must not be null.");
        }

        public enum Status {
            SOLD,
            /** The company does not hold the chunk. */
            NOT_HELD,
            /** The chunk is rented. */
            NOT_OWNED,
            /** The chunk has no price right now, so there is no resale value. */
            UNPRICEABLE
        }
    }

    public enum CancelResult {
        /** The rent is cancelled.*/
        CANCELLED,
        /** The rent had been cancelled before. */
        ALREADY_CANCELLED,
        /** The company does not hold the chunk.*/
        NOT_HELD,
        /** The chunk is owned, not rented. */
        NOT_RENTED
    }

    /**
     * Previews what acquiring would do right now
     * @param company The company
     * @param allCompanies Every company
     * @param chunk The chunk
     * @param biome The biome
     * @param mode Buy or Rent
     * @param params The company parameters
     * @return The acquire result (preview)
     */
    public static AcquireResult preview(
            Company company,
            Collection<Company> allCompanies,
            ChunkKey chunk,
            BiomeId biome,
            Mode mode,
            CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(allCompanies, "allCompanies must not be null.");
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(biome, "biome must not be null.");
        Objects.requireNonNull(mode, "mode must not be null.");
        Objects.requireNonNull(params, "params must not be null.");

        for (Company other : allCompanies) {
            if (!other.id().equals(company.id()) && other.property(chunk).isPresent()) {
                return result(AcquireResult.Status.OCCUPIED_BY_OTHER);
            }
        }

        PropertyHolding existing = company.property(chunk).orElse(null);
        BiomeId priceBiome = biome;
        boolean converting = false;
        if (existing != null) {
            if (existing.isOwned()) {
                return result(AcquireResult.Status.ALREADY_HELD);
            }
            if (mode == Mode.RENT) {
                return result(
                        existing.cancelled() ? AcquireResult.Status.REACTIVATED : AcquireResult.Status.ALREADY_HELD);
            }
            converting = true;
            priceBiome = existing.biome();
        }

        if (!converting && count(company) >= company.legalForm(params).maxProperties()) {
            return result(AcquireResult.Status.LIMIT_REACHED);
        }

        Money price = priceOf(chunk, priceBiome, params.property()).orElse(null);
        if (price == null) {
            return result(AcquireResult.Status.UNPRICEABLE);
        }
        Money rent = params.property().dailyRent(price);

        if (!company.isOperational()) {
            return new AcquireResult(AcquireResult.Status.NOT_OPERATIONAL, price, rent);
        }
        if (mode == Mode.BUY && !company.canSpend(price, params)) {
            return new AcquireResult(AcquireResult.Status.CANNOT_AFFORD, price, rent);
        }

        AcquireResult.Status status;
        if (mode == Mode.RENT) {
            status = AcquireResult.Status.RENTED;
        } else {
            status = converting ? AcquireResult.Status.CONVERTED : AcquireResult.Status.BOUGHT;
        }
        return new AcquireResult(status, price, rent);
    }

    /**
     * Buys or rents a chunk
     * @param company The company
     * @param allCompanies All companies
     * @param chunk The chunk
     * @param biome The biome
     * @param mode Buy or Rent
     * @param day The day
     * @param params The company parameters
     * @return The acquire result
     */
    public static AcquireResult acquire(
            Company company,
            Collection<Company> allCompanies,
            ChunkKey chunk,
            BiomeId biome,
            Mode mode,
            long day,
            CompanyParams params) {
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        AcquireResult result = preview(company, allCompanies, chunk, biome, mode, params);
        switch (result.status()) {
            case BOUGHT -> {
                pay(company, result, chunk, day, params);
                company.addProperty(PropertyHolding.bought(chunk, biome, day));
            }
            case CONVERTED -> {
                pay(company, result, chunk, day, params);
                company.replaceProperty(company.property(chunk).orElseThrow().boughtOn(day));
            }
            case RENTED -> company.addProperty(PropertyHolding.rented(chunk, biome, day));
            case REACTIVATED ->
                company.replaceProperty(company.property(chunk).orElseThrow().reactivate());
            default -> {}
        }
        return result;
    }

    private static void pay(Company company, AcquireResult result, ChunkKey chunk, long day, CompanyParams params) {
        if (!company.trySpend(day, BookingKind.PROPERTY_PURCHASE, result.price(), chunk.toString(), params)) {
            throw new IllegalStateException(
                    "The company could pay " + result.price() + " a moment ago but cannot now.");
        }
    }

    /**
     * Sells an owned chunk back
     * @param company The company
     * @param chunk The chunk
     * @param day The day
     * @param params The company parameters
     * @return What happened
     */
    public static SellResult sell(Company company, ChunkKey chunk, long day, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        PropertyHolding holding = company.property(chunk).orElse(null);
        if (holding == null) {
            return new SellResult(SellResult.Status.NOT_HELD, Money.ZERO);
        }
        if (!holding.isOwned()) {
            return new SellResult(SellResult.Status.NOT_OWNED, Money.ZERO);
        }
        Money price = priceOf(chunk, holding.biome(), params.property()).orElse(null);
        if (price == null) {
            return new SellResult(SellResult.Status.UNPRICEABLE, Money.ZERO);
        }

        Money proceeds = params.property().resaleValue(price);
        company.removeProperty(chunk);
        if (proceeds.isPositive()) {
            company.account().book(day, BookingKind.PROPERTY_SALE, proceeds, chunk.toString());
        }
        return new SellResult(SellResult.Status.SOLD, proceeds);
    }

    /**
     * Cancels a rented chunk. Chunk is still held and billed until the next day.
     * @param company The company
     * @param chunk The chunk
     * @return What happened
     */
    public static CancelResult cancelRent(Company company, ChunkKey chunk) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(chunk, "chunk must not be null.");

        PropertyHolding holding = company.property(chunk).orElse(null);
        if (holding == null) {
            return CancelResult.NOT_HELD;
        }
        if (!holding.isRented()) {
            return CancelResult.NOT_RENTED;
        }
        if (holding.cancelled()) {
            return CancelResult.ALREADY_CANCELLED;
        }
        company.replaceProperty(holding.cancel());
        return CancelResult.CANCELLED;
    }

    /**
     * Drops all chunks that have cancelled rents
     * @param company The company
     * @return The dropped chunks
     */
    public static List<ChunkKey> dropCancelled(Company company) {
        Objects.requireNonNull(company, "company must not be null.");

        List<ChunkKey> dropped = new ArrayList<>();
        for (PropertyHolding holding : company.properties()) {
            if (holding.cancelled()) {
                company.removeProperty(holding.chunk());
                dropped.add(holding.chunk());
            }
        }
        return dropped;
    }

    public static int count(Company company) {
        Objects.requireNonNull(company, "company must not be null.");
        return company.properties().size();
    }

    public static Optional<Company> ownerOf(Collection<Company> companies, ChunkKey chunk) {
        Objects.requireNonNull(companies, "companies must not be null.");
        Objects.requireNonNull(chunk, "chunk must not be null.");
        for (Company company : companies) {
            if (company.property(chunk).isPresent()) {
                return Optional.of(company);
            }
        }
        return Optional.empty();
    }

    public static Money dailyRent(PropertyHolding holding, CompanyParams params) {
        Objects.requireNonNull(holding, "holding must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        if (!holding.isRented()) {
            return Money.ZERO;
        }
        return priceOf(holding.chunk(), holding.biome(), params.property())
                .map(params.property()::dailyRent)
                .orElse(Money.ZERO);
    }

    public static Money totalDailyRent(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Money total = Money.ZERO;
        for (PropertyHolding holding : company.properties()) {
            total = total.plus(dailyRent(holding, params));
        }
        return total;
    }

    public static Money landValue(Company company, CompanyParams params) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(params, "params must not be null.");
        Money total = Money.ZERO;
        for (PropertyHolding holding : company.properties()) {
            if (!holding.isOwned()) {
                continue;
            }
            Optional<Money> price = priceOf(holding.chunk(), holding.biome(), params.property());
            if (price.isPresent()) {
                total = total.plus(params.property().resaleValue(price.get()));
            }
        }
        return total;
    }

    public static Optional<Money> priceOf(ChunkKey chunk, BiomeId biome, PropertyParams params) {
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(biome, "biome must not be null.");
        BiomeFrequencies measured = params.frequenciesOf(chunk.dimension());
        if (!PropertyPricingRules.isPriceable(params.pricing(), chunk.dimension(), measured)) {
            return Optional.empty();
        }
        return Optional.of(PropertyPricingRules.priceOf(params.pricing(), chunk.dimension(), measured, biome));
    }

    private static AcquireResult result(AcquireResult.Status status) {
        return new AcquireResult(status, Money.ZERO, Money.ZERO);
    }
}
