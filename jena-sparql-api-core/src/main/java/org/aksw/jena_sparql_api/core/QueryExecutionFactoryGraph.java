package org.aksw.jena_sparql_api.core;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Use QueryExecutionFactoryModel instead
 * @author raven
 *
 */
@Deprecated
public class QueryExecutionFactoryGraph
    extends QueryExecutionFactoryBackQuery
{
    private Graph graph;
    private boolean doClose;

//    public QueryExecutionFactoryDatasetGraph() {
//        this(DatasetFactory.createMem());
//    }

    public QueryExecutionFactoryGraph(Graph graph, boolean doClose) {
        this.graph = graph;
        this.doClose = doClose;
    }

    public Graph getGraph() {
        return graph;
    }

    @Override
    public String getId() {
        return "" + graph.hashCode();
    }

    @Override
    public String getState() {
        return "" + graph.hashCode();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        //GraphStore graphStore = GraphStoreFactory.create(graph);
        Model model = ModelFactory.createDefaultModel();
        QueryExecution result = org.apache.jena.query.QueryExecutionFactory.create(query, model);
        return result;
    }

    @Override
    public void close() {
        if(doClose) {
            graph.close();
        }
    }
}