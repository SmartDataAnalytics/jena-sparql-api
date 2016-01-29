package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import org.apache.jena.sparql.core.DatasetDescription;

public class FactoryBeanSparqlService
    extends AbstractFactoryBean<SparqlService>
{
    private SparqlServiceFactory factory;

    private String service;
    private DatasetDescription dataset;
    private Object auth;

    public FactoryBeanSparqlService() {
        super();
        setSingleton(false);
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

    public Object getAuth() {
        return auth;
    }

    public void setAuth(Object auth) {
        this.auth = auth;
    }


    @Override
    public Class<?> getObjectType() {
        return SparqlService.class;
    }

    @Override
    public SparqlService createInstance() throws Exception {
        SparqlService result = factory.createSparqlService(service, dataset, auth);
        return result;
    }
}
