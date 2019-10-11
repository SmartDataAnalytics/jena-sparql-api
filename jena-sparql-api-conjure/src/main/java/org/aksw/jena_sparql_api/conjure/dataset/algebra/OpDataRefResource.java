package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

@ResourceView
@RdfTypeNs("rpif")
public interface OpDataRefResource
	extends Op0
{
	@PolymorphicOnly
	@IriNs("rpif")
	DataRef getDataRef();
	OpDataRefResource setDataRef(DataRef dataRef);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpDataRefResource from(Model model, DataRef dataRef) {
		OpDataRefResource result = model
				.createResource().as(OpDataRefResource.class)
				.setDataRef(dataRef);

		return result;
	}

}
