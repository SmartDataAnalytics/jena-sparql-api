package org.aksw.jena_sparql_api.normal_form;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.apache.jena.sparql.expr.Expr;

public class Dnf
    extends ExprNormalForm
{
    public Dnf(Collection<Clause> clauses) {
        super(clauses);
    }

    public static Dnf create(Expr expr) {
        Set<Set<Expr>> ss = DnfUtils.toSetDnf(expr);

        Set<Clause> clauses = new HashSet<Clause>();
        for(Set<Expr> s : ss) {
            clauses.add(new Clause(s));
        }

        return new Dnf(clauses);
    }
}
