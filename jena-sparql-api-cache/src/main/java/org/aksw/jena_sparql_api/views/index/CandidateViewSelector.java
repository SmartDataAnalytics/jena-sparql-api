package org.aksw.jena_sparql_api.views.index;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;

public interface CandidateViewSelector<T>
    extends Function<Expr, Collection<T>>
{
}
