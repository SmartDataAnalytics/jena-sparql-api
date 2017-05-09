package org.aksw.jena_sparql_api.iso.index;

import java.util.Set;

import org.aksw.commons.collections.trees.LabeledNodeImpl;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphIsoMap;

/**
 *
 *
 * FIXME Materialize if nesting of views gets too deep
 * FIXME Cluster graphs by size (or maybe this is separate indices?) - we don't want to compute isomorphisms from { ?s ?p ?o } to a BGP with 100 TPs
 *
 * Materialization vs recomputation of iso-mappings
 *
 * We check whether a graph being inserted is subsumed by other graphs in regard to isomorphism.
 * Insert graph = graph being inserted
 * Node graph = graph at the current index node
 * As there can be several mappings between in insert graph and the node graph, we can always re-compute the set of iso mappings
 * based on the path to the root.
 *
 *
 *
 *
 * @author raven
 *
 */
public class GraphIndexNode<K>
    extends LabeledNodeImpl<Long, GraphIndexNode<K>, SubGraphIsomorphismIndex<K>>
{
    public GraphIndexNode(SubGraphIsomorphismIndex<K> tree, Long id) {
        super(tree, id);
    }

    public GraphIsoMap getValue() {
        return tree.idToGraph.get(id);
    }

    public GraphIsoMap setValue(GraphIsoMap graphIso) {
        return tree.idToGraph.put(id, graphIso);
    }

    public Set<K> getKeys() {
        return tree.idToKeys.get(id);
    }

//    public Set<K> setKeys() {
//        return tree.idToKeys.pu
//    }

}