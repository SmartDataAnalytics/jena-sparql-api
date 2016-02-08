package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathLib;
import org.jgrapht.DirectedGraph;
import org.jgrapht.VertexFactory;

public class PathVisitorNfaCompilerImpl<V>
    extends PathVisitorNfaCompilerBase<V, LabeledEdge<V, Path>, Path>
{
    public PathVisitorNfaCompilerImpl(
            DirectedGraph<V, LabeledEdge<V, Path>> graph,
            VertexFactory<V> vertexFactory,
            EdgeLabelAccessor<LabeledEdge<V, Path>, Path> edgeLabelAccessor) {
        super(graph, vertexFactory, edgeLabelAccessor);
    }

    public void processPrimitivePath(Path path) {
        V s = vertexFactory.createVertex();
        graph.addVertex(s);
        PartialNfa<V, Path> partialNfa = PartialNfa.create(s, Collections.singletonList(new HalfEdge<V, Path>(s, path)));

        stack.push(partialNfa);
    }

    @Override
    public void visit(P_Link path) {
        processPrimitivePath(path);
    }

    @Override
    public void visit(P_ReverseLink path) {
        processPrimitivePath(path);
    }

    @Override
    public void visit(P_NegPropSet path) {
        processPrimitivePath(path);
    }

    @Override
    public void visit(P_Inverse path) {
        processPrimitivePath(path);
    }



    public Nfa<V, LabeledEdge<V, Path>> complete() {
        PartialNfa<V, Path> partialNfa = this.peek();

        V finalVertex = vertexFactory.createVertex();
        graph.addVertex(finalVertex);

        for(HalfEdge<V, Path> looseEnd : partialNfa.getLooseEnds()) {
            V v = looseEnd.getStartVertex();
            Path label = looseEnd.getEdgeLabel();

            LabeledEdge<V, Path> edge = graph.addEdge(v, finalVertex);
            edgeLabelAccessor.setLabel(edge, label);
        }

        Set<V> startStates = Collections.singleton(partialNfa.getStartVertex());
        Set<V> finalStates = Collections.singleton(finalVertex);

        NfaImpl<V, LabeledEdge<V, Path>> result = new NfaImpl<V, LabeledEdge<V, Path>>(graph, startStates, finalStates);
        return result;
    }

    //public Nfa()
}