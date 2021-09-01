package org.aksw.difs.engine;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.engine.QueryExecutionBase;

public class QueryExecutionFactoryQuadForm {
    public static QueryExecution create(Query query, Dataset dataset) {
        return new QueryExecutionBase(query, dataset, null, QueryEngineMainQuadForm.FACTORY);
    }
}
