package org.aksw.jena_sparql_api.update;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;

public class SparqlServiceFactoryTransform
    implements SparqlServiceFactory
{
    protected SparqlServiceFactory delegate;
    protected Function<SparqlService, SparqlService> transform;

    public SparqlServiceFactoryTransform(SparqlServiceFactory delegate, Function<SparqlService, SparqlService> transform) {
        this.delegate = delegate;
        this.transform = transform;
    }

    @Override
    public SparqlService createSparqlService(String serviceUri,
            DatasetDescription datasetDescription, HttpClient httpClient) {
          SparqlService raw = delegate.createSparqlService(serviceUri, datasetDescription, httpClient);
          SparqlService r = transform.apply(raw);
          return r;
    }

}
