package org.aksw.jena_sparql_api.core;

import com.google.common.base.Predicate;
import org.apache.jena.sparql.core.DatasetDescription;

public class SparqlServiceFactoryDefaults
    implements SparqlServiceFactory
{
    protected SparqlServiceFactory delegate;
    protected String defaultServiceUri;
    protected Predicate<String> serviceUriValidator;

    public SparqlServiceFactoryDefaults(SparqlServiceFactory delegate, String defaultServiceUri, Predicate<String> serviceUriValidator) {
        super();
        this.delegate = delegate;
        this.defaultServiceUri = defaultServiceUri;
        this.serviceUriValidator = serviceUriValidator;
    }

    @Override
    public SparqlService createSparqlService(
            String serviceUri, DatasetDescription datasetDescription, Object authenticator) {

        if(serviceUri == null) {
            serviceUri = defaultServiceUri;
        } else if(serviceUriValidator != null) {
            boolean isValidService = serviceUriValidator.apply(serviceUri);
            if(!isValidService) {
                throw new RuntimeException("Access to service not allowed " + serviceUri);
            }
        }

        SparqlService result = delegate.createSparqlService(serviceUri, datasetDescription, authenticator);
        return result;
    }
}
