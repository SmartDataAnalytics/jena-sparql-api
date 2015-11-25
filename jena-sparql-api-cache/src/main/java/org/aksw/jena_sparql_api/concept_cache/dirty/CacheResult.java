package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Collection;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

import com.hp.hpl.jena.sparql.algebra.Table;

public class CacheResult {
    private QuadFilterPatternCanonical replacementPattern;
    private Collection<Table> tables;

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
}
