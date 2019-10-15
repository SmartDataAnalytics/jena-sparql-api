package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jena_sparql_api.conjure.datapod.impl.RdfDataPodHdt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpMacroCall;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;
import org.aksw.jena_sparql_api.conjure.traversal.engine.FunctionAssembler;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO The visitor should delegate to the executor implementation(s) instead of
// performing operations directly
public class OpExecutorDefault
	implements OpVisitor<RdfDataPod>
{
	protected static final Logger logger = LoggerFactory.getLogger(OpExecutorDefault.class);
	
//	protected DataObjectRdfVisitor<RDFConnection> DataObjectRdfToConnection;

	protected HttpResourceRepositoryFromFileSystemImpl repo;
	
	/**
	 * The task context holds the input record and
	 * an assignment of variables to data references
	 * 
	 */
	protected TaskContext taskContext;

	
	// Would a dry run mode help for computing hashes consistently?
	// Or should this be done separately?
	protected boolean isDryRun;
	
	
	// Execution context
	// TODO Maybe rename this to 'substitution context' as it is mainly used for this purpose
	protected Map<String, Node> execCtx;
	
	
	public OpExecutorDefault(HttpResourceRepositoryFromFileSystem repo, TaskContext taskContext) {
		super();
		// TODO HACK Avoid the down cast
		this.repo = (HttpResourceRepositoryFromFileSystemImpl)repo;
		this.taskContext = taskContext;
		
		this.execCtx = new LinkedHashMap<>();
	}

	public TaskContext getTaskContext() {
		return taskContext;
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
					
					Query query = QueryFactory.create(queryStr);
					Query effQuery = QueryUtils.applyNodeTransform(query,
							x -> x.isVariable() ? execCtx.getOrDefault(x.getName(), x) : x);
					
					// TODO Check whether substitution is needed
					logger.info("Query after substitution: " + effQuery);
					
//					Model contrib = conn.queryConstruct(queryStr);
					Model contrib = conn.queryConstruct(effQuery);
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
		String varName = op.getName();
		Map<String, DataRef> map = taskContext.getDataRefMapping();
		DataRef dataRef = map.get(varName);
		RdfDataPod result = DataPods.fromDataRef(dataRef, repo, this);

		//RdfDataPod result = dataRef.visit(this);
		return result;
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
		Op subOp = op.getSubOp();		
		
		RdfDataPod result = subOp.accept(this);
		String ctxVarName = Objects.requireNonNull(op.getCtxVarName());			
		String queryStr = Objects.requireNonNull(op.getSelector());
		String selVarName = op.getSelectorVarName();
		String pathStr = op.getPropertyPath();
		Path path = pathStr == null ? null : PathParser.parse(pathStr, PrefixMapping.Extended);
		
		Query query = null;
		if(selVarName == null) {
			query = QueryFactory.create(queryStr);
			List<String> resultVars = query.getResultVars();
			if(resultVars.size() != 1) {
				throw new RuntimeException("Require exactly 1 selector result var");
			}
			
			selVarName = resultVars.get(0);
		}
		

		try(RDFConnection conn = result.openConnection()) {
			String selVarN = selVarName;
			RDFNode node = SparqlRx.execSelect(conn, queryStr)
				.map(qs -> qs.get(selVarN))
				.firstElement()
				.blockingGet();

			if(path != null) {
				Set<RDFNode> tgts = FunctionAssembler.execPath(conn, node, path);				
				node = tgts.isEmpty() ? null : tgts.iterator().next();
			}
		
			Node n = node == null ? null : node.asNode();
			
			Node priorValue = execCtx.get(ctxVarName);
			
			logger.info("Updating ctx[" + ctxVarName + "] = " + n + " <- " + priorValue);
			execCtx.put(ctxVarName, n);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;		
	}

	@Override
	public RdfDataPod visit(OpWhen op) {
		String conditionStr = op.getCondition();
		Expr expr = ExprUtils.parse(conditionStr);
		
		// Context to binding
		BindingHashMap binding = new BindingHashMap();
		for(Entry<String, Node> e : execCtx.entrySet()) {
			String k = e.getKey();
			Node v = e.getValue();
		
			if(v != null) {
				binding.add(Var.alloc(k), v);
			}
		}
		//BindingUtils.fromMap(execCtx);
		
		NodeValue val = ExprUtils.eval(expr);
		Op subOp = val.getBoolean()
			? op.getLhs()
			: op.getRhs();
			
		subOp = subOp == null ? OpData.create(ModelFactory.createDefaultModel()) : subOp;

		RdfDataPod result = subOp.accept(this);
		return result;
	}

	@Override
	public RdfDataPod visit(OpError op) {
		throw new RuntimeException("Reached a user error state, user specified reason was: " + op.getReason());
	}

	@Override
	public RdfDataPod visit(OpMacroCall op) {
		throw new RuntimeException("not implemented");
	}

}
