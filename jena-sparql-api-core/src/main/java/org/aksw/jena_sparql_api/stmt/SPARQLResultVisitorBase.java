package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

//public abstract class SPARQLResultVisitorBase
//	implements SPARQLResultVisitor<Void> 
//{
//	protected abstract void $onBoolean(Boolean value);
//	protected abstract void $onJson(JsonArray value);
//	protected abstract void $onQuad(Quad value);
//	protected abstract void $onModel(Model value);
//	protected abstract void $onDataset(Dataset value);
//	protected abstract void $onResultSet(ResultSet value);
//	
//	@Override
//	public Void onBoolean(Boolean value) {
//		$onBoolean(value);
//		return null;
//	}
//	
//	@Override
//	public Void onJson(JsonArray value) {
//		$onJson(value);
//		return null;
//	}
//	
//	@Override
//	public Void onQuad(Quad value) {
//		$onQuad(value);
//		return null;
//	}
//	
//	@Override
//	public Void onModel(Model value) {
//		$onModel(value);
//		return null;
//	}
//	
//	@Override
//	public Void onDataset(Dataset value) {
//		$onDataset(value);
//		return null;
//	}
//	
//	@Override
//	public Void onResultSet(ResultSet value) {
//		$onResultSet(value);
//		return null;
//	}
//
//}
