package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.function.BiFunction;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

public class ProxyImplementation
	extends Implementation
{
	protected BiFunction<? super Node, ? super EnhGraph, ?> ctor;
	
	public ProxyImplementation(BiFunction<? super Node, ? super EnhGraph, ?> ctor) {
		super();
		this.ctor = ctor;
	}
	
	@Override
	public EnhNode wrap(Node node, EnhGraph eg) {
		Object o = ctor.apply(node, eg);
		EnhNode result = (EnhNode)o;
		return result;
	}
	
	@Override
	public boolean canWrap(Node node, EnhGraph eg) {
		return true;
	}
}
