package org.aksw.jena_sparql_api.core;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;

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

    @Override
    public void close() {
        dataset.close();
    }
}