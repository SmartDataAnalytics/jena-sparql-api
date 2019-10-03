package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
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
	implements OpVisitor<RdfDataObject>
{
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	
	@Override
	public RdfDataObject visit(OpModel op) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public RdfDataObject visit(OpConstruct op) {
		RdfDataObject result;
		
		Op subOp = op.getSubOp();
		try(RdfDataObject subDataObject = subOp.accept(this)) {
			try(RDFConnection conn = subDataObject.openConnection()) {
				String queryStr = op.getQueryString();
				
				Model model = conn.queryConstruct(queryStr);

				result = DataObjects.fromModel(model);				
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	public RdfDataObject visit(OpUpdateRequest op) {
		Op subOp = op.getSubOp();		
		RdfDataObject subDataObject = subOp.accept(this);
		try(RDFConnection conn = subDataObject.openConnection()) {

			for(String updateRequestStr : op.getUpdateRequests()) {
				conn.update(updateRequestStr);
			}
		}

		return subDataObject;
	}

	@Override
	public RdfDataObject visit(OpUnion op) {
		List<Op> subOps = op.getSubOps();
		
		Model model = ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			try(RdfDataObject subDataObject = subOp.accept(this)) {
				try(RDFConnection conn = subDataObject.openConnection()) {
					Model contribModel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					model.add(contribModel);				
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		RdfDataObject result = DataObjects.fromModel(model);
		return result;
	}

	@Override
	public RdfDataObject visit(OpPersist op) {
		throw new RuntimeException("not implemented yet");
	}

}
