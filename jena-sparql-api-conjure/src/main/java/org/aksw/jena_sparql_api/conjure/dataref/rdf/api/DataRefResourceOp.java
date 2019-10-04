package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefOp;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType
public interface DataRefResourceOp
	extends DataRefOp, DataRefResource
{
	@IriNs("eg")
	@PolymorphicOnly
	DataRefResourceOp setOp(Op dataRef);

	@Override
	default <T> T accept2(DataRefResourceVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefResourceOp create(Op op) {
		DataRefResourceOp result = op.getModel().createResource().as(DataRefResourceOp.class)
				.setOp(op);
		return result;
	}

}
