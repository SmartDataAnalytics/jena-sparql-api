package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

import org.apache.jena.sparql.algebra.Table;

public class CacheResult2<V> {
    protected QuadFilterPatternCanonical replacementPattern;
    protected Collection<V> tables;

    public CacheResult2(QuadFilterPatternCanonical replacementPattern, Collection<V> tables) {
        super();
        this.replacementPattern = replacementPattern;
        this.tables = tables;
    }

    public QuadFilterPatternCanonical getReplacementPattern() {
        return this.replacementPattern;
    }

    public Collection<V> getTables() {
        return tables;
    }

    @Override
    public String toString() {
        return "CacheResult [replacementPattern=" + replacementPattern
                + ", tables=" + tables + "]";
    }
}
