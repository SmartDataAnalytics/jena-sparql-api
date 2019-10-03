package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.RdfDataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;

public class DataObjectFactoryImpl
	implements DataRefVisitor<RdfDataObject>
{
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

}
