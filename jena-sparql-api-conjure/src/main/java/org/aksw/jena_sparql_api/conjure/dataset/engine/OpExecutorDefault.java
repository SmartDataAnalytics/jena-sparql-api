package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataobject.impl.DataObjects;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;

public class OpExecutorDefault
	implements OpVisitor<RdfDataObject>
{
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	protected HttpResourceRepositoryFromFileSystem repo;
	
	public OpExecutorDefault(HttpResourceRepositoryFromFileSystem repo) {
		super();
		this.repo = repo;
	}

	@Override
	public RdfDataObject visit(OpDataRefResource op) {
		DataRefResource dataRef = op.getDataRef();
		RdfDataObject result = DataObjects.fromDataRef(dataRef, repo);
		return result;
	}

	@Override
	public RdfDataObject visit(OpConstruct op) {
		RdfDataObject result;
		
		Op subOp = op.getSubOp();
		try(RdfDataObject subDataObject = subOp.accept(this)) {
			try(RDFConnection conn = subDataObject.openConnection()) {
				
				Collection<String> queryStrs = op.getQueryStrings();
				
				Model model = ModelFactory.createDefaultModel();
				for(String queryStr : queryStrs) {
					Model contrib = conn.queryConstruct(queryStr);
					model.add(contrib);
				}

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

	@Override
	public RdfDataObject visit(OpVar op) {
		throw new RuntimeException("no handler for variables");
	}

}
