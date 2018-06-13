package org.aksw.jena_sparql_api.changeset;

import java.util.function.Function;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Iterators;

public class FunctionTableFirstRow
    implements Function<Table, Binding>
{
    @Override
    public Binding apply(Table input) {
        Binding result = Iterators.getNext(input.rows(), null);
        return result;
    }

    public static final FunctionTableFirstRow fn = new FunctionTableFirstRow();
}