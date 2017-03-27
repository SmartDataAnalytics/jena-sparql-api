package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.QueryExecution;

public class FactoryBeanSparqlFile
    extends AbstractFactoryBean<SparqlService>
{
    private String fileNameOrUrl;

    public FactoryBeanSparqlFile() {
        super();
        setSingleton(false);
    }

    public String getFileNameOrUrl() {
        return fileNameOrUrl;
    }


    public void setFileNameOrUrl(String fileNameOrUrl) {
        this.fileNameOrUrl = fileNameOrUrl;
    }


    @Override
    public Class<?> getObjectType() {
        return SparqlService.class;
    }

    @Override
    public SparqlService createInstance() throws Exception {
        Graph graph = new GraphResource(this.fileNameOrUrl);
        QueryExecutionFactory qef = new QueryExecutionFactoryModel(graph);

        // Test whether the resource works
        QueryExecution qe = qef.createQueryExecution("Ask { ?s ?p ?o }");
        boolean ask = qe.execAsk();


        SparqlService result = new SparqlServiceImpl(qef, null);
        return result;
    }
}
