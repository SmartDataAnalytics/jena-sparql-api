package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.utils.PatternSummary;
import org.apache.jena.sparql.core.Var;

public class CacheSummary<V> {
    private PatternSummary patternSummary;

    // A map from sets of variables to the cache data (or some other kind of data)
    private Map<Set<Var>, V> varsToData;

    public CacheSummary(PatternSummary patternSummary,
            Map<Set<Var>, V> varsToData) {
        super();
        this.patternSummary = patternSummary;
        this.varsToData = varsToData;
    }

    public PatternSummary getPatternSummary() {
        return patternSummary;
    }

    public Map<Set<Var>, V> getVarsToData() {
        return varsToData;
    }


}
