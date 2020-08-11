package org.aksw.jena_sparql_api.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;

public class AccSinkTriples<T extends Sink<Triple>>
    implements Acc<T>
{
    protected T sink;
    protected Template template;
    protected Node reverse;

    protected Map<Node, Node> bNodeMap = new HashMap<Node, Node>();


//    public AccSinkTriples(Template template) {
//       this(template, NodeValue.FALSE.asNode());
//    }

    public AccSinkTriples(T sink, Template template) {
        this(sink, template, NodeValue.FALSE.asNode());
    }
//    public AccSinkTriples(Template template, Node reverse) {
//        this(GraphFactory.createDefaultGraph(), template, reverse);
//    }

    public AccSinkTriples(T sink, Template template, Node reverse) {
        super();
        this.sink = sink;
        this.template = template;
        this.reverse = reverse;
    }

    public static boolean isTrue(Object o) {
        boolean result = Boolean.TRUE.equals(o) || (o instanceof Number && ((Number)o).intValue() == 1);
        return result;
    }

    @Override
    public void accumulate(Binding binding) {
//        Set<Triple> triples = new HashSet<Triple>();
//        template.subst(triples, bNodeMap, binding);

        Node node = reverse.isVariable()
                ? binding.get((Var)reverse)
                : reverse
                ;

        boolean doReverse = node.isLiteral()
                ? isTrue(node.getLiteralValue())
                : false
                ;


        Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Collections.singleton(binding).iterator());

        //for(Triple triple : triples) {
        while(it.hasNext()) {
            Triple triple = it.next();
            if(doReverse) {
                triple = TripleUtils.swap(triple);
            }

            // graph.add(triple);
            sink.send(triple);
        }
    }

    @Override
    public T getValue() {
        return sink;
    }

}
