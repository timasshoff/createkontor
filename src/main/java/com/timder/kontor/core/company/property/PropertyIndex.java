package com.timder.kontor.core.company.property;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import java.util.*;

/**
 * Fast lookup for land ownership.
 * Only a snapshot that is rebuild when necessary, {@link Company} stays the single source of truth, so check against it.
 */
public class PropertyIndex {

    private final Map<ChunkKey, CompanyId> candidates = new HashMap<>();

    public void rebuild(Collection<Company> companies) {
        Objects.requireNonNull(companies, "companies must not be null.");
        candidates.clear();
        for (Company company : companies) {
            for (PropertyHolding holding : company.properties()) {
                candidates.put(holding.chunk(), company.id());
            }
        }
    }

    public void register(ChunkKey chunk, CompanyId company) {
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(company, "company must not be null.");
        candidates.put(chunk, company);
    }

    public void clear() {
        candidates.clear();
    }

    public int size() {
        return candidates.size();
    }

    public boolean isIndexed(ChunkKey chunk) {
        Objects.requireNonNull(chunk, "chunk must not be null.");
        return candidates.containsKey(chunk);
    }

    public Optional<Company> holderOf(ChunkKey chunk, CompanyRegistry registry) {
        Objects.requireNonNull(chunk, "chunk must not be null.");
        Objects.requireNonNull(registry, "registry must not be null.");
        CompanyId id = candidates.get(chunk);
        if (id == null) {
            return Optional.empty();
        }
        Optional<Company> holder =
                registry.get(id).filter(company -> company.property(chunk).isPresent());
        if (holder.isEmpty()) {
            candidates.remove(chunk);
        }
        return holder;
    }

    public boolean isProtected(ChunkKey chunk, CompanyRegistry registry) {
        return holderOf(chunk, registry).isPresent();
    }

    public boolean mayBeModified(ChunkKey chunk, UUID player, CompanyRegistry registry) {
        Objects.requireNonNull(player, "player must not be null.");
        return holderOf(chunk, registry)
                .map(company -> company.isMember(player))
                .orElse(true);
    }

    /**
     * Whether a machine / block (e.g. piston) can reach and modify land
     * @param origin The chunk that the machine stands in
     * @param target The chunk that the machine is reaching towards
     * @param registry The company registry
     * @return True, if the machine may modify the target chunk
     */
    public boolean mayBeReached(ChunkKey origin, ChunkKey target, CompanyRegistry registry) {
        Optional<Company> targetHolder = holderOf(target, registry);
        if (targetHolder.isEmpty()) {
            return true;
        }
        return holderOf(origin, registry)
                .map(originHolder -> originHolder.id().equals(targetHolder.get().id()))
                .orElse(false);
    }
}
