package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObjectRdf;
import org.apache.jena.rdfconnection.RDFConnection;

public class DataObjectRdfBase
	implements DataObjectRdf
{
	protected RDFConnection conn;
	protected boolean isMutable;
	
	public DataObjectRdfBase(RDFConnection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public boolean isMutable() {
		return isMutable;
	}

	@Override
	public void release() {
		conn.close();
	}

	@Override
	public RDFConnection getConnection() {
		return conn;
	}
}
