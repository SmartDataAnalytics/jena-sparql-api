package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.List;

import org.apache.jena.graph.Node;



/**
 * Data provider where ingoing and outgoing predicates are at the same
 * level of nesting:
 *
 * The schema of a triple-based resource traversal path is comprised of the following constituents:
 *
 * ${value}/${direction}/${property}/${alias}/${value}/...
 *   Bob   /    fwd     /   job     /   x    / Artist
 *
 *
 * For quads, a graph component is included:
 *
 * ${value}/${direction}/${property}/${alias}/${graph}/${value}/...
 *   Bob   /    fwd     /   job     /   x    /  old   / Artist
 *   Bob   /    fwd     /   job     /   x    /  new   / Architect
 *
 * Interpretation: Followng the predicate 'job' in forward direction starting from 'Bob'
 * leads to a set of graphs, each of which contains a certain set of values.
 *
 *
 *
 *
 *
 *
 * One can navigate from a value (a Node) to the set of related nodes
 *
 * The schema for a property traversal path is:
 *
 * {direction}/${property}/${alias}/${direction}/...
 *
 *
 * ${value} and {$property} are generally an identifier for a <b>set</b> of resources.
 * However, it is common to use concrete identifiers to denote singleton sets containing the identifier itself
 * For example to the set labelled dbo:Leipzig may contain dbo:Leipzig as its only member.
 *
 *
 *
 *
 * Example:
 * dataProvider.fetchChildren(hierarchicalQuery("root")) yields
 *   outgoing/predicate/
 *
 *
 * @author raven
 *
 */
public class HierarchicalDataProviderRdfFlat {
    public enum SegmentType {
        VALUE,
        DIRECTION,
        PROPERTY,
        ALIAS
    }


    /**
     * Determine which kind of data provider needs to be created for the given
     * path
     *
     * @param segments
     */
    protected void interpretAbsolutePath(List<Node> segments) {

        int n = segments.size();

        SegmentType currentSegmentType = SegmentType.VALUE;
        for (int i = 0; i < n; ++i) {
            Node segment = segments.get(i);

            switch (currentSegmentType) {
            case VALUE:

            case DIRECTION:
            case PROPERTY:
            case ALIAS:

            }

        }




    }
}
