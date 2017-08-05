package org.aksw.jena_sparql_api.algebra.utils;

import java.util.function.Supplier;

import org.aksw.commons.graph.index.jena.transform.QueryToGraphVisitor;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpExt;

public class ExtendedQueryToGraphVisitor
    extends QueryToGraphVisitor
{
    public ExtendedQueryToGraphVisitor() {
        super();
    }

//    public ExtendedQueryToGraphVisitor(Graph graph, Supplier<Node> nodeSupplier) {
//        super(graph, nodeSupplier);
//    }

    public ExtendedQueryToGraphVisitor(Supplier<Node> nodeSupplier) {
        super(nodeSupplier);
    }

    @Override
    public void visit(OpExt op) {
        if(op instanceof OpExtConjunctiveQuery) {
            ((OpExtConjunctiveQuery) op).getQfpc().toOp().visit(this);
        }
    }

}