package org.aksw.jena_sparql_api.transform.result_set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.graph.NodeTransform;

public class QueryExecutionFactoryTransformResult
	extends QueryExecutionFactoryDecorator
{
	protected NodeTransform nodeTransform;
	
	public QueryExecutionFactoryTransformResult(QueryExecutionFactory decoratee, NodeTransform nodeTransform) {
		super(decoratee);
		this.nodeTransform = nodeTransform;
	}

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        throw new RuntimeException("Query must be parsed");
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution tmp = super.createQueryExecution(query);
        QueryExecutionTransformResult result = new QueryExecutionTransformResult(tmp, nodeTransform);
        return result;
    }
}
