package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPodFactoryImpl
	implements PlainDataRefVisitor<RdfDataPod>
{
	private static Logger logger = LoggerFactory.getLogger(DataPodFactoryImpl.class);
	
	protected OpVisitor<? extends RdfDataPod> opExecutor;
	
	public DataPodFactoryImpl(OpVisitor<? extends RdfDataPod> opExecutor) {
		super();
		this.opExecutor = Objects.requireNonNull(opExecutor);
	}

	@Override
	public RdfDataPod visit(PlainDataRefUrl dataRef) {
		throw new RuntimeException("no user handler");
		
//		// Check the static datasets of the executor first
//		// TODO HACK - Add an interface to access an executor's task context
//		String url = dataRef.getDataRefUrl();
//		
//		TaskContext context = ((OpExecutorDefault)opExecutor).getTaskContext();
//		Model m = context.getCtxModels().get(url);
//		RdfDataPod result;
//		if(m != null) {
//			logger.info("Accessed input model");
//			result = DataPods.fromModel(m);
//		} else {
//			result = DataPods.fromUrl(dataRef);
//		}
//		
//		return result;
	}

	@Override
	public RdfDataPod visit(PlainDataRefSparqlEndpoint dataRef) {
		RdfDataPod result = DataPods.fromSparqlEndpoint(dataRef);
		return result;
	}

	@Override
	public RdfDataPod visit(PlainDataRefExt dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

	@Override
	public RdfDataPod visit(PlainDataRefCatalog dataRef) {
		throw new RuntimeException("To be done");
	}

	@Override
	public RdfDataPod visit(PlainDataRefOp dataRef) {
		// We assume the Op type here
		Op op = (Op)Objects.requireNonNull(dataRef.getOp());
		RdfDataPod result = op.accept(opExecutor);
		
		return result;
	}

	@Override
	public RdfDataPod visit(PlainDataRefDcat dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

	@Override
	public RdfDataPod visit(PlainDataRefGit dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

//	@Override
//	public RdfDataObject visit(DataRefEmpty dataRef) {
//		RdfDataObject result = DataObjects.empty();
//		return result;
//	}

}
