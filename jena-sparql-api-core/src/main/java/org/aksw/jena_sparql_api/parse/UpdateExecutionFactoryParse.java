package org.aksw.jena_sparql_api.parse;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDelegate;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import com.google.common.base.Function;

public class UpdateExecutionFactoryParse
    extends UpdateExecutionFactoryDelegate
{
    protected Function<String, UpdateRequest> parser;

    public UpdateExecutionFactoryParse(UpdateExecutionFactory decoratee, Function<String, UpdateRequest> parser) {
        super(decoratee);
        this.parser = parser;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
        UpdateRequest updateRequest = parser.apply(updateRequestStr);
        UpdateProcessor result = createUpdateProcessor(updateRequest);
        return result;
    }

}
