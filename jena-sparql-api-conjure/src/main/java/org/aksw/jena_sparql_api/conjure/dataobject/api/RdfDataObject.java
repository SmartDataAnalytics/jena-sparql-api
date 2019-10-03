package org.aksw.jena_sparql_api.conjure.dataobject.api;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RdfDataObject
	extends DataObject
{
	/**
	 * Attempt to establish establish an {@link RDFConnection} to the RDF dataset represented
	 * by this object
	 * 
	 * Clients should always eventually invoke RDFConnection.close() on the returned connection
	 * Only a single connection should be established to a dataset at a given time.
	 * Implementations should throw an exception if openConnection is invoked while a prior
	 * connection has not yet been closed.
	 * 
	 * Closing a connection allows to obtain another one at a later time
	 * However, it is not valid to invoke openConnection() after release() was called. 
	 * 
	 * See also {@link java.sql.DataSource}
	 * 
	 * @return
	 */
	RDFConnection openConnection();
}
