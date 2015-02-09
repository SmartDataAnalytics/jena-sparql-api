package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

public class QueryExecutionFactoryDataset
    extends QueryExecutionFactoryBackQuery
{
    private Dataset dataset;

    public QueryExecutionFactoryDataset() {
        this(DatasetFactory.createMem());
    }

    public QueryExecutionFactoryDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public String getId() {
        return "" + dataset.hashCode();
    }

    @Override
    public String getState() {
        return "" + dataset.hashCode();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = QueryExecutionFactory.create(query, dataset);
        return result;
    }
}