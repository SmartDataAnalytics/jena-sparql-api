package org.aksw.jena_sparql_api.concept_cache;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

import com.hp.hpl.jena.sparql.algebra.Table;

class CacheHit {
    private QuadFilterPatternCanonical pattern;
    private Table table;

    public CacheHit(QuadFilterPatternCanonical pattern, Table table) {
        super();
        this.pattern = pattern;
        this.table = table;
    }

    public QuadFilterPatternCanonical getPattern() {
        return pattern;
    }

    public Table getTable() {
        return table;
    }
}