package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionJsa;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * A SparqlService is an object that bundles together related sparql features
 * - i.e. querying and updating.
 *
 *
 * @author raven
 *
 */
public interface SparqlService
//    extends AutoCloseable
{
    /**
     * Returns the default dataset description associated with this service.
     * May be null.
     * @return
     */
    String getServiceUri();
    DatasetDescription getDatasetDescription();

    QueryExecutionFactory getQueryExecutionFactory();
    UpdateExecutionFactory getUpdateExecutionFactory();
    
    default RDFConnection getRDFConnection() {
    	return new RDFConnectionModular(
    			new SparqlQueryConnectionJsa(getQueryExecutionFactory()),
    			new SparqlUpdateConnectionJsa(getUpdateExecutionFactory()),
    			null);
    }
}
