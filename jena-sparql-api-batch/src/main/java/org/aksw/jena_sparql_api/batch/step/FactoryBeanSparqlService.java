package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * A singleton factory bean for creating {@link SparqlService} instances.
 *
 * @author raven
 *
 */
public class FactoryBeanSparqlService
    extends AbstractFactoryBean<SparqlService>
{
    private SparqlServiceFactory factory;

    private String service;
    private DatasetDescription dataset;
    private HttpClient httpClient;

    public FactoryBeanSparqlService() {
        super();
        setSingleton(true);
    }

    public SparqlServiceFactory getFactory() {
        return factory;
    }

    @Autowired
    public void setFactory(SparqlServiceFactory factory) {
        this.factory = factory;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public DatasetDescription getDataset() {
        return dataset;
    }

    public void setDataset(DatasetDescription dataset) {
        this.dataset = dataset;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Class<?> getObjectType() {
        return SparqlService.class;
    }

    @Override
    public SparqlService createInstance() throws Exception {
        SparqlService result = factory.createSparqlService(service, dataset, httpClient);
        return result;
    }
}
