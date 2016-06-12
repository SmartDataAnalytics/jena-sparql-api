package org.aksw.jena_sparql_api.data_client;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

public interface DataClient {
    //protected QueryExecutionFactory qef;

    QueryExecutionFactory findDistributions(Concept datasetFilter);
}
