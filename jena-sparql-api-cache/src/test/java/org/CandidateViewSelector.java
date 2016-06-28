package org;
import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;

public interface CandidateViewSelector<T>
    extends Function<Expr, Collection<T>>
{
}
