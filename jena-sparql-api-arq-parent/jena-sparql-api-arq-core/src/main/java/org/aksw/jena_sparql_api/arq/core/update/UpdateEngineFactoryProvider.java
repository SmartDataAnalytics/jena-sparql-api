package org.aksw.jena_sparql_api.arq.core.update;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.util.Context;

@FunctionalInterface
public interface UpdateEngineFactoryProvider {
    UpdateEngineFactory find(/* UpdateRequest updateRequest, */ DatasetGraph datasetGraph, Context context);
}