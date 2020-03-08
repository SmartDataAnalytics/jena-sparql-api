package org.aksw.jena_sparql_api.utils.model;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * A Resource linked to a Dataset in addition to its model.
 * 
 * @author raven
 *
 */
public interface ResourceInDataset
	extends Resource
{
	/**
	 * The named graph such that
	 * r.getDataset().getNamedGraph(r.getGraphName()).equals(r.getModel())
	 * While the model and possibly graph instances may not be reference equal due to being different views,
	 * they should still operate on the same underlying collection of triples.
	 * 
	 * @return
	 */
	String getGraphName();
	Dataset getDataset();

	ResourceInDataset inDataset(Dataset other);
	
	/**
	 * Pass itself to a consumer and return this.
	 * Useful for retaining the type when adding properties to a ResourceInDataset: 
	 * 
	 * flowOrStream.map(ResourceInDatasetImpl.createX().acceptResource(r -> r.addProperty(foo, bar));
	 * 
	 * 
	 * @return
	 */
	default ResourceInDataset applyOnResource(Consumer<? super Resource> consumer) {
		consumer.accept(this);
		return this;
	}
	
	//default ResourceInDataset yieldFromModel(Function<? super Model, Resource> fn) {
	/**
	 * Create a new resource in the same graph/model as this resource
	 * 
	 * @return
	 */
	default ResourceInDataset wrapCreate(Function<? super Model, ? extends Resource> fn) {
		Dataset ds = getDataset();
		String gn = getGraphName();
		Model m = getModel();
		Resource r = fn.apply(m);
		Node n = r.asNode();
		
		return new ResourceInDatasetImpl(ds, gn, n);
	}
}
