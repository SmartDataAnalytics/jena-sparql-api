package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;

public class BidirectionalExprConverter {
    protected UnaryXExpr readFn;
    protected Function<Node, Collection<NodeValue>> writeFn;
}
