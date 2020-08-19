package org.aksw.jena_sparql_api.sparql_path2;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.path.Path;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class PathStore {

}



/**
 * A set of paths reaching a certain node
 *
 * @author raven
 *
 */
class PathSet<V> {
    protected Node node;
    protected V vertex;
    protected Nfa<V, LabeledEdge<V, Path>> nfa;
    protected Map<V, Multimap<Node, TriplePath>> reaches;

    protected void getPaths(V vertex, Node node) {
        // Check by which predecessor states the current node was reached
        //nfa.getGraph()
    }
}


interface ReachabilityStore<V> {
    /**
     *
     * @param triplePath The object is the reached node
     * @param targetState In which state the node was encountered
     * @param sourceState Prior state of the transition
     */
    public void add(TriplePath triplePath, V targetState, V sourceState);
}


class ReachabilityStoreMem<V>
    implements ReachabilityStore<V>
{
    protected Map<V, Multimap<Node, TriplePath>> reaches = new HashMap<>();


    protected Multimap<Node, TriplePath> getOrCreateStateInfo(V state) {
        Multimap<Node, TriplePath> result = reaches.get(state);
        if(result == null) {
            result = HashMultimap.create();
            reaches.put(state, result);
        }

        return result;
    }

    @Override
    public void add(TriplePath triplePath, V targetState, V sourceState) {
        Multimap<Node, TriplePath> nodeToPath = getOrCreateStateInfo(targetState);

        Node node = triplePath.getObject();
        nodeToPath.put(node, triplePath);
    }


    /**
     * List paths
     * @return
     */
//    Set<Object> getPaths(Node targetNode) {
//
//    }

}
