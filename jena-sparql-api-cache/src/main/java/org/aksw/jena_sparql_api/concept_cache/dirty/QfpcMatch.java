package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.apache.jena.sparql.core.Var;

// TODO: Rename table to 'key'
public class QfpcMatch {
    private QuadFilterPatternCanonical replacementPattern;
    private QuadFilterPatternCanonical diffPattern;
    //private K table;
    private Map<Var, Var> varMap;

    public QfpcMatch(QuadFilterPatternCanonical replacementPattern, QuadFilterPatternCanonical diffPattern, Map<Var, Var> varMap) {
        super();
        this.replacementPattern = replacementPattern;
        this.diffPattern = diffPattern;
        //this.table = table;
        this.varMap = varMap;
    }

    public QuadFilterPatternCanonical getReplacementPattern() {
        return this.replacementPattern;
    }

    public QuadFilterPatternCanonical getDiffPattern() {
        return diffPattern;
    }

//    public K getTable() {
//        return table;
//    }

    public Map<Var, Var> getVarMap() {
        return varMap;
    }

	@Override
	public String toString() {
		return "QfpcMatch [replacementPattern=" + replacementPattern + ", diffPattern=" + diffPattern + ", table="
				 + ", varMap=" + varMap + "]";
	}
}
