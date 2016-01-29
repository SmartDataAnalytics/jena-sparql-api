package org.aksw.jena_sparql_api.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;


public class NfUtils {
    public static Set<Var> getVarsMentioned(Iterable<? extends Iterable<? extends Expr>> clauses) {
        Set<Var> result = new HashSet<Var>();

        for(Iterable<? extends Expr> clause : clauses) {
            Set<Var> tmp = ClauseUtils.getVarsMentioned(clause);
            tmp.addAll(tmp);
        }

        return result;
    }
}
