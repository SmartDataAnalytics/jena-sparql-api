package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionWrapper;

public abstract class RdfDataObjectBase
	implements RdfDataObject
{
	protected RDFConnection activeConnection = null;
	protected boolean isClosed = false;

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public synchronized void close() {
		if(!isClosed) {
			isClosed = true;

			if(activeConnection != null) {
				activeConnection.close();
				throw new RuntimeException("Data Object was closed, however a connection was still open");
			}
		}
	}

	@Override
	public synchronized RDFConnection openConnection() {
		if(activeConnection != null) {
			throw new RuntimeException("A prior obtained connection has not yet been closed");
		}
		
		RDFConnection core = newConnection();

		activeConnection = new RDFConnectionWrapper(core) {
			
			// TODO Intercept and reject update calls if isMutable is false
			
			@Override
			public void close() {
				// Prevent from e.g. running this while e.g. DataObject.close() is called
				synchronized(RdfDataObjectBase.this) {
					activeConnection = null;
				}
				super.close();
			}
		};
		
		return activeConnection;
	}
	
	abstract protected RDFConnection newConnection();

}
