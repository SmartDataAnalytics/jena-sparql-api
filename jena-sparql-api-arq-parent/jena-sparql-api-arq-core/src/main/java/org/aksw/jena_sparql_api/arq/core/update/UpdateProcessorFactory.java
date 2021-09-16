package org.aksw.jena_sparql_api.arq.core.update;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

@FunctionalInterface
public interface UpdateProcessorFactory
//    extends Function<UpdateRequest, UpdateProcessor>
{
    UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest);
}
