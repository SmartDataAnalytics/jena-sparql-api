package org.aksw.jena_sparql_api.arq.core.query;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.sparql.util.Context;

public class QueryExecutionFactoryDataset
    implements QueryExecutionFactoryQuery
{
    protected Dataset dataset;
    protected Context context;
    protected QueryEngineFactoryProvider queryEngineFactoryProvider;


    public QueryExecutionFactoryDataset() {
        this(DatasetFactory.create());
    }

    public QueryExecutionFactoryDataset(Dataset dataset) {
        this(dataset, null);
    }

    public QueryExecutionFactoryDataset(Dataset dataset, Context context) {
        this(dataset, context, QueryEngineRegistry.get()::find);
    }

    public QueryExecutionFactoryDataset(Dataset dataset, Context context, QueryEngineFactoryProvider queryEngineFactoryProvider) {
        super();
        this.dataset = dataset;
        this.context = context;
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
    }

 // QueryEngineRegistry.get().find(query, dsg, context);

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        // Copied from internals of jena's QueryExecutionFactory.create(query, dataset);
        query.setResultVars() ;
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext.
        DatasetGraph dsg = null ;
        if ( dataset != null )
            dsg = dataset.asDatasetGraph() ;
        QueryEngineFactory f = queryEngineFactoryProvider.find(query, dsg, context);
        if ( f == null )
        {
            Log.warn(QueryExecutionFactory.class, "Failed to find a QueryEngineFactory for query: "+query) ;
            return null ;
        }
        //dataset.begin(ReadWrite.WRITE);
        QueryExecutionBase tmp = new QueryExecutionBase(query, dataset, context, f) ;

        // TODO We shouldn't wrap with txn here
        QueryExecution result = new QueryExecutionDecoratorTxn<QueryExecution>(tmp, dsg);
        return result;
    }
}
