package org.aksw.jena_sparql_api.relation;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.base.Converter;

public class ConverterTripleToNode
    extends Converter<Triple, Node>
{
    // TODO Put the state into a DirectedFilteredTriplePattern?
    protected Node source;
    protected Node predicate;
    protected boolean isForward;

    public ConverterTripleToNode(Node source, Node predicate, boolean isForward) {
        super();
        this.source = source;
        this.predicate = predicate;
        this.isForward = isForward;
    }

    public Node getSource() {
        return source;
    }

    public Node getPredicate() {
        return predicate;
    }

    public boolean isForward() {
        return isForward;
    }

    @Override
    protected Node doForward(Triple a) {
        Node result = TripleUtils.getTarget(a, isForward);
        return result;
    }

    @Override
    protected Triple doBackward(Node b) {
        Triple result = TripleUtils.create(source, predicate, b, isForward);
        return result;
    }

}