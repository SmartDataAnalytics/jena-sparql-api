package org.aksw.jena_sparql_api.concept.builder.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.TreeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

//class Graph {
//    Map<Node, TreeBuilder> sToTs;
//    Map<Node, TreeBuilder> oToTs;
//
//}

public class NodeBuilderImpl
    implements NodeBuilder
{
    protected Node alias;
    protected Map<Node, TreeBuilder> out = new HashMap<>();
    protected Map<Node, TreeBuilder> in = new HashMap<>();

    protected Set<Expr> exprs = new HashSet<>();

    public NodeBuilderImpl() {
        super();
    }

    public NodeBuilderImpl(Node alias) {
        super();
        this.alias = alias;
    }



    @Override
    public List<NodeBuilder> getOverlaps() {
        throw new UnsupportedOperationException("not implemented yet");
    }

//    @Override
//    public TreeBuilder out(Path path) {
//        throw new UnsupportedOperationException("not implemented yet");
//    }

//    @Override
//    public TreeBuilder in(Path path) {
//        throw new UnsupportedOperationException("not implemented yet");
//    }


    /**
     * Gets or creates a tree builder for the given variable
     * relative to this node builder.
     *
     */
    @Override
    public TreeBuilder out(Node node) {
        TreeBuilder result = either(this, out, node, false);
        return result;
    }

    @Override
    public TreeBuilder in(Node node) {
        TreeBuilder result = either(this, out, node, false);
        return result;
    }

    public static TreeBuilder either(NodeBuilder self, Map<Node, TreeBuilder> map, Node node, boolean reverse) {
        TreeBuilder result = map.computeIfAbsent(node, key -> {
            NodeBuilder predicateNodeBuilder = new NodeBuilderImpl();
            NodeBuilder targetNodeBuilder = new NodeBuilderImpl();
            TreeBuilderImpl r = reverse
                    ? new TreeBuilderImpl(targetNodeBuilder, predicateNodeBuilder, self, true)
                    : new TreeBuilderImpl(self, predicateNodeBuilder, targetNodeBuilder, true)
                    ;

            return r;
        });

        return result;

    }

    @Override
    public Set<Expr> getExprs() {
        return exprs;
    }

    @Override
    public NodeBuilder addExpr(Expr expr) {
        exprs.add(expr);
        return this;
    }

    @Override
    public NodeBuilder as(Var var) {
        this.alias = var;
        return this;
    }

    @Override
    public NodeBuilder getParent() {
        // TODO Auto-generated method stub
        return null;
    }
}
