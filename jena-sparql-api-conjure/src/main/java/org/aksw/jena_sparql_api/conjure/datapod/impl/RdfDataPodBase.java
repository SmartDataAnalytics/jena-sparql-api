package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.Set;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionWrapper;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;

public abstract class RdfDataPodBase
	implements RdfDataPod
{
	protected Set<RDFConnection> openConnections = Sets.newIdentityHashSet();
	protected boolean isClosed = false;

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public synchronized void close() throws Exception{
		if(!isClosed) {
			isClosed = true;

			if(!openConnections.isEmpty()) {
				//activeConnection.close();
				throw new RuntimeException("DataPod was closed, however " + openConnections.size() + " connections were still open");
			}
		}
	}

	@Override
	public synchronized RDFConnection openConnection() {
//		if(openConnections.isEmpty()) {
//			throw new RuntimeException("A prior obtained connection has not yet been closed");
//		}
		
		RDFConnection core = newConnection();

		RDFConnection[] newConnection = new RDFConnection[] { null };
		newConnection[0] = new RDFConnectionWrapper(core) {
			
			// TODO Intercept and reject update calls if isMutable is false
			
			@Override
			public void close() {
				// Prevent from e.g. running this while e.g. DataObject.close() is called
				synchronized(RdfDataPodBase.this) {
					openConnections.remove(newConnection[0]);
				}
				super.close();
			}
		};
		
		return newConnection[0];
	}
	
	abstract protected RDFConnection newConnection();

}
