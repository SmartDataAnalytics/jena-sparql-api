package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.arq.core.update.UpdateProcessorFactory;
import org.apache.jena.update.UpdateProcessor;

public interface UpdateExecutionFactory
    extends UpdateProcessorFactory, AutoCloseable
{
    UpdateProcessor createUpdateProcessor(String updateRequestStr);

    <T> T unwrap(Class<T> clazz);

//    @Override
//    void close();
}
