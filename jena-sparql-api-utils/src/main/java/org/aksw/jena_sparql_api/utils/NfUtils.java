package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.aksw.commons.collections.SetUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class NfUtils {

    public static Set<Var> getVarsMentioned(Iterable<? extends Iterable<? extends Expr>> clauses) {
        Set<Var> result = new HashSet<Var>();

        for(Iterable<? extends Expr> clause : clauses) {
            Set<Var> tmp = ClauseUtils.getVarsMentioned(clause);
            result.addAll(tmp);
        }

        return result;
    }

    public static Set<Set<Expr>> canonicalize(Iterable<? extends Iterable<? extends Expr>> clauses) {
        Set<Set<Expr>> result = StreamSupport.stream(clauses.spliterator(), false)
            .map(clause -> ClauseUtils.signaturize(clause))
            .collect(Collectors.toSet());

        return result;
    }

    /**
     * Create equivalence classes for clauses of a normal form
     *
     *
     */
    public static Multimap<Set<Expr>, Set<Expr>> createStructuralEquivalenceClasses(Iterable<? extends Iterable<? extends Expr>> clauses) {
        Multimap<Set<Expr>, Set<Expr>> result = HashMultimap.create();

        for(Iterable<? extends Expr> clause : clauses) {
            Set<Expr> clazz = ClauseUtils.signaturize(clause);
            result.put(clazz, SetUtils.asSet((Set<Expr>)clause));
        }

        return result;
    }
}
