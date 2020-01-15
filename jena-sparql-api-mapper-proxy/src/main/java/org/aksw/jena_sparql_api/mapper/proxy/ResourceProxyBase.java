package org.aksw.jena_sparql_api.mapper.proxy;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

/**
 * Experimental base implementation that is used for generated proxies.
 * The asResource() method returns a plain jena ResourceImpl instead of 'this'.
 * This effectively allows for un-proxying.
 * 
 * Note, that "r == r.asResource() if r is a Resource" no longer holds for proxied resources.
 * It's hard to image someone would rely on that, but one never knows.
 * 
 * The change in behavior is useful e.g. for
 * Apache Spark, where serializers may expect only standard Jena types.
 * 
 * 
 * @author raven
 *
 */
public class ResourceProxyBase
	extends ResourceImpl
{
	public ResourceProxyBase(Node node, EnhGraph enhGraph) {
		super(node, enhGraph);
	}
	
	@Override
	public Resource asResource() {
		Resource result = new ResourceImpl(this.node, this.enhGraph);
		return result;
	}
}
