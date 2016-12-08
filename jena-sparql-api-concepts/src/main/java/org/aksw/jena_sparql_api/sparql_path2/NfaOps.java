package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Collections;

import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;

import com.google.common.collect.FluentIterable;

//https://swtch.com/~rsc/regexp/regexp1.html
public class NfaOps {

    /**
     * e1 e2
     *
     * @param graph
     * @param a
     * @param b
     * @return
     */
    public static <V, E, T> PartialNfa<V, T> concatenate(DirectedGraph<V, E> graph, PartialNfa<V, T> a, PartialNfa<V, T> b, EdgeLabelAccessor<E, T> edgeLabelAccessor) {
        V target = b.getStartVertex();
        for(HalfEdge<V, T> looseEnd : a.getLooseEnds()) {
            V start = looseEnd.getStartVertex();
            T edgeLabel = looseEnd.getEdgeLabel();

            E edge = graph.addEdge(start, target);
            edgeLabelAccessor.setLabel(edge, edgeLabel);
            //E edge = edgeFactory.createEdge(start, target, edgeLabel);
            //graph.addEdge(start, target, edge);
        }

        PartialNfa<V, T> result = PartialNfa.create(a.getStartVertex(), b.getLooseEnds());
        return result;
    }

    /**
     * e1 | e2
     *
     * @param graph
     * @param a
     * @param b
     * @return
     */
    public static <V, E, T> PartialNfa<V, T> alternate(DirectedGraph<V, E> graph, VertexFactory<V> vertexFactory, PartialNfa<V, T> a, PartialNfa<V, T> b) {
        V newStartVertex = vertexFactory.createVertex();
        graph.addVertex(newStartVertex);

        graph.addEdge(newStartVertex, a.getStartVertex());
        graph.addEdge(newStartVertex, b.getStartVertex());
        Iterable<HalfEdge<V, T>> newLooseEnds = FluentIterable
                .from(a.getLooseEnds())
                .append(b.getLooseEnds())
                .toList();

        PartialNfa<V, T> result = PartialNfa.create(newStartVertex, newLooseEnds);
        return result;
    }

    /**
     * e?
     *
     * @param graph
     * @param vertexFactory
     * @param a
     * @return
     */
    public static <V, E, T> PartialNfa<V, T> zeroOrOne(DirectedGraph<V, E> graph, VertexFactory<V> vertexFactory, PartialNfa<V, T> a) {
        V newStartVertex = vertexFactory.createVertex();
        graph.addVertex(newStartVertex);

        V oldStartVertex = a.getStartVertex();
        graph.addEdge(newStartVertex, oldStartVertex);
        Iterable<HalfEdge<V, T>> newLooseEnds = FluentIterable
                .from(a.getLooseEnds())
                .append(Collections.singletonList(new HalfEdge<V, T>(newStartVertex, null)))
                .toList();

        PartialNfa<V, T> result = PartialNfa.create(newStartVertex, newLooseEnds);
        return result;

    }

    /**
     * e*
     *
     * @param graph
     * @param a
     * @return
     */
    public static <V, E, T> PartialNfa<V, T> zeroOrMore(DirectedGraph<V, E> graph, VertexFactory<V> vertexFactory, PartialNfa<V, T> a, EdgeLabelAccessor<E, T> edgeLabelAccessor) {
        V newStartVertex = vertexFactory.createVertex();
        graph.addVertex(newStartVertex);

        V oldStartVertex = a.getStartVertex();
        graph.addEdge(newStartVertex, oldStartVertex);

        for(HalfEdge<V, T> looseEnd : a.getLooseEnds()) {
            E edge = graph.addEdge(looseEnd.getStartVertex(), newStartVertex);
            edgeLabelAccessor.setLabel(edge, looseEnd.getEdgeLabel());
        }

        Iterable<HalfEdge<V, T>> newLooseEnds = Collections.singletonList(new HalfEdge<V, T>(newStartVertex, null));
        PartialNfa<V, T> result = PartialNfa.create(newStartVertex, newLooseEnds);
        return result;
    }

    /**
     * e+
     *
     * @param graph
     * @param vertexFactory
     * @param a
     * @return
     */
    public static <V, E, T> PartialNfa<V, T> oneOrMore(DirectedGraph<V, E> graph, VertexFactory<V> vertexFactory, PartialNfa<V, T> a, EdgeLabelAccessor<E, T> edgeLabelAccessor) {
        V tmpVertex = vertexFactory.createVertex();
        graph.addVertex(tmpVertex);

        for(HalfEdge<V, T> looseEnd : a.getLooseEnds()) {
            E edge = graph.addEdge(looseEnd.getStartVertex(), tmpVertex);
            edgeLabelAccessor.setLabel(edge, looseEnd.getEdgeLabel());
        }

        V oldStartVertex = a.getStartVertex();
        graph.addEdge(tmpVertex, oldStartVertex);

        Iterable<HalfEdge<V, T>> newLooseEnds = Collections.singletonList(new HalfEdge<V, T>(tmpVertex, null));
        PartialNfa<V, T> result = PartialNfa.create(oldStartVertex, newLooseEnds);
        return result;
    }

}