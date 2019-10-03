package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.conjure.dataref.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefVisitor;
import org.apache.jena.rdfconnection.RDFConnection;

public class OpExecutorDefault
	implements OpVisitor<DataRef>
{
	protected DataRefVisitor<RDFConnection> dataRefToConnection;

	
	@Override
	public DataRef visit(OpModel op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataRef visit(OpConstruct op) {
		Op subOp = op.getSubOp();
		DataRef subDataRef = subOp.accept(this);
		
		
		String queryStr = op.getQueryString();
//		try(QueryExecution qe = conn.query(queryStr)) {
//			Iterator<Triple> it = qe.execConstructTriples();
//			
//		}
		throw new RuntimeException("not implemented yet");

	}

	@Override
	public DataRef visit(OpUpdateRequest op) {
		Op subOp = op.getSubOp();
		DataRef subDataRef = subOp.accept(this);
		
		for(String updateRequestStr : op.getUpdateRequests()) {
			//conn.update(updateRequestStr);
		}
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public DataRef visit(OpUnion op) {
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public DataRef visit(OpPersist op) {
		throw new RuntimeException("not implemented yet");
	}

}
