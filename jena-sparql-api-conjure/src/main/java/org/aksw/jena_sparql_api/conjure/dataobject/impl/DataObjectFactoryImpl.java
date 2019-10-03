package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObject;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromEntity;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromRDFConnection;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromRemoteSparqlDataset;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefVisitor;

public class DataObjectFactoryImpl
	implements DataRefVisitor<DataObject>
{

	@Override
	public DataObject visit(DataRefFromUrl dataRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataObject visit(DataRefFromEntity dataRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataObject visit(DataRefFromRemoteSparqlDataset dataRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataObject visit(DataRefFromRDFConnection dataRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataObject visit(DataRefExt dataRef) {
		// TODO Auto-generated method stub
		return null;
	}

}
