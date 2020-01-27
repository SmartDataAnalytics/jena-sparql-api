package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefOp;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;

@ResourceView
@RdfTypeNs("rpif")
public interface DataRefOp
	extends PlainDataRefOp, DataRef
{
	@IriNs("rpif")
	@PolymorphicOnly
	DataRefOp setOp(Op dataRef);
	Op getOp();


	@Override
	default <T> T accept2(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	public static DataRefOp create(Op op) {
		DataRefOp result = op.getModel().createResource().as(DataRefOp.class)
				.setOp(op);
		return result;
	}

}
