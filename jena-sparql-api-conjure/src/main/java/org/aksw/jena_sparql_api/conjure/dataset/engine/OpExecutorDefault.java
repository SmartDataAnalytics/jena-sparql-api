package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObjectRdf;
import org.aksw.jena_sparql_api.conjure.dataobject.impl.DataObjects;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpModel;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;

public class OpExecutorDefault
	implements OpVisitor<DataObjectRdf>
{
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	
	@Override
	public DataObjectRdf visit(OpModel op) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public DataObjectRdf visit(OpConstruct op) {
		Op subOp = op.getSubOp();
		DataObjectRdf subDataObject = subOp.accept(this);

		Model model;
		try {
			
			String queryStr = op.getQueryString();
			
			RDFConnection conn = subDataObject.getConnection();
			model = conn.queryConstruct(queryStr);
		} finally {		
			subDataObject.release();
		}

		DataObjectRdf result = DataObjects.fromModel(model);
		return result;
	}

	@Override
	public DataObjectRdf visit(OpUpdateRequest op) {
		Op subOp = op.getSubOp();		
		DataObjectRdf subDataObject = subOp.accept(this);
		RDFConnection conn = subDataObject.getConnection();

		for(String updateRequestStr : op.getUpdateRequests()) {
			conn.update(updateRequestStr);
		}

		return subDataObject;
	}

	@Override
	public DataObjectRdf visit(OpUnion op) {
		List<Op> subOps = op.getSubOps();
		
		Model model = ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			DataObjectRdf subDataObject = subOp.accept(this);
			try {
				RDFConnection conn = subDataObject.getConnection();
				Model contribModel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
				model.add(contribModel);				
			} finally {
				subDataObject.release();
			}
		}
		
		DataObjectRdf result = DataObjects.fromModel(model);
		return result;
	}

	@Override
	public DataObjectRdf visit(OpPersist op) {
		throw new RuntimeException("not implemented yet");
	}

}
