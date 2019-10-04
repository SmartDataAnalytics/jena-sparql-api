package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefResourceEmpty
	extends DataRefFromUrl, DataRefResource
{
	@Override
	default <T> T accept2(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefResourceEmpty create() {
		DataRefResourceEmpty result = ModelFactory.createDefaultModel().createResource().as(DataRefResourceEmpty.class);
		return result;
	}
}
