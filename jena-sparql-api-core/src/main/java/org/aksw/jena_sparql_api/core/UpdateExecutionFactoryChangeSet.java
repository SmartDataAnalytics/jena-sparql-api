package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;


/**
 * An UpdateExecution that fires events on changes
 *
 * @author raven
 *
 */
public class UpdateExecutionFactoryChangeSet
    implements UpdateExecutionFactory
{
    private UpdateExecutionFactory uef;

    @Override
    public UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest) {
        // TODO Auto-generated method stub
        return null;
    }


}
