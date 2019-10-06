package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataObjects;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
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
	implements OpVisitor<RdfDataPod>
{
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	protected HttpResourceRepositoryFromFileSystem repo;
	
	public OpExecutorDefault(HttpResourceRepositoryFromFileSystem repo) {
		super();
		this.repo = repo;
	}

	@Override
	public RdfDataPod visit(OpDataRefResource op) {
		PlainDataRef dataRef = op.getDataRef();
		RdfDataPod result = DataObjects.fromDataRef(dataRef, repo, this);
		return result;
	}

	@Override
	public RdfDataPod visit(OpData op) {
		Object data = null; // TODO op.getData();
		RdfDataPod result = DataObjects.fromData(data);
		return result;
	}


	@Override
	public RdfDataPod visit(OpConstruct op) {
		RdfDataPod result;
		
		Op subOp = op.getSubOp();
		try(RdfDataPod subDataObject = subOp.accept(this)) {
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
	public RdfDataPod visit(OpUpdateRequest op) {
		Op subOp = op.getSubOp();		
		RdfDataPod subDataObject = subOp.accept(this);
		try(RDFConnection conn = subDataObject.openConnection()) {

			for(String updateRequestStr : op.getUpdateRequests()) {
				conn.update(updateRequestStr);
			}
		}

		return subDataObject;
	}

	@Override
	public RdfDataPod visit(OpUnion op) {
		List<Op> subOps = op.getSubOps();
		
		Model model = ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			try(RdfDataPod subDataObject = subOp.accept(this)) {
				try(RDFConnection conn = subDataObject.openConnection()) {
					Model contribModel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					model.add(contribModel);				
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		RdfDataPod result = DataObjects.fromModel(model);
		return result;
	}

	@Override
	public RdfDataPod visit(OpPersist op) {
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public RdfDataPod visit(OpVar op) {
		throw new RuntimeException("no handler for variables");
	}

}
