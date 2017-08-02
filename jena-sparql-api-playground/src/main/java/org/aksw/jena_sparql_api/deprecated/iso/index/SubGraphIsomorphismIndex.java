package org.aksw.jena_sparql_api.deprecated.iso.index;

import org.apache.jena.graph.Graph;

import com.google.common.collect.Multimap;

public interface SubGraphIsomorphismIndex<K> {

    void removeKey(Object key);

    /**
     * Insert the graph and allocate a fresh, unused, id
     *
     * @param graph
     */
    K add(Graph graph);

    /**
     * For each key, returns sets of objects each comprising:
     * - the index node, holding the residual index graph
     * - the isomorphism from the residual index graph to the residual query graph
     * - the residual query graph
     *
     *
     *
     * @param queryGraph
     * @return
     */
    Multimap<K, InsertPositionOld<K>> lookup(Graph queryGraph, boolean exactMatch);
    //
    //    public Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> lookupStream(Graph queryGraph, boolean exactMatch) {
    //        Multimap<K, InsertPositionOld<K>> matches = lookup(queryGraph, exactMatch);
    //
    //        Map<K, ProblemNeighborhoodAware<BiMap<Var, Var>, Var>> result = matches.asMap().entrySet().stream().collect(Collectors.toMap(
    //            e -> e.getKey(),
    //            e -> SparqlViewMatcherQfpcIso.createCompound(e.getValue())));
    //
    //
    //        return result;
    //    }

    /**
     * Insert a graph pattern with a specific key, thereby replacing any existing one having this key already
     *
     * @param key
     * @param graph
     * @return
     */
    K put(K key, Graph graph);

}