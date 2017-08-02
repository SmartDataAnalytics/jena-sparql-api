package org.aksw.jena_sparql_api.iso.index;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphUnion;

import com.google.common.base.MoreObjects;

public abstract class SetOpsJGraphTBase<V, E, G extends Graph<V, E>>
    implements SetOps<G, V>
{
    protected abstract E transformEdge(E edge, Function<V, V> nodeTransform);

    @Override
    public G transformItems(G a, Function<V, V> nodeTransform) {
        G result = createNew();
        SetOpsJGraphTBase.transformItems(result, a, nodeTransform, this::transformEdge);
        return result;
    }

    @Override
    public G union(G a, G b) {
        Graph<V, E> tmp = new GraphUnion<V, E, G>(a, b);
        G result = createNew();
        Graphs.addGraph(result, tmp);

        return result;
    }


//	@Override
//	public DirectedGraph<Node, Triple> applyIso(DirectedGraph<Node, Triple> a, BiMap<Node, Node> itemTransform) {
//		G result = transformItems(graph, iso::get);
//		return result;
//	}


// JGraphT comes with an intersect view of graphs
//    @Override
//    public G intersect(G a, G b) {
//    	SetOpsJGraphTBase.intersection(a, b);
//    }

    @Override
    public G difference(G baseGraph, G removalGraph) {
        G result = createNew();
        SetOpsJGraphTBase.difference(result, baseGraph, removalGraph);
        return result;
    }

    @Override
    public int size(G g) {
        int result = g.edgeSet().size();
        return result;
    }


    public static <V, E, T extends Graph<V, E>> T difference(T result, Graph<V, E> baseSet, Graph<V, E> removalSet) {

        baseSet.edgeSet().forEach(e -> {
            boolean skip = removalSet.containsEdge(e);
            if(!skip) {
                V srcVertex = baseSet.getEdgeSource(e);
                V tgtVertex = baseSet.getEdgeTarget(e);
                result.addVertex(srcVertex);
                result.addVertex(tgtVertex);
                result.addEdge(srcVertex, tgtVertex, e);
            }
        });

//    	Graphs.addGraph(result, baseSet);
//
//        //Graphs.unio
//        result.removeAllEdges(removalSet.edgeSet());
//        baseSet.vertexSet().forEach(v -> {
//            if(baseSet.edgesOf(v).isEmpty()) {
//                result.removeVertex(v);
//            }
//        });
//
        return result;
    }

//    public static <V, E> DirectedGraph<V, E> intersection(DirectedGraph<V, E> baseSet, DirectedGraph<V, E> removalSet) {
//        DirectedGraph<V, E> result = new DirectedSubgraph<>(baseSet, removalSet.vertexSet(), removalSet.edgeSet());
//        return result;
//    }


    public static <V, E, G extends Graph<V, E>> G transformItems(G result, G set, Function<V, V> nodeTransform, BiFunction<E, Function<V, V>, E> edgeTransform) {
        set.vertexSet().stream()
            .map(item -> MoreObjects.firstNonNull(nodeTransform.apply(item), item))
            .forEach(result::addVertex);

        set.edgeSet().stream().forEach(e -> {
            V src = set.getEdgeSource(e);
            V tgt = set.getEdgeTarget(e);

            V tmpSrc = nodeTransform.apply(src);
            V tmpTgt = nodeTransform.apply(tgt);

            V isoSrc = tmpSrc != null ? tmpSrc : src;
            V isoTgt = tmpTgt != null ? tmpTgt : tgt;

            E isoEdge = edgeTransform.apply(e, nodeTransform);

            result.addEdge(isoSrc, isoTgt, isoEdge);
        });

        return result;
    }


}
