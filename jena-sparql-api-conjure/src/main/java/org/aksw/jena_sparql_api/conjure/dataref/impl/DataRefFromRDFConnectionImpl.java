package org.aksw.jena_sparql_api.conjure.dataref.impl;

import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromRDFConnection;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefVisitor;
import org.apache.jena.rdfconnection.RDFConnection;

public class DataRefFromRDFConnectionImpl
	implements DataRefFromRDFConnection
{
	protected RDFConnection conn;
	
	public DataRefFromRDFConnectionImpl(RDFConnection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public RDFConnection getConnection() {
		return conn;
	}

	public static DataRefFromRDFConnectionImpl create(RDFConnection conn) {
		return new DataRefFromRDFConnectionImpl(conn);
	}
}
