package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

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
    	Dataset dataset = DatasetFactory.create(datasetGraph);

    	//QueryExecution result = QueryEngineRegistry.get().find(query, datasetGraph, ARQ.getContext());
        //GraphStore graphStore = GraphStoreFactory.create(datasetGraph);
        QueryExecution result = com.hp.hpl.jena.query.QueryExecutionFactory.create(query, dataset);
        return result;
    }

    @Override
    public void close() {
    	if(doClose) {
    		datasetGraph.close();
    	}
    }
}