package org.aksw.jena_sparql_api.changeset;

import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Iterables;

public class FunctionResultSetPartFirstRow
    implements Function<ResultSetPart, Binding>
{
    @Override
    public Binding apply(ResultSetPart input) {
        Binding result = Iterables.getFirst(input.getBindings(), null);
        return result;
    }

    public static final FunctionResultSetPartFirstRow fn = new FunctionResultSetPartFirstRow();
}