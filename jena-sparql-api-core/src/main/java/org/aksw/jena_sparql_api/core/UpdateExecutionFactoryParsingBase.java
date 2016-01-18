package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public abstract class UpdateExecutionFactoryParsingBase
    implements UpdateExecutionFactory
{
    @Override
    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestStr, Syntax.syntaxARQ);
        UpdateProcessor result = this.createUpdateProcessor(updateRequest);
        return result;
    }
}
