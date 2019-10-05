package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;

public class DataObjectFactoryImpl
	implements PlainDataRefVisitor<RdfDataObject>
{
	protected OpVisitor<? extends RdfDataObject> opExecutor;
	
	public DataObjectFactoryImpl(OpVisitor<? extends RdfDataObject> opExecutor) {
		super();
		this.opExecutor = Objects.requireNonNull(opExecutor);
	}

	@Override
	public RdfDataObject visit(PlainDataRefUrl dataRef) {
		RdfDataObject result = DataObjects.fromUrl(dataRef);
		return result;
	}

	@Override
	public RdfDataObject visit(PlainDataRefSparqlEndpoint dataRef) {
		RdfDataObject result = DataObjects.fromSparqlEndpoint(dataRef);
		return result;
	}

	@Override
	public RdfDataObject visit(PlainDataRefExt dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

	@Override
	public RdfDataObject visit(PlainDataRefCatalog dataRef) {
		throw new RuntimeException("To be done");
	}

	@Override
	public RdfDataObject visit(PlainDataRefOp dataRef) {
		// We assume the Op type here
		Op op = (Op)Objects.requireNonNull(dataRef.getOp());
		RdfDataObject result = op.accept(opExecutor);
		
		return result;
	}

//	@Override
//	public RdfDataObject visit(DataRefEmpty dataRef) {
//		RdfDataObject result = DataObjects.empty();
//		return result;
//	}

}
