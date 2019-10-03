package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObject;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;

public class DataObjectFactoryImpl
	implements DataRefVisitor<DataObject>
{
	@Override
	public DataObject visit(DataRefFromUrl dataRef) {
		DataObject result = DataObjects.fromUrl(dataRef);
		return result;
	}

	@Override
	public DataObject visit(DataRefFromSparqlEndpoint dataRef) {
		DataObject result = DataObjects.fromSparqlEndpoint(dataRef);
		return result;
	}

	@Override
	public DataObject visit(DataRefExt dataRef) {
		throw new RuntimeException("No override with custom handler");
	}

	@Override
	public DataObject visit(DataRefFromCatalog dataRef) {
		throw new RuntimeException("To be done");
	}

}
