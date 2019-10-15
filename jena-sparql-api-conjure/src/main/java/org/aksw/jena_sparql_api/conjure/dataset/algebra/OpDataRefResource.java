package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

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
	
	@Override
	default OpDataRefResource clone(Model cloneModel, List<Op> subOps) {
		// TODO Here we also clone the data ref, which might be undesired
		Resource tmpDataRef = getDataRef();
		Model tmp = ResourceUtils.reachableClosure(tmpDataRef);
		cloneModel.add(tmp);
		Resource dataRef = tmpDataRef.inModel(cloneModel);
		DataRef cloneDataRef = JenaPluginUtils.polymorphicCast(dataRef, DataRef.class);
		
		return this.inModel(cloneModel).as(OpDataRefResource.class)
				.setDataRef(cloneDataRef);
	}

	public static OpDataRefResource from(Model model, DataRef dataRef) {
		OpDataRefResource result = model
				.createResource().as(OpDataRefResource.class)
				.setDataRef(dataRef);

		return result;
	}

}
