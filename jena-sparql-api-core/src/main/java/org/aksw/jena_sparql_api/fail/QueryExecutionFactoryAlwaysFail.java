package org.aksw.jena_sparql_api.fail;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBase;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * QueryExecutionFactory that always fails.
 * Useful for test cases that should operate
 * on cached queries only. This qef can be used
 * as a backend to a cache.
 * 
 * @author raven Mar 26, 2018
 *
 */
public class QueryExecutionFactoryAlwaysFail
	extends QueryExecutionFactoryBase
{
	protected String serviceId;
	protected DatasetDescription datasetDescription;
	
	public QueryExecutionFactoryAlwaysFail(String serviceId, DatasetDescription datasetDescription) {
		super();
		this.serviceId = serviceId;
		this.datasetDescription = datasetDescription;
	}

	@Override
	public String getId() {
		return serviceId;
	}

	@Override
	public String getState() {
        String result = DatasetDescriptionUtils.toString(datasetDescription);

        //TODO Include authenticator
        return result;
	}

	@Override
	public QueryExecution createQueryExecution(String queryString) {
		//throw new RuntimeException("Query sent to always failing query execution factory");
		QueryExecution result = new QueryExecutionAlwaysFail(queryString);
		return result;
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {
		//throw new RuntimeException("Query sent to always failing query execution factory");
		QueryExecution result = new QueryExecutionAlwaysFail(query);
		return result;
	}
}
