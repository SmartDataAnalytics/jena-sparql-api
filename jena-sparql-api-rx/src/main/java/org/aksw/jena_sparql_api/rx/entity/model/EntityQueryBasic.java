package org.aksw.jena_sparql_api.rx.entity.model;

import java.util.List;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.EntityGraphFragment;
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
public class EntityQueryBasic {

//    protected EntityTemplate entityTemplate;
//    protected Query partitionSelector;
//    protected List<Var> partitionVars;
//    protected List<SortCondition> sortConditions;

    protected EntityBaseQuery baseQuery;

    protected EntityGraphFragment attributeFragment;
    protected EntityGraphFragment optionalAttributeFragment;

    public EntityBaseQuery getBaseQuery() {
        return baseQuery;
    }

    public void setBaseQuery(EntityBaseQuery baseQuery) {
        this.baseQuery = baseQuery;
    }

    public EntityGraphFragment getAttributeFragment() {
        return attributeFragment;
    }

    public void setAttributeFragment(EntityGraphFragment attributeFragment) {
        this.attributeFragment = attributeFragment;
    }

    public void setOptionalAttributeFragment(EntityGraphFragment optionalAttributeFragment) {
        this.optionalAttributeFragment = optionalAttributeFragment;
    }

    public EntityGraphFragment getOptionalAttributeFragment() {
        return optionalAttributeFragment;
    }

    @Override
    public String toString() {
        return baseQuery + "\n" + attributeFragment + "\n" + optionalAttributeFragment;
    }

    /**
     * Return the underlying SPARQL 1.1 query for which
     * the extensions in this model are defined
     *
     * @return
     */
//    public EntityTemplate getDirectGraphPartition() {
//        return entityTemplate;
//    }
//
//    public void setDirectGraphPartition(EntityTemplate entityTemplate) {
//        this.entityTemplate = entityTemplate;
//    }
//
//    /**
//     * The select query that specifies the set of entities,
//     * and their order
//     *
//     * The number and order of the projection variables must
//     * match that of the graph partitions and vice versa
//     *
//     * @return
//     */
//    public Query getPartitionSelectorQuery() {
//        return partitionSelector;
//    }
//
//    void setPartitionSelectorQuery(Query query) {
//        this.partitionSelector = query;
//    }
//
//
//    public EntityGraphFragment getAttributeFragment() {
//        return attributeFragment;
//    }
//
//    public void setAttributeFragment(EntityGraphFragment attributeFragment) {
//        this.attributeFragment = attributeFragment;
//    }
//
//    /**
//     * The variables of the base select query by which to partition
//     *
//     * @return
//     */
//    List<Var> getPartitionVars() {
//        return this.partitionVars;
//    }
//
//    List<SortCondition> getPartitionOrderBy() {
//        return this.sortConditions;
//    }

}
