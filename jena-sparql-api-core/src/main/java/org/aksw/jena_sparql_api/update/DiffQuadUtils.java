package org.aksw.jena_sparql_api.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class DiffQuadUtils {


    public static <K, V> Map<K, Diff<Set<V>>> mergeDiff(Map<K, Set<V>> added, Map<K, Set<V>> removed) {
        Set<K> graphs = new HashSet<K>();
        graphs.addAll(added.keySet());
        graphs.addAll(removed.keySet());

        Map<K, Diff<Set<V>>> result = new HashMap<K, Diff<Set<V>>>();
        for(K graph : graphs) {
            Set<V> sa = added.get(graph);
            Set<V> sr = removed.get(graph);

            sa = sa == null ? new HashSet<V>() : sa;
            sr = sr == null ? new HashSet<V>() : sr;

            Diff<Set<V>> item = Diff.create(sa, sr);
            result.put(graph, item);
        }

        return result;
    }


    public static Map<Node, Diff<Set<Triple>>> partitionQuads(Diff<? extends Iterable<Quad>> diff) {
        Map<Node, Set<Triple>> added = QuadUtils.partitionByGraphTriples(diff.getAdded());
        Map<Node, Set<Triple>> removed = QuadUtils.partitionByGraphTriples(diff.getRemoved());

        Map<Node, Diff<Set<Triple>>> result = mergeDiff(added, removed);
        return result;
    }

    public static Map<Node, Diff<Set<Quad>>> partitionQuadsByGraph(Diff<? extends Iterable<Quad>> diff) {
        Map<Node, Set<Quad>> added = QuadUtils.partitionByGraph(diff.getAdded());
        Map<Node, Set<Quad>> removed = QuadUtils.partitionByGraph(diff.getRemoved());

        Map<Node, Diff<Set<Quad>>> result = mergeDiff(added, removed);
        return result;

        /*
        Set<Node> graphs = new HashSet<Node>();
        graphs.addAll(added.keySet());
        graphs.addAll(removed.keySet());

        Map<Node, Diff<Set<Quad>>> result = new HashMap<Node, Diff<Set<Quad>>>();
        for(Node graph : graphs) {
            Set<Quad> sa = added.get(graph);
            Set<Quad> sr = removed.get(graph);

            sa = sa == null ? new HashSet<Quad>() : sa;
            sr = sr == null ? new HashSet<Quad>() : sr;

            Diff<Set<Quad>> item = Diff.create(sa, sr);
            result.put(graph, item);
        }

        return result;
        */
    }
}
