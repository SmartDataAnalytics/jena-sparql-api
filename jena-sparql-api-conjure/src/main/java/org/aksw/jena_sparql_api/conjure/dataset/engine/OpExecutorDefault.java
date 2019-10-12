package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jena_sparql_api.conjure.datapod.impl.RdfDataPodHdt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;

// TODO The visitor should delegate to the executor implementation(s) instead of
// performing operations directly
public class OpExecutorDefault
	implements OpVisitor<RdfDataPod>
{
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	protected HttpResourceRepositoryFromFileSystemImpl repo;
	
	// Execution context
	protected Map<Var, Node> execCtx;
	
	
	public OpExecutorDefault(HttpResourceRepositoryFromFileSystem repo) {
		super();
		// TODO HACK Avoid the down cast
		this.repo = (HttpResourceRepositoryFromFileSystemImpl)repo;
	}

	/**
	 * Check the repository for whether it can supply an entity for the hash 
	 * 
	 * @param hash
	 * @return
	 */
	public <T extends RDFNode> RdfDataPod wrapWithGetFromHash(T op, Function<T, RdfDataPod> generator) {
		String hash = ResourceTreeUtils.createGenericHash(op);
		RdfDataPod result = DataPods.create(hash, repo);
		return result;
	}
	
	@Override
	public RdfDataPod visit(OpDataRefResource op) {
		PlainDataRef dataRef = op.getDataRef();
		RdfDataPod result = DataPods.fromDataRef(dataRef, repo, this);
		return result;
	}

	
	@Override
	public RdfDataPod visit(OpData op) {
		Object data = null; // TODO op.getData();
		RdfDataPod result = DataPods.fromData(data);
		return result;
	}


	@Override
	public RdfDataPod visit(OpConstruct op) {
		RdfDataPod result;
		
		Op subOp = op.getSubOp();
		try(RdfDataPod subDataPod = subOp.accept(this)) {
			try(RDFConnection conn = subDataPod.openConnection()) {
				
				Collection<String> queryStrs = op.getQueryStrings();
				
				Model model = ModelFactory.createDefaultModel();
				for(String queryStr : queryStrs) {
					Model contrib = conn.queryConstruct(queryStr);
					model.add(contrib);
				}

				result = DataPods.fromModel(model);				
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	public RdfDataPod visit(OpUpdateRequest op) {
		Op subOp = op.getSubOp();		
		RdfDataPod subDataPod = subOp.accept(this);
		try(RDFConnection conn = subDataPod.openConnection()) {

			for(String updateRequestStr : op.getUpdateRequests()) {
				conn.update(updateRequestStr);
			}
		}

		return subDataPod;
	}

	@Override
	public RdfDataPod visit(OpUnion op) {
		List<Op> subOps = op.getSubOps();
		
		Model model = ModelFactory.createDefaultModel();
		for(Op subOp : subOps) {
			try(RdfDataPod subDataPod = subOp.accept(this)) {
				try(RDFConnection conn = subDataPod.openConnection()) {
					Model contribModel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
					model.add(contribModel);				
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		RdfDataPod result = DataPods.fromModel(model);
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

	@Override
	public RdfDataPod visit(OpCoalesce op) {
		List<Op> subOps = op.getSubOps();
		
		RdfDataPod result = null;
		for(Op subOp : subOps) {
			result = subOp.accept(this);
			
			try(RDFConnection conn = result.openConnection()) {
				Model contribModel = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o } LIMIT 1");
				if(contribModel.isEmpty()) {
					try {
						result.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				} else {
					break;
				}
			}
		}
		
		if(result == null) {
			result = DataPods.empty();
		}
		
		return result;

	}

	@Override
	public RdfDataPod visit(OpHdtHeader op) {
		Op subOp = op.getSubOp();		
		
		RdfDataPod result;
		try(RdfDataPod subDataPod = subOp.accept(this)) {
			if(subDataPod instanceof RdfDataPodHdt) {
				result = ((RdfDataPodHdt)subDataPod).headerPod();
			} else {
				result = DataPods.empty();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	/**
	 * Execute all arguments and return the result of the last one
	 */
	@Override
	public RdfDataPod visit(OpSequence op) {
		RdfDataPod result = null;
		
		List<Op> subOps = op.getSubOps();
		int n = subOps.size();
		
		for(int i = 0; i < n; ++i) {
			boolean isLast = i + 1 == n;

			Op subOp = subOps.get(i);
			RdfDataPod tmp = subOp.accept(this);
			if(isLast) {
				result = tmp;
			} else {
				try {
					tmp.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		if(result == null) {
			result = DataPods.empty();
		}
		
		return result;
	}

	
	@Override
	public RdfDataPod visit(OpSet op) {
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public RdfDataPod visit(OpWhen op) {
		throw new RuntimeException("not implemented yet");
	}

	@Override
	public RdfDataPod visit(OpError op) {
		throw new RuntimeException("Reached a user error state, user specified reason was: " + op.getReason());
	}

}
