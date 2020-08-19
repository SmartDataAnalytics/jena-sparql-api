package org.aksw.jena_sparql_api.rx;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;

/**
 * Graph accumulation of bindings via a template.
 * Template blank nodes are remapped and the mapping is kept in the bnodeMap.
 *
 * @author raven
 *
 */
public class AccGraph// implements Acc<Graph> {
{
    protected Graph graph;
    protected Template template;
    protected Map<Node, Node> bnodeMap;

    public AccGraph(Template template) {
        this(template, GraphFactory.createDefaultGraph(), new HashMap<>());
    }

    public AccGraph(Template template, Graph graph, Map<Node,Node> bnodeMap) {
        super();
        this.template = Objects.requireNonNull(template);
        this.graph = Objects.requireNonNull(graph);
        this.bnodeMap = bnodeMap;
    }

    public void accumulate(Binding binding) {
        for(Triple t : template.getTriples()) {
            Triple newT = TemplateLib.subst(t, binding, bnodeMap);
            if(newT.isConcrete()) {
                graph.add(newT);
            }
        }
    }

    public Map<Node, Node> getBnodeMap() {
        return bnodeMap;
    }

    // @Override
    public Graph getValue() {
        return graph;
    }
}
