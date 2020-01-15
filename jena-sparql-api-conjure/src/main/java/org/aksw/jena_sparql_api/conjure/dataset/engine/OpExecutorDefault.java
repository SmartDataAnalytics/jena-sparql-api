package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;
import org.aksw.jena_sparql_api.conjure.traversal.engine.FunctionAssembler;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.http.repository.impl.ResourceStoreImpl;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.jena.ext.com.google.common.hash.HashCode;
import org.apache.jena.ext.com.google.common.hash.HashFunction;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFFormat;
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
	
	protected RDFFormat persistRdfFormat;

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
	// protected BindingMap execCtx;
	
	
	public OpExecutorDefault(
			HttpResourceRepositoryFromFileSystem repo,
			TaskContext taskContext,
			Map<String, Node> execCtx,
			RDFFormat persistRdfFormat) {
		super();
		// TODO HACK Avoid the down cast
		this.repo = (HttpResourceRepositoryFromFileSystemImpl)repo;
		this.taskContext = taskContext;
		
		this.execCtx = execCtx; 
		this.persistRdfFormat = persistRdfFormat;
		//this.execCtx = binding;
		//this.execCtx = new LinkedHashMap<>();
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

					// Apply substitution of variables in the query pattern
					// with values of variables in the context
					Query effQuery = QueryUtils.applyNodeTransform(query,
							x -> x.isVariable() ? execCtx.getOrDefault(x.getName(), x) : x);
					
					// TODO Check whether substitution is needed
//					logger.info("Query before substitution: " + queryStr);
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

		HashCode completeHash = computeOpHash(op, taskContext);

		String hashStr = completeHash.toString();
		
		ResourceStore hashStore = repo.getHashStore();
		
		//RdfHttpResourceFile cacheEntry = hashStore.getResource(hashStr);
		//hashStore.
		HttpUriRequest baseRequest =
				RequestBuilder.get(hashStr)
				.setHeader(HttpHeaders.ACCEPT, "application/x-hdt")
				.setHeader(HttpHeaders.ACCEPT_ENCODING, "identity,bzip2,gzip")
				.build();

		RdfDataPod result = null;
		
		HttpRequest effectiveRequest = HttpResourceRepositoryFromFileSystemImpl.expandHttpRequest(baseRequest);
		logger.info("Expanded HTTP Request: " + effectiveRequest);
		try {
			RdfHttpEntityFile entity = repo.get(effectiveRequest, null);
			if(entity != null) {
				String pathStr = entity.getAbsolutePath().toString();
				result = DataPods.fromUrl(pathStr);
			}
			
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		if(result == null) {
			Op subOp = op.getSubOp();
			try(RdfDataPod pod = subOp.accept(this)) {
				Model m;
				try {
					m = ResourceStoreImpl.requestModel(repo, hashStore, hashStr, persistRdfFormat, () -> pod.getModel()).getValue();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				result = DataPods.fromModel(m);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
		

		return result;
	}

// Old implementation of visit(OpPersist) - delete if the new impl works	
//	
//	try(RDFConnection conn = pod.openConnection()) {
//		Model m = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
//		
//		java.nio.file.Path tmpFile = Files.createTempFile("data-", ".ttl");
//		try(OutputStream out = Files.newOutputStream(tmpFile, StandardOpenOption.WRITE)) {
//			RDFDataMgr.write(out, m, RDFFormat.TURTLE_PRETTY);
//		} catch (IOException e1) {
//			throw new RuntimeException(e1);
//		}
//		
//		RdfEntityInfo entityInfo = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class)
//				.setContentType(WebContent.contentTypeTurtle);
//		RdfHttpEntityFile ent = hashStore.putWithMove(hashStr, entityInfo, tmpFile);
//		HttpResourceRepositoryFromFileSystemImpl.computeHashForEntity(ent, null);
//		
//		result = DataPods.fromUrl(ent.getAbsolutePath().toUri().toString());
//		
//	} catch (IOException e2) {
//		throw new RuntimeException(e2);
//	}
//	
//} catch (Exception e3) {
//	throw new RuntimeException(e3);
//}

	public static HashCode computeOpHash(OpPersist op, TaskContext taskContext) {
		HashFunction hashFn = Hashing.sha256();
		
		// The sup without operators that do not modify the result
		Op semanticSubOp = OpUtils.stripCache(op);
		HashCode subOpHash = ResourceTreeUtils.createGenericHash2(semanticSubOp);

		// Hash the task context
		// HashCode taskcon
		HashCode inputRecordHash = ResourceTreeUtils.createGenericHash2(taskContext.getInputRecord());

		List<HashCode> dataRefHashes = taskContext.getDataRefMapping().entrySet().stream().map(e ->
				Hashing.combineOrdered(Arrays.asList(
					Hashing.sha256().hashString(e.getKey(), StandardCharsets.UTF_8),
					ResourceTreeUtils.createGenericHash2(e.getValue()))))
			.collect(Collectors.toList());
		
		if(dataRefHashes.isEmpty()) {
			dataRefHashes.add(hashFn.hashInt(0));
		}
		
		// Hash the data refs
		HashCode dataRefHash = Hashing.combineUnordered(dataRefHashes);

		List<HashCode> ctxModelHashes = taskContext.getCtxModels().entrySet().stream().map(e ->
				Hashing.combineOrdered(Arrays.asList(
					hashFn.hashString(e.getKey(), StandardCharsets.UTF_8),
					ResourceTreeUtils.generateModelHash(e.getValue(), hashFn))))
			.collect(Collectors.toList());
		
		if(ctxModelHashes.isEmpty()) {
			ctxModelHashes.add(hashFn.hashInt(0));
		}

		HashCode ctxModelsHash = Hashing.combineUnordered(ctxModelHashes);

		HashCode completeHash = Hashing.combineOrdered(Arrays.asList(
				subOpHash,
				inputRecordHash,
				ctxModelsHash,
				dataRefHash));
		return completeHash;
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
				if(!contribModel.isEmpty()) {
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
			try {
				result.close();
			} catch (Exception e1) {
				// Ignored in favor of 'e'
			}

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
