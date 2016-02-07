package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;

import com.google.common.base.Function;
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
            DatasetDescription datasetDescription, Object authenticator) {
          SparqlService raw = delegate.createSparqlService(serviceUri, datasetDescription, authenticator);
          SparqlService r = transform.apply(raw);
          return r;
    }

}
