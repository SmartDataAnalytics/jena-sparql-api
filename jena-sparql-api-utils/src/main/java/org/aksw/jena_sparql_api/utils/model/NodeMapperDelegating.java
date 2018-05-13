package org.aksw.jena_sparql_api.utils.model;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;

public class NodeMapperDelegating<T>
	implements NodeMapper<T>
{
	protected Class<?> javaClass;
	protected Function<? super T, Node> toNode;
	protected Function<? super Node, T> toJava;
	protected Predicate<? super Node> canMap;
	
	public NodeMapperDelegating(
			Class<?> javaClass,
			Predicate<? super Node> canMap,
			Function<? super T, Node> toNode,
			Function<? super Node, T> toJava) {
		super();
		this.javaClass = javaClass;
		this.canMap = canMap;
		this.toNode = toNode;
		this.toJava = toJava;
	}
	
	public Class<?> getJavaClass() {
		return javaClass;
	}

	@Override
	public boolean canMap(Node node) {
		boolean result = canMap.test(node);
		return result;
	}
	
	@Override
	public Node toNode(T obj) {
		Node result = toNode.apply(obj);
		return result;
	}

	@Override
	public T toJava(Node node) {
		T result = toJava.apply(node);
		return result;
	}
}