package org.aksw.jena_sparql_api.utils.graph;

import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphWrapperTransform
	extends GraphWrapper
{
	// TODO Add method to transform the lookup triple object
	protected Function<ExtendedIterator<Triple>, ExtendedIterator<Triple>> transform;

	public GraphWrapperTransform(Graph graph, Function<ExtendedIterator<Triple>, ExtendedIterator<Triple>> transform) {
		super(graph);
		this.transform = transform;
	}

	@Override
	public ExtendedIterator<Triple> find() {
		ExtendedIterator<Triple> raw = super.find();
		ExtendedIterator<Triple> result = transform.apply(raw);
		return result;
	}
	
	@Override
	public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
		ExtendedIterator<Triple> raw = super.find(s, p, o);
		ExtendedIterator<Triple> result = transform.apply(raw);
		return result;
	}
	
	@Override
	public ExtendedIterator<Triple> find(Triple triple) {
		ExtendedIterator<Triple> raw = super.find(triple);
		ExtendedIterator<Triple> result = transform.apply(raw);
		return result;
	}
}
