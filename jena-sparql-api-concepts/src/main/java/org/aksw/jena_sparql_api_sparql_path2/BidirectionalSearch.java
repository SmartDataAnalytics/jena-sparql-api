package org.aksw.jena_sparql_api_sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class BidirectionalSearch<V> {
    protected NfaExecution<V> forwards;
    protected NfaExecution<V> backwards;

    public BidirectionalSearch(NfaExecution<V> forwards, NfaExecution<V> backwards) {
        this.forwards = forwards;
        this.backwards = backwards;
    }


    public static <V> Set<RdfPath> intersect(Frontier<V> fwd, Frontier<V> bwd) {
        Set<RdfPath> result = new HashSet<>();

        // Get the sets of states where the frontiers meet
        Set<V> fwdStates = fwd.getCurrentStates();
        Set<V> bwdStates = bwd.getCurrentStates();
        Set<V> commonStates = Sets.intersection(fwdStates, bwdStates);

        for(V state : commonStates) {
            Multimap<Node, NestedRdfPath> fwdNodeToPaths = fwd.getPaths(state);
            Multimap<Node, NestedRdfPath> bwdNodeToPaths = bwd.getPaths(state);

            Set<Node> fwdNodes = fwdNodeToPaths.keySet();
            Set<Node> bwdNodes = bwdNodeToPaths.keySet();
            Set<Node> commonNodes = Sets.union(fwdNodes, bwdNodes);

            for(Node node : commonNodes) {
                Collection<NestedRdfPath> fwdPaths = fwdNodeToPaths.get(node);
                Collection<NestedRdfPath> bwdPaths = bwdNodeToPaths.get(node);
                for(NestedRdfPath fwdPath : fwdPaths) {
                    for(NestedRdfPath bwdPath : bwdPaths) {
                        RdfPath fwdPart = fwdPath.asSimplePath();
                        RdfPath bwdPart = bwdPath.asSimplePath().reverse();

                        Node start = fwdPart.getStart();
                        Node end = fwdPart.getEnd();
                        List<Triple> triples = new ArrayList<>();
                        triples.addAll(fwdPart.getTriples());
                        triples.addAll(bwdPart.getTriples());

                        RdfPath path = new RdfPath(start, end, triples);
                        result.add(path);
                    }
                }
            }
        }

        return result;
    }
}

