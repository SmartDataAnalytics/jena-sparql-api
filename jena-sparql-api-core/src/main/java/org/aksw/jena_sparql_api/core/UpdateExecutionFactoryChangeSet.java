package org.aksw.jena_sparql_api.core;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;


/**
 * An UpdateExecution that fires events on changes
 *
 * @author raven
 *
 */
public class UpdateExecutionFactoryChangeSet
    extends UpdateExecutionFactoryParsingBase
{
    private UpdateExecutionFactory uef;

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        // TODO Auto-generated method stub
        return null;
    }

}
