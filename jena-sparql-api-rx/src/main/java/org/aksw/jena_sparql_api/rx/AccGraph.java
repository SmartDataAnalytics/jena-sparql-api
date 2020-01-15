package org.aksw.jena_sparql_api.rx;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;

public class AccGraph// implements Acc<Graph> {
{
	protected Graph graph;
	protected Template template;

	public AccGraph(Template template) {
		this(template, GraphFactory.createDefaultGraph());
	}

	public AccGraph(Template template, Graph graph) {
		super();
		this.template = Objects.requireNonNull(template);
		this.graph = Objects.requireNonNull(graph);
	}

	public void accumulate(Binding binding) {
		Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(binding));
		while(it.hasNext()) {
			Triple t = it.next();
			graph.add(t);
		}
	}

	// @Override
	public Graph getValue() {
		return graph;
	}
}
