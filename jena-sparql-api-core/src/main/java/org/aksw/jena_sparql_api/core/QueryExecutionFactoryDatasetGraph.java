package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;

public class QueryExecutionFactoryDatasetGraph
    extends QueryExecutionFactoryBackQuery
{
    private DatasetGraph datasetGraph;
    private boolean doClose;

//    public QueryExecutionFactoryDatasetGraph() {
//        this(DatasetFactory.createMem());
//    }

    public QueryExecutionFactoryDatasetGraph(DatasetGraph datasetGraph, boolean doClose) {
        this.datasetGraph = datasetGraph;
        this.doClose = doClose;
    }

    public DatasetGraph getDatasetGraph() {
        return datasetGraph;
    }

    @Override
    public String getId() {
        return "" + datasetGraph.hashCode();
    }

    @Override
    public String getState() {
        return "" + datasetGraph.hashCode();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Dataset dataset = DatasetFactory.wrap(datasetGraph);

        //QueryExecution result = QueryEngineRegistry.get().find(query, datasetGraph, ARQ.getContext());
        //GraphStore graphStore = GraphStoreFactory.create(datasetGraph);
        QueryExecution result = org.apache.jena.query.QueryExecutionFactory.create(query, dataset);
        return result;
    }

    @Override
    public void close() {
        if(doClose) {
            datasetGraph.close();
        }
    }
}