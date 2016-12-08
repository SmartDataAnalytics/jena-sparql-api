package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import org.aksw.jena_sparql_api.utils.ResultSetPart;

import org.apache.jena.sparql.engine.binding.Binding;

public class AccResultSetPart
    implements Acc<ResultSetPart>
{
    private ResultSetPart value;

    public AccResultSetPart(List<String> varNames) {
        this.value = new ResultSetPart(varNames);
    }

    @Override
    public void accumulate(Binding binding) {
        value.getBindings().add(binding);
    }

    @Override
    public ResultSetPart getValue() {
        return value;
    }
}
