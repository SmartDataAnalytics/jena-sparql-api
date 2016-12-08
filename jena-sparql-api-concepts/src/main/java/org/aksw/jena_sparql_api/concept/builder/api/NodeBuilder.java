package org.aksw.jena_sparql_api.concept.builder.api;

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public interface NodeBuilder {

    NodeBuilder getParent();

    //TreeBuilder out(Path path);
    TreeBuilder out(Node node);

    //TreeBuilder in(Path path);
    TreeBuilder in(Node node);

    // ref
    //TreeBuilder attachOut(QueryBuilder queryBuilder);

    Set<Expr> getExprs();
    NodeBuilder addExpr(Expr expr); // add an expression over this sub-tree

    // set an alias for this node
    NodeBuilder as(Var var);

    /**
     * List all node builders having the same alias as this one
     * @return
     */
    List<NodeBuilder> getOverlaps();

    default NodeBuilder getRoot() {
        NodeBuilder parent = getParent();
        NodeBuilder result = parent == null
                ? this
                : parent.getParent();
        return result;
    }

    default TreeBuilder out(Resource r) {
        return out(r.asNode());
    }

    default TreeBuilder in(Resource r) {
        return in(r.asNode());
    }
}
