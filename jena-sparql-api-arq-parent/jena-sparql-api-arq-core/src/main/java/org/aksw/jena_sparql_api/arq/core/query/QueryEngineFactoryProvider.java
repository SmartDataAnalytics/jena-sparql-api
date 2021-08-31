package org.aksw.jena_sparql_api.arq.core.query;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.util.Context;

/** Functional Interface whose signature matches {@link QueryEngineRegistry#find(Query, DatasetGraph, Context)}*/
@FunctionalInterface
public interface QueryEngineFactoryProvider {
    QueryEngineFactory find(Query query, DatasetGraph dataset, Context context);
}