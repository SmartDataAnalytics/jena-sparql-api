package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;

public class DataObjectFactoryImpl
	implements DataRefVisitor<RdfDataObject>
{
	protected OpVisitor<? extends RdfDataObject> opExecutor;
	
	public DataObjectFactoryImpl(OpVisitor<? extends RdfDataObject> opExecutor) {
		super();
		this.opExecutor = Objects.requireNonNull(opExecutor);
	}

	@Override
	public RdfDataObject visit(DataRefFromUrl dataRef) {
		RdfDataObject result = DataObjects.fromUrl(dataRef);
		return result;
	}

	@Override
	public RdfDataObject visit(DataRefFromSparqlEndpoint dataRef) {
		RdfDataObject result = DataObjects.fromSparqlEndpoint(dataRef);
		return result;
	}

	@Override
	public RdfDataObject visit(DataRefExt dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

	@Override
	public RdfDataObject visit(DataRefFromCatalog dataRef) {
		throw new RuntimeException("To be done");
	}

	@Override
	public RdfDataObject visit(DataRefOp dataRef) {
		Op op = Objects.requireNonNull(dataRef.getOp());
		RdfDataObject result = op.accept(opExecutor);
		
		return result;
	}

//	@Override
//	public RdfDataObject visit(DataRefEmpty dataRef) {
//		RdfDataObject result = DataObjects.empty();
//		return result;
//	}

}
