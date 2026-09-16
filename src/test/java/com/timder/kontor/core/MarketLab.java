package com.timder.kontor.core;

import com.timder.kontor.core.macro.MacroParams;
import com.timder.kontor.core.macro.MacroRules;
import com.timder.kontor.core.macro.MacroState;
import com.timder.kontor.core.macro.Phase;
import com.timder.kontor.core.macro.ProgressParams;
import com.timder.kontor.core.market.*;
import com.timder.kontor.core.port.Rng;
import com.timder.kontor.core.port.SeededRng;
import com.timder.kontor.core.raw.PriceProcessParams;
import com.timder.kontor.core.raw.RawMaterialParams;
import com.timder.kontor.core.raw.RawMaterialRules;
import com.timder.kontor.core.raw.RawMaterialState;
import com.timder.kontor.core.value.CostRules;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.ToDoubleFunction;

/**
 * One tool to run a market with a business cycle, a wandering raw material and a supplier
 * behind it, and look at every number it produces, down to the trading tick.
 *
 * <p>Give it market parameters, macro parameters and a number of days. It runs the
 * simulation twelve trading ticks per day and writes an HTML page with a chart for every
 * value, plus a summary table. Several runs can be put on the same page to compare them.
 *
 * <p><b>This class invents nothing.</b> Every number comes from a small, fixed set of core
 * calls: {@link MacroRules#demand}, {@link MacroRules#technicalProgress}, {@link
 * RawMaterialRules#advanceDay}, {@link CostRules#stepCost}, {@link
 * MarketRules#advanceTradingTick} and {@link MarketRules#advanceDay}. Each already exists
 * in core and is covered by its own tests. If a chart looks wrong, the bug is in core, not
 * in this tool, because this tool does not add any arithmetic of its own between those
 * calls.
 *
 * <p>One consequence of that promise: plant size is <em>not</em> scaled with the trend
 * here, because {@code MarketParams.plantSize()} is a plain fixed number in core today.
 * The concept document (chapter 11.3) says it should grow with the trend, but core does not
 * do that yet. Run a scenario for a long time and you will see the rival count drift
 * upward beyond its usual range, purely because demand grows through the trend (and, once a
 * raw material is configured, because technical progress also lowers the cost floor) while
 * capacity does not. That drift is a real, honest gap between the concept and core, not a
 * flaw of this tool, and a good candidate to fix once the {@code Economy} facade composes
 * macro and market together for real.
 *
 * <p>Vocabulary used throughout this class and its charts:
 * <ul>
 *   <li><b>Supplier</b> - the one business this tool follows in detail. Stands in for the
 *       player later on. Its listed price is fixed for a whole day, exactly like {@code
 *       Marktpreis ± x %} pricing in the concept only updates at the daily changeover
 *       (chapter 14.1), even though the market it is compared against keeps moving every
 *       trading tick.</li>
 *   <li><b>Rivals</b> ({@link MarketState#getCompanies()}) - everyone else in the market,
 *       modelled as a single number. Not a list, not individual firms, and the supplier is
 *       never part of it.</li>
 *   <li><b>Price level</b> - the rivals' structural price, moves once a day.</li>
 *   <li><b>Deviation</b> - a fast overlay on top of the price level that moves every
 *       trading tick and fades on its own, see {@link MarketRules#advanceTradingTick}.</li>
 *   <li><b>Displayed price</b> - price level with the deviation applied,
 *       {@link MarketState#getDisplayedPrice()}. What every share calculation actually
 *       compares against.</li>
 *   <li><b>Cost floor</b> - the rivals' own cost per unit, {@link
 *       MarketParams#referenceCost()}. Fixed unless a raw material is configured.</li>
 * </ul>
 *
 * <p>Lives in the test source set, because it is a balancing tool and not game content.
 *
 * <pre>
 * // watch a single big delivery move the market within minutes
 * MarketLab.of("a sudden dump", ironSheet)
 *          .supplier(1.00, 3.0)
 *          .oneTimeDump(5, 300.0)
 *          .days(10)
 *          .chart();
 * </pre>
 */
public final class MarketLab {

    private static final Path OUTPUT = Path.of("build", "market");

    private static final String[] COLOURS = {
            "#2a78d6", "#eb6834", "#1baf7a", "#eda100", "#e87ba4", "#6250d6", "#e34948"
    };

    // ------------------------------------------------------------------ one trading tick of results

    /**
     * Everything one simulated trading tick produced. Immutable, twelve per day.
     *
     * <p>Values that only change once a day (cycle index, demand, raw material price,
     * technical progress, reference cost, rivals, utilisation, overflow, the supplier's own
     * price) repeat across the twelve ticks of that day, so a chart at tick resolution shows
     * them as a staircase: flat within a day, a step at the boundary. Price level, deviation
     * and displayed price, and the supplier's share, genuinely move every tick.
     *
     * @param day Day number, starting at one
     * @param tick Tick within the day, zero to eleven
     * @param cycleIndex The business cycle index, one is normal
     * @param phase The phase the newspaper would report
     * @param groupFactor How the cycle hits this product group
     * @param trend Long term growth factor
     * @param demand Demand of the whole day
     * @param rawMaterialPrice Price of the configured raw material, or NaN without one
     * @param technicalProgressFactor Technical progress, 1.0 means none yet
     * @param referenceCost The rivals' cost per unit today
     * @param priceLevel The rivals' structural price today
     * @param deviation The trading deviation right now
     * @param displayedPrice priceLevel with deviation applied, what share calculations compare against
     * @param equilibriumPrice Price the market is heading for today
     * @param companies Number of rival businesses today
     * @param equilibriumCompanies Number the market is heading for today
     * @param utilisation Utilisation of the rivals, zero to one
     * @param overflow Demand nobody could serve today
     * @param supplierPrice What the supplier asks today, fixed for the whole day, or NaN without one
     * @param supplierShare Share of this tick's demand the supplier received
     * @param supplierSold How much the supplier actually delivered this tick
     * @param equilibriumShare Share the supplier settles on in the long run, or NaN
     */
    public record Tick(
            int day,
            int tick,
            double cycleIndex,
            Phase phase,
            double groupFactor,
            double trend,
            double demand,
            double rawMaterialPrice,
            double technicalProgressFactor,
            double referenceCost,
            double priceLevel,
            double deviation,
            double displayedPrice,
            double equilibriumPrice,
            double companies,
            double equilibriumCompanies,
            double utilisation,
            double overflow,
            double supplierPrice,
            double supplierShare,
            double supplierSold,
            double equilibriumShare
    ) {
    }

    // ------------------------------------------------------------------ configuration

    private final String name;
    private final MarketParams params;

    private MacroParams macroParams = MacroParams.standard();
    private ProgressParams progressParams = ProgressParams.standard();
    private double baseDemand = 1800.0;
    private long seed = 42L;
    private int days = 60;

    private double supplierPriceFactor = Double.NaN;
    private double supplierReputation = MarketParams.NEUTRAL_REPUTATION;
    private double fixedDeliveryShare = 0.0;

    private RawMaterialParams rawMaterialParams;
    private PriceProcessParams priceProcessParams = PriceProcessParams.standard();
    private double processingCost;
    private double dailyPurchase = 0.0;

    private double startPrice = Double.NaN;
    private double startCompanies = 3.0;

    private int dumpDay = -1;
    private double dumpAmount = 0.0;

    private List<Tick> result;

    private MarketLab(String name, MarketParams params) {
        this.name = name;
        this.params = params;
    }

    public static MarketLab of(String name, MarketParams params) {
        return new MarketLab(name, params);
    }

    /** Settings of the business cycle. Defaults to the standard values. */
    public MarketLab macro(MacroParams value) {
        this.macroParams = value;
        return this;
    }

    /** Settings of technical progress. Defaults to the standard values. */
    public MarketLab progress(ProgressParams value) {
        this.progressParams = value;
        return this;
    }

    /** Base demand of the product before cycle and trend. Defaults to 1800. */
    public MarketLab baseDemand(double value) {
        this.baseDemand = value;
        return this;
    }

    /** Seed of the random source. Same seed, same history. Defaults to 42. */
    public MarketLab seed(long value) {
        this.seed = value;
        return this;
    }

    /**
     * How many days to simulate. Each day is twelve trading ticks, so the chart data grows
     * fast; the default of 60 already produces 720 points per line. Keep this modest unless
     * you specifically want to look at long-run drift.
     */
    public MarketLab days(int value) {
        this.days = value;
        return this;
    }

    /**
     * A supplier who asks a fixed factor of the market's displayed price and receives
     * whatever share that earns him each tick.
     *
     * <p>The price itself is only recomputed once a day, at the start of the day, from that
     * moment's displayed price, and then held fixed for all twelve ticks, exactly like the
     * "Marktpreis ± x %" pricing mode in the concept (chapter 14.1) only updates at the
     * daily changeover. The share still moves every tick, because the market it is compared
     * against keeps moving underneath it.
     *
     * @param priceFactor 0.9 means ten percent below the market price
     * @param reputationStars One to five stars
     */
    public MarketLab supplier(double priceFactor, double reputationStars) {
        this.supplierPriceFactor = priceFactor;
        this.supplierReputation = reputationStars;
        this.fixedDeliveryShare = 0.0;
        return this;
    }

    /** A supplier who dumps a fixed fraction of each tick's demand regardless of the price. */
    public MarketLab deliveryShare(double share) {
        this.fixedDeliveryShare = share;
        this.supplierPriceFactor = Double.NaN;
        return this;
    }

    /**
     * Adds one extra, unexpected delivery on top of whatever the supplier would normally
     * sell, at the first tick of the given day. This is exactly the scenario the concept
     * works through by hand in chapter 11.9: a factory batch finishing all at once and
     * showing up as a single large delivery, moving the deviation within minutes and then
     * fading over the following ticks. Works with or without a configured supplier.
     *
     * @param atDay The day the dump happens, one-based
     * @param extraUnits How many units arrive on top of the normal delivery
     */
    public MarketLab oneTimeDump(int atDay, double extraUnits) {
        this.dumpDay = atDay;
        this.dumpAmount = extraUnits;
        return this;
    }

    /**
     * Makes the cost floor wander. One unit of output consumes one unit of this raw
     * material, run through one processing step. Both the market's reference cost and the
     * chart's cost floor follow the raw material price and technical progress from now on,
     * recomputed fresh every day through {@link CostRules#stepCost}.
     *
     * @param materialParams Settings of the raw material
     * @param processingCost What the processing step itself costs, before technical progress
     */
    public MarketLab rawMaterial(RawMaterialParams materialParams, double processingCost) {
        this.rawMaterialParams = materialParams;
        this.processingCost = processingCost;
        return this;
    }

    /** Global settings of the raw material price process. Defaults to the standard values. */
    public MarketLab priceProcess(PriceProcessParams value) {
        this.priceProcessParams = value;
        return this;
    }

    /** How much of the raw material is bought every day, on top of its own wandering. */
    public MarketLab dailyPurchase(double value) {
        this.dailyPurchase = value;
        return this;
    }

    /** Starting price of the rivals. Defaults to the equilibrium price. */
    public MarketLab startPrice(double value) {
        this.startPrice = value;
        return this;
    }

    /** Starting number of rival businesses. Defaults to three. */
    public MarketLab startCompanies(double value) {
        this.startCompanies = value;
        return this;
    }

    public String name() {
        return name;
    }

    // ------------------------------------------------------------------ the simulation

    /** Runs the simulation once and caches the result. One entry per trading tick. */
    public List<Tick> run() {
        if (result != null) {
            return result;
        }

        Rng rng = new SeededRng(seed);
        MacroState macro = MacroState.fresh(macroParams, rng);
        RawMaterialState raw = rawMaterialParams == null
                ? null : RawMaterialState.fresh(rawMaterialParams);
        MarketState market = new MarketState(
                Double.isNaN(startPrice) ? params.equilibriumPrice() : startPrice,
                startCompanies);

        double attractiveness = supplierAttractiveness();
        List<Tick> history = new ArrayList<>(days * MarketRules.TRADING_TICKS_PER_DAY);

        for (int day = 1; day <= days; day++) {
            // Everything that only changes once a day: the business cycle, the raw
            // material, technical progress, and from them the demand and the cost floor.
            MacroRules.advanceDay(macro, macroParams, rng);
            double demand = MacroRules.demand(baseDemand, macro, macroParams, params.cycleSensitivity());
            double techProgress = MacroRules.technicalProgress(macro, progressParams);

            double rawPrice = Double.NaN;
            MarketParams todayParams = params;
            if (raw != null) {
                if (dailyPurchase > 0) {
                    raw.recordPurchase(dailyPurchase);
                }
                RawMaterialRules.advanceDay(raw, rawMaterialParams, priceProcessParams, rng);
                rawPrice = raw.getPrice();
                double referenceCost = CostRules.stepCost(rawPrice, 1.0, processingCost, techProgress, 1.0);
                todayParams = new MarketParams(referenceCost, params.plantSize(),
                        params.targetUtilisation(), params.group());
            }

            double equilibriumCompanies = todayParams.equilibriumCompanies(demand);
            double equilibriumShare = Double.isNaN(attractiveness)
                    ? Double.NaN : attractiveness / equilibriumCompanies;

            // The supplier's own price for the day: a factor of whatever the market
            // displays right now, then held fixed until tomorrow.
            double supplierPrice = Double.isNaN(supplierPriceFactor)
                    ? Double.NaN : supplierPriceFactor * market.getDisplayedPrice();

            double demandPerTick = demand / MarketRules.TRADING_TICKS_PER_DAY;

            for (int tick = 0; tick < MarketRules.TRADING_TICKS_PER_DAY; tick++) {
                double share = 0.0;
                if (!Double.isNaN(supplierPrice)) {
                    share = MarketRules.share(supplierPrice, supplierReputation, market, todayParams);
                } else if (fixedDeliveryShare > 0) {
                    share = fixedDeliveryShare;
                }
                double expected = share * demandPerTick;
                double extra = (day == dumpDay && tick == 0) ? dumpAmount : 0.0;
                double actual = expected + extra;

                market.recordDelivery(actual);
                MarketRules.advanceTradingTick(market, actual, expected, demandPerTick);

                history.add(new Tick(
                        day, tick,
                        macro.getIndex(), MacroRules.phase(macro),
                        MacroRules.groupFactor(macro, params.cycleSensitivity()),
                        MacroRules.trend(macro, macroParams),
                        demand, rawPrice, techProgress, todayParams.referenceCost(),
                        market.getPriceLevel(), market.getDeviation(), market.getDisplayedPrice(),
                        todayParams.equilibriumPrice(),
                        market.getCompanies(), equilibriumCompanies,
                        // utilisation and overflow are daily figures, only known after
                        // advanceDay; filled in with the previous day's values here and
                        // corrected below once the day is complete.
                        Double.NaN, Double.NaN,
                        supplierPrice, share, actual, equilibriumShare));
            }

            DayResult r = MarketRules.advanceDay(market, todayParams, demand);

            // Backfill this day's utilisation and overflow into the twelve ticks just
            // written, now that advanceDay has computed them.
            int base = history.size() - MarketRules.TRADING_TICKS_PER_DAY;
            for (int i = base; i < history.size(); i++) {
                Tick t = history.get(i);
                history.set(i, new Tick(t.day(), t.tick(), t.cycleIndex(), t.phase(),
                        t.groupFactor(), t.trend(), t.demand(), t.rawMaterialPrice(),
                        t.technicalProgressFactor(), t.referenceCost(), t.priceLevel(),
                        t.deviation(), t.displayedPrice(), t.equilibriumPrice(),
                        t.companies(), t.equilibriumCompanies(), r.utilisation(), r.overflow(),
                        t.supplierPrice(), t.supplierShare(), t.supplierSold(), t.equilibriumShare()));
            }
        }

        result = history;
        return history;
    }

    /** Constant attractiveness of the configured supplier, or NaN if there is none. */
    private double supplierAttractiveness() {
        if (Double.isNaN(supplierPriceFactor)) {
            return Double.NaN;
        }
        return Math.pow(supplierReputation / MarketParams.NEUTRAL_REPUTATION,
                params.reputationWeight())
                * Math.pow(supplierPriceFactor, -params.priceSensitivity());
    }

    // ------------------------------------------------------------------ output

    /** Prints a short summary to the console. */
    public MarketLab summary() {
        List<Tick> history = run();
        Tick last = history.get(history.size() - 1);

        System.out.printf(Locale.ROOT, "%n%s  (%d days x %d ticks, seed %d)%n",
                name, days, MarketRules.TRADING_TICKS_PER_DAY, seed);
        System.out.printf(Locale.ROOT, "  %-22s %8s %8s %8s %8s%n", "", "min", "max", "average", "last");
        line("cycle index", history, Tick::cycleIndex, last.cycleIndex());
        line("demand", history, Tick::demand, last.demand());
        if (rawMaterialParams != null) {
            line("raw material price", history, Tick::rawMaterialPrice, last.rawMaterialPrice());
            line("reference cost", history, Tick::referenceCost, last.referenceCost());
        }
        line("price level", history, Tick::priceLevel, last.priceLevel());
        line("deviation", history, Tick::deviation, last.deviation());
        line("displayed price", history, Tick::displayedPrice, last.displayedPrice());
        line("rivals", history, Tick::companies, last.companies());
        line("rivals utilisation", history, Tick::utilisation, last.utilisation());
        if (!Double.isNaN(last.supplierPrice()) || last.supplierShare() > 0) {
            line("supplier share", history, Tick::supplierShare, last.supplierShare());
        }
        return this;
    }

    private void line(String label, List<Tick> history, ToDoubleFunction<Tick> value, double last) {
        double min = history.stream().mapToDouble(value).min().orElse(0);
        double max = history.stream().mapToDouble(value).max().orElse(0);
        double avg = history.stream().mapToDouble(value).average().orElse(0);
        System.out.printf(Locale.ROOT, "  %-22s %8.4f %8.4f %8.4f %8.4f%n", label, min, max, avg, last);
    }

    /** Writes every value of every tick to {@code build/market/<name>.csv}. */
    public Path csv() {
        List<Tick> history = run();
        StringBuilder out = new StringBuilder(
                "tag;takt;konjunktur;phase;gruppenfaktor;trend;nachfrage;rohstoffpreis;fortschritt;"
                        + "kostenboden;preisniveau;abweichung;angezeigter_preis;gleichgewichtspreis;"
                        + "betriebe;gleichgewichtsbetriebe;auslastung;ueberlauf;"
                        + "anbieterpreis;anteil;geliefert;gleichgewichtsanteil\n");

        for (Tick t : history) {
            out.append(String.format(Locale.GERMANY,
                    "%d;%d;%.4f;%s;%.4f;%.4f;%.1f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.4f;%.1f;%.4f;%.4f;%.1f;%.4f%n",
                    t.day(), t.tick(), t.cycleIndex(), t.phase(), t.groupFactor(), t.trend(), t.demand(),
                    t.rawMaterialPrice(), t.technicalProgressFactor(), t.referenceCost(),
                    t.priceLevel(), t.deviation(), t.displayedPrice(), t.equilibriumPrice(),
                    t.companies(), t.equilibriumCompanies(), t.utilisation(), t.overflow(),
                    t.supplierPrice(), t.supplierShare(), t.supplierSold(), t.equilibriumShare()));
        }
        return write(fileName(name, ".csv"), out.toString());
    }

    /** Writes the chart page for this single run and opens it. */
    public Path chart() {
        return chart(name, this);
    }

    /** Writes one chart page holding several runs and opens it. */
    public static Path chart(String title, MarketLab... labs) {
        if (labs.length == 0) {
            throw new IllegalArgumentException("pass at least one run");
        }
        Path file = write(fileName(title, ".html"), buildHtml(title, Arrays.asList(labs)));
        open(file);
        return file;
    }

    // ------------------------------------------------------------------ html

    private static String buildHtml(String title, List<MarketLab> labs) {
        StringBuilder demand = new StringBuilder();
        StringBuilder raw = new StringBuilder();
        StringBuilder price = new StringBuilder();
        StringBuilder deviation = new StringBuilder();
        StringBuilder companies = new StringBuilder();
        StringBuilder utilisation = new StringBuilder();
        StringBuilder share = new StringBuilder();
        StringBuilder overflow = new StringBuilder();
        StringBuilder legend = new StringBuilder();
        StringBuilder table = new StringBuilder();

        int longest = 0;
        boolean anyRawMaterial = false;

        for (int i = 0; i < labs.size(); i++) {
            MarketLab lab = labs.get(i);
            List<Tick> history = lab.run();
            longest = Math.max(longest, history.size());
            String colour = COLOURS[i % COLOURS.length];

            demand.append(set(lab.name, colour, series(history, Tick::demand)));
            price.append(set(lab.name + " displayed", colour, series(history, Tick::displayedPrice)));
            price.append(set(lab.name + " price level", colour, series(history, Tick::priceLevel)));
            price.append(set(lab.name + " cost floor", colour, series(history, Tick::referenceCost)));
            deviation.append(set(lab.name, colour, series(history, Tick::deviation)));
            companies.append(set(lab.name, colour, series(history, Tick::companies)));
            utilisation.append(set(lab.name, colour, series(history, Tick::utilisation)));
            share.append(set(lab.name, colour, series(history, Tick::supplierShare)));
            overflow.append(set(lab.name, colour, series(history, Tick::overflow)));

            if (lab.rawMaterialParams != null) {
                anyRawMaterial = true;
                raw.append(set(lab.name, colour, series(history, Tick::rawMaterialPrice)));
            }

            if (!Double.isNaN(history.get(0).equilibriumShare())) {
                share.append(flat(lab.name + " long run",
                        series(history, Tick::equilibriumShare)));
            }

            legend.append("<span class=\"k\"><span class=\"s\" style=\"background:%s\"></span>%s</span>%n"
                    .formatted(colour, escape(lab.name)));
            table.append(row(lab, history, colour));
        }

        MarketLab first = labs.get(0);
        List<Tick> firstRun = first.run();
        companies.append(flat("equilibrium", series(firstRun, Tick::equilibriumCompanies)));
        utilisation.append(flat("target", constant(first.params.targetUtilisation(), longest)));

        StringJoiner labels = new StringJoiner(",", "[", "]");
        for (Tick t : firstRun) {
            labels.add("\"" + t.day() + "." + (t.tick() + 1) + "\"");
        }

        String rawSectionStyle = anyRawMaterial ? "" : "display:none";

        return TEMPLATE
                .replace("{{title}}", escape(title))
                .replace("{{legend}}", legend.toString())
                .replace("{{table}}", table.toString())
                .replace("{{labels}}", labels.toString())
                .replace("{{demand}}", demand.toString())
                .replace("{{raw}}", raw.toString())
                .replace("{{rawSectionStyle}}", rawSectionStyle)
                .replace("{{price}}", price.toString())
                .replace("{{deviation}}", deviation.toString())
                .replace("{{companies}}", companies.toString())
                .replace("{{utilisation}}", utilisation.toString())
                .replace("{{share}}", share.toString())
                .replace("{{overflow}}", overflow.toString());
    }

    private static String row(MarketLab lab, List<Tick> history, String colour) {
        Tick last = history.get(history.size() - 1);
        return """
                <tr>
                  <td><span class="s" style="background:%s"></span> %s</td>
                  <td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>
                </tr>
                """.formatted(colour, escape(lab.name),
                range(history, Tick::cycleIndex, 2),
                range(history, Tick::demand, 0),
                range(history, Tick::displayedPrice, 2),
                range(history, Tick::companies, 2),
                range(history, Tick::utilisation, 2),
                num(last.supplierShare() * 100, 1) + " %");
    }

    private static String range(List<Tick> history, ToDoubleFunction<Tick> value, int digits) {
        double min = history.stream().mapToDouble(value).min().orElse(0);
        double max = history.stream().mapToDouble(value).max().orElse(0);
        return num(min, digits) + " to " + num(max, digits);
    }

    private static String num(double value, int digits) {
        return String.format(Locale.ROOT, "%." + digits + "f", value);
    }

    private static String series(List<Tick> history, ToDoubleFunction<Tick> value) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Tick t : history) {
            double v = value.applyAsDouble(t);
            joiner.add(Double.isNaN(v) ? "null" : String.format(Locale.ROOT, "%.4f", v));
        }
        return joiner.toString();
    }

    private static String constant(double value, int length) {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (int i = 0; i < length; i++) {
            joiner.add(String.format(Locale.ROOT, "%.4f", value));
        }
        return joiner.toString();
    }

    private static String set(String label, String colour, String data) {
        return "{label:\"%s\",data:%s,borderColor:\"%s\",borderWidth:2,pointRadius:0,tension:0.1},%n"
                .formatted(escape(label), data, colour);
    }

    private static String flat(String label, String data) {
        return ("{label:\"%s\",data:%s,borderColor:\"#898781\",borderWidth:1,"
                + "pointRadius:0,borderDash:[6,4]},%n").formatted(escape(label), data);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("<", "&lt;");
    }

    // ------------------------------------------------------------------ files

    private static String fileName(String title, String suffix) {
        return title.replaceAll("[^a-zA-Z0-9-_]", "_") + suffix;
    }

    private static Path write(String fileName, String content) {
        try {
            Files.createDirectories(OUTPUT);
            Path file = OUTPUT.resolve(fileName);
            Files.writeString(file, content);
            System.out.println("written: " + file.toAbsolutePath());
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void open(Path file) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(file.toUri());
            }
        } catch (IOException | UnsupportedOperationException e) {
            System.out.println("could not open a browser: " + e.getMessage());
        }
    }

    private static final String TEMPLATE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="utf-8">
            <title>{{title}}</title>
            <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
            <style>
              body{font-family:system-ui,sans-serif;max-width:980px;margin:2rem auto;padding:0 1rem;color:#1a1a19}
              h1{font-size:20px;font-weight:500}
              h2{font-size:15px;font-weight:500;margin:2rem 0 .25rem}
              p.hint{font-size:13px;color:#73726c;margin:0 0 .5rem}
              .box{position:relative;height:260px}
              .k{display:inline-flex;align-items:center;gap:6px;margin-right:16px;font-size:13px;color:#52514e}
              .s{width:11px;height:11px;border-radius:2px;display:inline-block}
              .glossary{background:#f4f3ee;border-radius:8px;padding:.75rem 1rem;margin:1rem 0;font-size:13px;color:#3d3d3a}
              .glossary p{margin:.35rem 0}
              table{border-collapse:collapse;width:100%;font-size:13px;margin:1rem 0 2rem}
              th,td{text-align:left;padding:6px 10px;border-bottom:1px solid #e1e0d9}
              th{color:#73726c;font-weight:500}
            </style>
            </head>
            <body>
            <h1>{{title}}</h1>
            <div>{{legend}}</div>

            <div class="glossary">
              <p><b>Supplier</b> -- the one business this page follows in detail. Its price is fixed for a
                 whole day, like every listed price in the game, even though the market underneath moves
                 every trading tick.</p>
              <p><b>Rivals</b> -- everyone else in the market, combined into a single number. The supplier
                 is never counted in it.</p>
              <p><b>Price level</b> -- the rivals structural price, moves once a day. <b>Deviation</b> -- a
                 fast overlay on top of it that moves every trading tick and fades on its own, driven by
                 how much was delivered versus how much the market expected. <b>Displayed price</b> -- the
                 two combined, what every share calculation actually compares against.</p>
              <p><b>Cost floor</b> -- the rivals own cost per unit. Fixed unless a raw material is
                 configured, in which case it wanders with that material price and technical progress.</p>
              <p><b>Demand</b> -- decided only by the business cycle and the long-term trend, never by
                 anyone price. Runs sharing a seed always show the same demand curve.</p>
            </div>

            <table>
              <tr><th>supplier</th><th>cycle index</th><th>demand</th><th>displayed price</th>
                  <th>rivals</th><th>rivals utilisation</th><th>supplier final share</th></tr>
              {{table}}
            </table>

            <h2>Demand</h2>
            <p class="hint">Only changes once a day, hence the staircase. Depends only on the business
               cycle, never on price or rivals.</p>
            <div class="box"><canvas id="c2"></canvas></div>

            <div style="{{rawSectionStyle}}">
              <h2>Raw material price</h2>
              <p class="hint">Also a once-a-day figure. Wanders randomly but keeps drifting back to its
                 base value. Feeds the cost floor below through one processing step.</p>
              <div class="box"><canvas id="c8"></canvas></div>
            </div>

            <h2>Market price</h2>
            <p class="hint">Three lines per run: the displayed price customers actually see (moves every
               tick), the slower price level underneath it (moves once a day), and the dashed cost floor.
               The gap between displayed price and price level is exactly the deviation, shown on its own
               below.</p>
            <div class="box"><canvas id="c3"></canvas></div>

            <h2>Trading deviation</h2>
            <p class="hint">Zero means the market got exactly what it expected this tick. A delivery larger
               than expected pushes it negative (temporarily cheaper looking), a shortfall pushes it
               positive. Decays on its own, roughly halving every eleven ticks.</p>
            <div class="box"><canvas id="c9"></canvas></div>

            <h2>Rivals</h2>
            <p class="hint">The competition as a single number, excluding the supplier. A once-a-day
               figure. Dashed: the number the market is heading for.</p>
            <div class="box"><canvas id="c4"></canvas></div>

            <h2>Utilisation of the rivals</h2>
            <p class="hint">Also once a day. Drives both the price level and the rival count above.</p>
            <div class="box"><canvas id="c5"></canvas></div>

            <h2>Market share of the supplier</h2>
            <p class="hint">Moves every tick, because the displayed price it is compared against does.
               Dashed: the share the supplier settles on in the long run.</p>
            <div class="box"><canvas id="c6"></canvas></div>

            <h2>Unserved demand (overflow)</h2>
            <p class="hint">Once a day. Anything above zero means customers went home empty handed.</p>
            <div class="box"><canvas id="c7"></canvas></div>

            <script>
            const labels = {{labels}};
            const base = {
              responsive:true, maintainAspectRatio:false, animation:false,
              interaction:{mode:"index",intersect:false},
              plugins:{legend:{display:false}},
              scales:{x:{grid:{display:false},ticks:{maxTicksLimit:12,maxRotation:0}},
                      y:{grid:{color:"#e1e0d9"}}}
            };
            const pct = {...base, scales:{...base.scales,
              y:{min:0,max:1.1,grid:{color:"#e1e0d9"},ticks:{callback:v=>Math.round(v*100)+" %"}}}};
            const draw = (id, sets, opts) => new Chart(document.getElementById(id),
              {type:"line", data:{labels, datasets:sets}, options:opts});
            draw("c2",[{{demand}}],base);
            draw("c8",[{{raw}}],base);
            draw("c3",[{{price}}],base);
            draw("c9",[{{deviation}}],{...base,scales:{...base.scales,
              y:{min:-0.55,max:0.55,grid:{color:"#e1e0d9"},ticks:{callback:v=>Math.round(v*100)+" %"}}}});
            draw("c4",[{{companies}}],{...base,scales:{...base.scales,y:{min:0,max:8.5,grid:{color:"#e1e0d9"}}}});
            draw("c5",[{{utilisation}}],pct);
            draw("c6",[{{share}}],pct);
            draw("c7",[{{overflow}}],base);
            </script>
            </body>
            </html>
            """;

    // ------------------------------------------------------------------ examples

    public static void main(String[] args) {
        MarketParams ironSheet = new MarketParams(8.06, 720.0, 0.80, GroupDef.metal());

        // The headline example of this session: a single large delivery, and nothing else,
        // watched tick by tick. No supplier needed, the dump speaks for itself.
        MarketLab.of("a single dump", ironSheet)
                .oneTimeDump(3, 300.0)
                .days(10)
                .summary()
                .chart();

        // The self-braking effect: a supplier who dumps once, then keeps asking the price
        // he had before the dump, watches his own share dip for the rest of that day.
        MarketLab.of("dumping brakes itself", ironSheet)
                .supplier(1.00, 3.0)
                .oneTimeDump(3, 300.0)
                .days(6)
                .summary()
                .chart();

        // A steady supplier over a longer run: deviation should sit at zero throughout,
        // proving that ordinary, predictable delivery never disturbs the market.
        MarketLab.of("steady delivery disturbs nothing", ironSheet)
                .supplier(0.95, 3.0)
                .days(60)
                .chart();

        MarketLab.of("nothing", ironSheet)
                .days(900)
                .summary()
                .chart();
    }
}