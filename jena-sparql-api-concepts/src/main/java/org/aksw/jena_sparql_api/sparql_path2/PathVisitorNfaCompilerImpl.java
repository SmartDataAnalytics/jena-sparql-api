package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.jgrapht.Graph;

public class PathVisitorNfaCompilerImpl<V, E, D>
    extends PathVisitorNfaCompilerBase<V, E, D>
{
    protected Function<Path, D> primitivePathMapper;

    public PathVisitorNfaCompilerImpl(
            Graph<V, E> graph,
            Supplier<V> vertexFactory,
            EdgeLabelAccessor<E, D> edgeLabelAccessor,
            Function<Path, D> primitivePathMapper) {
        super(graph, vertexFactory, edgeLabelAccessor);
        this.primitivePathMapper = primitivePathMapper;
    }


    public void processPrimitivePath(Path path) {
        D edgeLabel = primitivePathMapper.apply(path);
        V s = vertexFactory.get();
        graph.addVertex(s);
        PartialNfa<V, D> partialNfa = PartialNfa.create(s, Collections.singletonList(new HalfEdge<V, D>(s, edgeLabel)));

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



    public Nfa<V, E> complete() {
        PartialNfa<V, D> partialNfa = this.peek();

        V finalVertex = vertexFactory.get();
        graph.addVertex(finalVertex);

        for(HalfEdge<V, D> looseEnd : partialNfa.getLooseEnds()) {
            V v = looseEnd.getStartVertex();
            D label = looseEnd.getEdgeLabel();

            E edge = graph.addEdge(v, finalVertex);
            edgeLabelAccessor.setLabel(edge, label);
        }

        Set<V> startStates = Collections.singleton(partialNfa.getStartVertex());
        Set<V> finalStates = Collections.singleton(finalVertex);

        NfaImpl<V, E> result = new NfaImpl<V, E>(graph, startStates, finalStates);
        return result;
    }

    //public Nfa()
}