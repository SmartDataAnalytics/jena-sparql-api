package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;

import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.apache.jena.sparql.algebra.Table;

// TODO Remove and use QfpcAggMatch instead
public class CacheResult {
    protected QuadFilterPatternCanonical replacementPattern;
    protected Collection<Table> tables;

    public CacheResult(QuadFilterPatternCanonical replacementPattern, Collection<Table> tables) {
        super();
        this.replacementPattern = replacementPattern;
        this.tables = tables;
    }

    public QuadFilterPatternCanonical getReplacementPattern() {
        return this.replacementPattern;
    }

    public Collection<Table> getTables() {
        return tables;
    }

    @Override
    public String toString() {
        return "CacheResult [replacementPattern=" + replacementPattern
                + ", tables=" + tables + "]";
    }
}
