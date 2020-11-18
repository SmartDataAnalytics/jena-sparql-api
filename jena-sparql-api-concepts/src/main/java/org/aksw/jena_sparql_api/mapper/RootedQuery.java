package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * A wrapper for a partitioned query where a single node of
 * its template is designated as a root node.
 *
 * This enables construction of {@link Resource} instances even from partitions based on
 * multiple partition variables.
 *
 * For example, facet value counts can be conceptually expressed
 * in the following extended SPARQL syntax:
 *
 * <pre>
 * {@code
 *   CONSTRUCT { _:b :facet ?f; :facetValue ?fv ; :facetValueCount ?fvc }
 *     { SELECT ?f ?fv (COUNT(DISTINCT *) AS ?fvc) { ... } GROUP BY ?f ?fv}
 *   PARTITION BY ?f ?fv
 *   ROOT _:b
 * }
 * </pre>
 *
 * Evaluation of partitioned queries remaps blank nodes of the template only ONCE per partition.
 * In this example, for each distinct binding of ?f and ?fv the blank node _:b will be mapped to
 * a single fresh blank node for each partition.
 *
 *
 * As a consequence, in the example, the computed count ONLY applies to a certain binding of <i>both</i> ?f and ?fv.
 * There does not exist a resource the count can be attached to as a property, though one
 * can be allocated using:
 * <pre>
 *   BIND(CONCAT('http://...', STR(?f), STR(?vf)) AS ?grouped)
 * </pre>
 * However, this allocation is usually not very useful:
 * <ul>
 *   <li>Query rewriting that filters on ?grouped is inefficient as it can not exploit any index</li>
 *   <li>As ?grouped is derived from ?f and ?fv it duplicates their information which consequently needlessly
 *   increases the result set size and thus lowers performance of transmitting it.</li>
 * </ul>
 *
 *
 *
 * @author raven
 *
 */
public interface RootedQuery {
    /**
     * The designated root node. The following rules apply:
     * <ul>
     *   <li>If {@code getObjectQuery().getTemplate()} is non-empty
     *       then the root node should be mentioned in the template.</li>
     *
     *   <li>In case the template is empty then the root node should be a variable
     *       that occurs in {@code getObjectQuery().getRelation().getElement()}.
     *       Furthermore, if in this case the query is partitioned by a single variable then the root
     *       node should match that one.</li>
     * </ul>
     *
     * @return
     */
    Node getRootNode();
    // PartitionedQuery getPartitionedQuery();
    ObjectQuery getObjectQuery();
}
