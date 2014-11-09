package org.aksw.jena_sparql_api.concept_cache;

import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.concept_cache.domain.PatternSummary;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;

public class QueryIndex {
    private Op rootOp;
    private Map<Op, Op> parentMap;

    private IBiSetMultimap<Set<Set<Expr>>, PatternSummary> quadCnfToSummary;
    //private Map<PatternSummary, Op>


    public QueryIndex(Op rootOp, Map<Op, Op> parentMap) {
        this.rootOp = rootOp;
        this.parentMap = parentMap;
    }

    public static QueryIndex create(Op rootOp) {
        Map<Op, Op> parentMap = OpUtils.parentMap(rootOp);


        QueryIndex result = new QueryIndex(rootOp, parentMap);
        return result;
    }

}
