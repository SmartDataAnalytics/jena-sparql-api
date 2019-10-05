package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfType
public interface OpDataRefResource
	extends Op0
{
	@PolymorphicOnly
	@IriNs("eg")
	DataRef getDataRef();
	OpDataRefResource setDataRef(DataRef dataRef);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static OpDataRefResource from(DataRef dataRef) {
		OpDataRefResource result = dataRef.getModel()
				.createResource().as(OpDataRefResource.class)
				.setDataRef(dataRef);

		return result;
	}

}
