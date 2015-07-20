package org.aksw.jena_sparql_api.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

public class DiffQuadUtils {

    public static Map<Node, Diff<Set<Quad>>> partitionByGraph(Diff<? extends Iterable<Quad>> diff) {
        Map<Node, Set<Quad>> added = QuadUtils.partitionByGraph(diff.getAdded());
        Map<Node, Set<Quad>> removed = QuadUtils.partitionByGraph(diff.getRemoved());

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
    }
}
