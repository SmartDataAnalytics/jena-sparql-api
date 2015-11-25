package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Var;

public class QfpcMatch {
    private QuadFilterPatternCanonical replacementPattern;
    private QuadFilterPatternCanonical diffPattern;
    private Table table;
    private Map<Var, Var> varMap;

    public QfpcMatch(QuadFilterPatternCanonical replacementPattern, QuadFilterPatternCanonical diffPattern, Table table, Map<Var, Var> varMap) {
        super();
        this.replacementPattern = replacementPattern;
        this.diffPattern = diffPattern;
        this.table = table;
        this.varMap = varMap;
    }

    public QuadFilterPatternCanonical getReplacementPattern() {
        return this.replacementPattern;
    }

    public QuadFilterPatternCanonical getDiffPattern() {
        return diffPattern;
    }

    public Table getTable() {
        return table;
    }

    public Map<Var, Var> getVarMap() {
        return varMap;
    }
}
