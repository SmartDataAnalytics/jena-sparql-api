package org.aksw.jena_sparql_api.changeset;

import org.aksw.jena_sparql_api.utils.ResultSetPart;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

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