package org.aksw.jena_sparql_api.data_client;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.rdf.model.Resource;

public class DataClientImpl
    implements DataClient
{
    public Stream<Resource> findDatasetsByName(String name) {
        return null;
    }


    @Override
    public QueryExecutionFactory findDistributions(Concept datasetFilter) {
        // TODO Auto-generated method stub
        return null;
    }

}
