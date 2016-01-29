package org.aksw.jena_sparql_api.model;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 8/3/11
 *         Time: 11:35 PM
 */
public class QueryExecutionFactoryModel
    extends QueryExecutionFactoryBackQuery
{
    private Model model;

    public QueryExecutionFactoryModel()
    {
        this.model = ModelFactory.createDefaultModel();
    }

    public QueryExecutionFactoryModel(Graph graph) {
        this(ModelFactory.createModelForGraph(graph));
    }

    public QueryExecutionFactoryModel(Model model)
    {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public String getId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = QueryExecutionFactory.create(query, model);
        //QueryExecution result = QueryExecutionWrapper.wrap(tmp);
        return result;
    }
}
