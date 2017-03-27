package org.aksw.jena_sparql_api.mapper;

import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class AggList<T>
    implements Agg<List<T>>
{
    private Agg<T> subAgg;

    public AggList(Agg<T> subAgg) {
        this.subAgg = subAgg;
    }

    //private Expr orderExpr;
    // TODO How to do ordering? Support a Comparator<T> for that - or use a transform?

    @Override
    public Acc<List<T>> createAccumulator() {
        Acc<List<T>> result = new AccList<T>(subAgg);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        Set<Var> result = subAgg.getDeclaredVars();
        return result;
    }

    public static <T> AggList<T> create(Agg<T> subAgg) {
        AggList<T> result = new AggList<T>(subAgg);
        return result;
    }
}
