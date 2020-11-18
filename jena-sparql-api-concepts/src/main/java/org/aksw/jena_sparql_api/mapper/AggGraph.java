package org.aksw.jena_sparql_api.mapper;

import java.util.Collections;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;

public class AggGraph
    implements Agg<Graph>
{
    protected Template template;
    protected Node reverse; // Whether to reverse the direction of all triples in the template. Can be a variable to be bound from bindings.

    public AggGraph(Template template) {
        this(template, NodeValue.FALSE.asNode());
    }

    public AggGraph(Template template, Node reverse) {
        super();
        this.template = template;
        this.reverse = reverse;
    }

    @Override
    public AccGraph createAccumulator() {
        AccGraph result = new AccGraph(template, reverse);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        BasicPattern bgp = template.getBGP();
        Set<Var> result = SetUtils.asSet(PatternVars.vars(new ElementTriplesBlock(bgp)));

        if(reverse.isVariable()) {
            result = Sets.union(Collections.singleton((Var)reverse), result);
        }

        return result;
    }

    public static AggGraph create(Template template) {
        AggGraph result = new AggGraph(template);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((template == null) ? 0 : template.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AggGraph other = (AggGraph) obj;
        if (template == null) {
            if (other.template != null)
                return false;
        } else if (!template.equals(other.template))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AggGraph [template=" + template + "]";
    }
}
