package org.aksw.jena_sparql_api.rx;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;


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
public interface EntityQuery {
    /**
     * Return the underlying SPARQL 1.1 query for which
     * the extensions in this model are defined
     *
     * @return
     */
//    List<GraphPartition> getGraphPartitions();

    GraphPartitionBase getDirectGraphPartition();

    /**
     * The select query that specifies the set of entities,
     * and their order
     *
     * The number and order of the projection variables must
     * match that of the graph partitions and vice versa
     *
     * @return
     */
    Query getPartitionSelectorQuery();
    void setPartitionSelectorQuery(Query query);


    /**
     * The variables of the base select query by which to partition
     *
     * @return
     */
    List<Var> getPartitionVars();
    List<SortCondition> getPartitionOrderBy();
}
