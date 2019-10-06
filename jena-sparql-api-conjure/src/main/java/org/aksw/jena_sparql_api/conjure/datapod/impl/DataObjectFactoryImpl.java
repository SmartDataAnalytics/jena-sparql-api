package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;

public class DataObjectFactoryImpl
	implements PlainDataRefVisitor<RdfDataPod>
{
	protected OpVisitor<? extends RdfDataPod> opExecutor;
	
	public DataObjectFactoryImpl(OpVisitor<? extends RdfDataPod> opExecutor) {
		super();
		this.opExecutor = Objects.requireNonNull(opExecutor);
	}

	@Override
	public RdfDataPod visit(PlainDataRefUrl dataRef) {
		RdfDataPod result = DataObjects.fromUrl(dataRef);
		return result;
	}

	@Override
	public RdfDataPod visit(PlainDataRefSparqlEndpoint dataRef) {
		RdfDataPod result = DataObjects.fromSparqlEndpoint(dataRef);
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

//	@Override
//	public RdfDataObject visit(DataRefEmpty dataRef) {
//		RdfDataObject result = DataObjects.empty();
//		return result;
//	}

}
