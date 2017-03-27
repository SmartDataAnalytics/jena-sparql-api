package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Template;

public class AccGraph
    implements Acc<Graph>
{
    protected Graph graph;
    protected Template template;
    protected Node reverse;

    public AccGraph(Template template) {
       this(template, NodeValue.FALSE.asNode());
    }

    public AccGraph(Template template, Node reverse) {
        this(GraphFactory.createDefaultGraph(), template, reverse);
    }

    public AccGraph(Graph graph, Template template, Node reverse) {
        super();
        this.graph = graph;
        this.template = template;
        this.reverse = reverse;
    }

    public static boolean isTrue(Object o) {
        boolean result = Boolean.TRUE.equals(o) || (o instanceof Number && ((Number)o).intValue() == 1);
        return result;
    }

    @Override
    public void accumulate(Binding binding) {
        Set<Triple> triples = new HashSet<Triple>();
        Map<Node, Node> bNodeMap = new HashMap<Node, Node>();
        template.subst(triples, bNodeMap, binding);

        //Node TRUE = NodeValue.TRUE.asNode();

        Node node = reverse.isVariable()
                ? binding.get((Var)reverse)
                : reverse
                ;

        boolean doReverse = node.isLiteral()
                ? isTrue(node.getLiteralValue())
                : false
                ;


        for(Triple triple : triples) {
            if(doReverse) {
                triple = TripleUtils.swap(triple);
            }

            graph.add(triple);
        }
    }

    @Override
    public Graph getValue() {
        return graph;
    }

}
