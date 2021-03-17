package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.jena.jgrapht.LabeledEdgeImpl;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.DefaultGraphType;

import com.google.common.base.Objects;

/**
 * Unfinished class
 * Issue: With this graph representation, expressions are not converted into nodes
 * which is what we actually want for our purposes of containment checking
 *
 *
 * @author raven
 *
 * @param <T>
 * @param <E>
 */
public class DirectedGraphTree<T, E>
    implements Graph<T, LabeledEdge<T, E>>
{
    protected Tree<T> tree;

    @Override
    public Set<LabeledEdge<T, E>> getAllEdges(T sourceVertex, T targetVertex) {
        LabeledEdge<T, E> tmp = getEdge(sourceVertex, targetVertex);

        Set<LabeledEdge<T, E>> result = tmp == null
                ? Collections.emptySet()
                : Collections.singleton(tmp)
                ;

        return result;
    }

    @Override
    public LabeledEdge<T, E> getEdge(T sourceVertex, T targetVertex) {
        LabeledEdge<T, E> result = Objects.equal(sourceVertex, targetVertex)
                ? new LabeledEdgeImpl<>(sourceVertex, targetVertex, null)
                : null;

        return result;
    }

//    @Override
//    public EdgeFactory<T, LabeledEdge<T, E>> getEdgeFactory() {
//        return null;
//    }

    @Override
    public boolean containsEdge(T sourceVertex, T targetVertex) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsEdge(LabeledEdge<T, E> e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsVertex(T v) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Implementation of edgeSet based on vertexSet() and edgesOf(T vertex)
     *
     */
    @Override
    public Set<LabeledEdge<T, E>> edgeSet() {
        Set<LabeledEdge<T, E>> result = vertexSet().stream()
            .map(tree::getChildren)
            .flatMap(Collection::stream)
            .map(this::edgesOf)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        return result;
    }

    @Override
    public Set<LabeledEdge<T, E>> edgesOf(T vertex) {
        //List<T> children = tree.getChildren(vertex);
        return null;
    }

    @Override
    public Set<T> vertexSet() {
        Set<T> result = TreeUtils.inOrderSearch(tree.getRoot(), tree::getChildren).collect(Collectors.toCollection(Sets::newIdentityHashSet));
        return result;
    }

    @Override
    public T getEdgeSource(LabeledEdge<T, E> e) {
        T result = e.getSource();
        return result;
    }

    @Override
    public T getEdgeTarget(LabeledEdge<T, E> e) {
        T result = e.getTarget();
        return result;
    }

    @Override
    public double getEdgeWeight(LabeledEdge<T, E> e) {
        //T result =
        return 1.0;
    }

    @Override
    public int inDegreeOf(T vertex) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<LabeledEdge<T, E>> incomingEdgesOf(T vertex) {


        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int outDegreeOf(T vertex) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Set<LabeledEdge<T, E>> outgoingEdgesOf(T vertex) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public LabeledEdge<T, E> addEdge(T sourceVertex, T targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(T sourceVertex, T targetVertex, LabeledEdge<T, E> e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addVertex(T v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllEdges(Collection<? extends LabeledEdge<T, E>> edges) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<LabeledEdge<T, E>> removeAllEdges(T sourceVertex, T targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllVertices(Collection<? extends T> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LabeledEdge<T, E> removeEdge(T sourceVertex, T targetVertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(LabeledEdge<T, E> e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeVertex(T v) {
        throw new UnsupportedOperationException();
    }

	@Override
	public int degreeOf(T vertex) {
		return inDegreeOf(vertex) + outDegreeOf(vertex);
	}

	@Override
	public GraphType getType() {
		return DefaultGraphType.directedSimple();
	}

	@Override
	public void setEdgeWeight(LabeledEdge<T, E> e, double weight) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Supplier<T> getVertexSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<LabeledEdge<T, E>> getEdgeSupplier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T addVertex() {
		// TODO Auto-generated method stub
		return null;
	}

}
