package org.aksw.jena_sparql_api.sparql_path.core.algorithm;

import java.util.Comparator;

import org.jgrapht.GraphPath;

public class GraphPathComparator<V, E>
    implements Comparator<GraphPath<V, E>> {

    @Override
    public int compare(
            GraphPath<V, E> a,
            GraphPath<V, E> b) {
        int x = a.getEdgeList().size();
        int y = b.getEdgeList().size();

        return x - y;
    }

}