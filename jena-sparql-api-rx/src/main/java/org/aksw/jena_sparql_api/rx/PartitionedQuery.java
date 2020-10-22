package org.aksw.jena_sparql_api.rx;

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;


/**
 * An extension (wrapper) of Jena's SPARQL {@link Query} model with the following features:
 *
 * <ul>
 *   <li>Partitioning of result sets into sets of bindings that have common values for some of their variables.
 *       A construct template can be instantiated for each partition individually which leads to a sequence of
 *       graph fragments</li>
 *   <li>A certain amount of control over blank node allocation by making their IDs dependent on a tuple of expressions
 *       over the projected columns. Within a single query execution the blank nodes whose
 *       corresponding expression tuple yields the same value
 *       should map to the same final blank node</li>
 *   <li>Designation of a {@link Node} in the template or a variable of the WHERE pattern as the root node</li>
 * </ul>
 *
 *
 * @author raven
 *
 */
public interface PartitionedQuery {
    /**
     * Return the underlying SPARQL 1.1 query for which
     * the extensions in this model are defined
     *
     * @return
     */
    Query toStandardQuery();

    Map<Node, ExprList> getIdMapping();

    List<Var> getPartitionVars();

    Node getRootNode();

    void setRootNode(Node rootVar);

    default void setRootNode(String rootVarName) {
        setRootNode(Var.alloc(rootVarName));
    }
}
