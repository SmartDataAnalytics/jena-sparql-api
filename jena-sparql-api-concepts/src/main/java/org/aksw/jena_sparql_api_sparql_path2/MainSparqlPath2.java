package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collections;
import java.util.Stack;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.FluentIterable;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.path.PathVisitor;

class HalfEdge<V, E> {
    protected V startVertex;
    protected E edgeLabel;

    public HalfEdge(V startVertex, E edgeLabel) {
        super();
        this.startVertex = startVertex;
        this.edgeLabel = edgeLabel;
    }
    public V getStartVertex() {
        return startVertex;
    }
    public E getEdgeLabel() {
        return edgeLabel;
    }

    public static <V, E> HalfEdge<V, E> create(V startVertex, E edgeValue) {
        HalfEdge<V, E> result = new HalfEdge<V, E>(startVertex, edgeValue);
        return result;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((edgeLabel == null) ? 0 : edgeLabel.hashCode());
        result = prime * result
                + ((startVertex == null) ? 0 : startVertex.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HalfEdge other = (HalfEdge) obj;
        if (edgeLabel == null) {
            if (other.edgeLabel != null)
                return false;
        } else if (!edgeLabel.equals(other.edgeLabel))
            return false;
        if (startVertex == null) {
            if (other.startVertex != null)
                return false;
        } else if (!startVertex.equals(other.startVertex))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "HalfEdge [startVertex=" + startVertex + ", edgeLabel="
                + edgeLabel + "]";
    }
}

class PartialNfa<V, T> {
    //protected DirectedGraph<V, P_Link> graph;

    protected V startVertex;
    protected Iterable<HalfEdge<V, T>> looseEnds;

    public PartialNfa(V startVertex,
            Iterable<HalfEdge<V, T>> looseEnds) {
        super();
        this.startVertex = startVertex;
        this.looseEnds = looseEnds;
    }

    public V getStartVertex() {
        return startVertex;
    }

    public Iterable<HalfEdge<V, T>> getLooseEnds() {
        return looseEnds;
    }

    public static <V, T> PartialNfa<V, T> create(V startState, Iterable<HalfEdge<V, T>> looseEnds) {
        PartialNfa<V, T> result = new PartialNfa<V, T>(startState, looseEnds);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((looseEnds == null) ? 0 : looseEnds.hashCode());
        result = prime * result
                + ((startVertex == null) ? 0 : startVertex.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PartialNfa other = (PartialNfa) obj;
        if (looseEnds == null) {
            if (other.looseEnds != null)
                return false;
        } else if (!looseEnds.equals(other.looseEnds))
            return false;
        if (startVertex == null) {
            if (other.startVertex != null)
                return false;
        } else if (!startVertex.equals(other.startVertex))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PartialNfa [startVertex=" + startVertex + ", looseEnds="
                + looseEnds + "]";
    }
}

class NfaOps {

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

        V newStartVertex = a.getStartVertex();
        graph.addEdge(tmpVertex, newStartVertex, null);

        Iterable<HalfEdge<V, T>> newLooseEnds = Collections.singletonList(new HalfEdge<V, T>(tmpVertex, null));
        PartialNfa<V, T> result = PartialNfa.create(newStartVertex, newLooseEnds);
        return result;
    }

}

class PathVisitorNfaCompilerImpl<V>
    extends PathVisitorNfaCompilerBase<V, LabeledEdge<V, P_Link>, P_Link>
{
    public PathVisitorNfaCompilerImpl(
            DirectedGraph<V, LabeledEdge<V, P_Link>> graph,
            VertexFactory<V> vertexFactory,
            EdgeLabelAccessor<LabeledEdge<V, P_Link>, P_Link> edgeLabelAccessor) {
        super(graph, vertexFactory, edgeLabelAccessor);
    }

    @Override
    public void visit(P_Link path) {
        V s = vertexFactory.createVertex();
        graph.addVertex(s);
        PartialNfa<V, P_Link> partialNfa = PartialNfa.create(s, Collections.singletonList(new HalfEdge<V, P_Link>(s, path)));

        stack.push(partialNfa);
    }
}

abstract class PathVisitorNfaCompilerBase<V, E, T>
    implements PathVisitor
{
    protected DirectedGraph<V, E> graph;
    protected VertexFactory<V> vertexFactory;
    protected EdgeLabelAccessor<E, T> edgeLabelAccessor;

    protected Stack<PartialNfa<V, T>> stack;

    public PartialNfa<V, T> peek() {
        PartialNfa<V, T> result = stack.peek();
        return result;
    }

    public PathVisitorNfaCompilerBase(DirectedGraph<V, E> graph,
            VertexFactory<V> vertexFactory,
            EdgeLabelAccessor<E, T> edgeLabelAccessor) {
        this(graph, vertexFactory, edgeLabelAccessor, new Stack<PartialNfa<V, T>>());
    }

    public PathVisitorNfaCompilerBase(DirectedGraph<V, E> graph,
            VertexFactory<V> vertexFactory, EdgeLabelAccessor<E, T> edgeLabelAccessor, Stack<PartialNfa<V, T>> stack) {
        super();
        this.graph = graph;
        this.vertexFactory = vertexFactory;
        this.edgeLabelAccessor = edgeLabelAccessor;
        this.stack = stack;
    }


    PartialNfa<V, T> process(Path path) {
        path.visit(this);
        PartialNfa<V, T> result = stack.pop();
        return result;
    }


    @Override
    public void visit(P_Seq path) {
        PartialNfa<V, T> left = process(path.getLeft());
        PartialNfa<V, T> right = process(path.getRight());

        PartialNfa<V, T> next = NfaOps.concatenate(graph, left, right, edgeLabelAccessor);
        stack.push(next);
    }

    @Override
    public void visit(P_ReverseLink arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_NegPropSet path) {
        //path.getBwdNodes()

        // TODO Convert the current NFA to a DFA
        // we might need an explicit dead state and transitions to it

        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Inverse arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Mod arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_FixedLength arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Distinct arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Multi arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Shortest arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_ZeroOrOne arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        PartialNfa<V, T> sub = process(path.getSubPath());

        PartialNfa<V, T> next = NfaOps.zeroOrMore(graph, vertexFactory, sub, edgeLabelAccessor);
        stack.push(next);
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_OneOrMore1 arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_OneOrMoreN arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(P_Alt path) {
        PartialNfa<V, T> left = process(path.getLeft());
        PartialNfa<V, T> right = process(path.getRight());

        PartialNfa<V, T> next = NfaOps.alternate(graph, vertexFactory, left, right);
        stack.push(next);
    }


}


class VertexFactoryInteger
    implements VertexFactory<Integer>
{
    protected Graph<Integer, ?> graph;
    int nextId;


    public VertexFactoryInteger(Graph<Integer, ?> graph) {
        this(graph, 0);
    }

    public VertexFactoryInteger(Graph<Integer, ?> graph, int nextId) {
        super();
        this.graph = graph;
        this.nextId = nextId;
    }



    @Override
    public Integer createVertex() {

        while(graph.containsVertex(nextId)) {
            ++nextId;
        }

        //graph.addVertex(nextId);
        return nextId;
    }

}

interface LabeledEdge<V, T>
{
//    V getSource();
//    V getTarget();
    T getLabel();
    void setLabel(T label);
}

class LabeledEdgeImpl<V, T>
    extends DefaultEdge
    implements LabeledEdge<V, T>
{
    private static final long serialVersionUID = 1L;

    protected V source;
    protected V target;
    protected T label;

    public LabeledEdgeImpl(V source, V target, T label) {
        super();
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public T getLabel() {
        return label;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "LabeledEdgeImpl [source=" + source + ", target=" + target
                + ", label=" + label + "]";
    }
}


interface LabeledEdgeFactory<V, E, T>
{
    E createEdge(V sourceVertex, V targetVertex, T label);
}

class LabeledEdgeFactoryImpl<V, T>
    implements LabeledEdgeFactory<V, LabeledEdgeImpl<V, T>, T>
{

    @Override
    public LabeledEdgeImpl<V, T> createEdge(V sourceVertex, V targetVertex, T data) {
        LabeledEdgeImpl<V, T> result = new LabeledEdgeImpl<V, T>(sourceVertex, targetVertex, data);
        return result;
    }
}
//
//
//interface LabeledEdgeFactory<V, E, T>
//{
//    E createEdge(V sourceVertex, V targetVertex, T label);
//}
//
//class LabeledEdgeFactoryImpl<V, T>

class EdgeFactoryLabeledEdge<V, T>
    implements EdgeFactory<V, LabeledEdge<V, T>>
{
    @Override
    public LabeledEdgeImpl<V, T> createEdge(V sourceVertex, V targetVertex) {
        LabeledEdgeImpl<V, T> result = new LabeledEdgeImpl<V, T>(sourceVertex, targetVertex, null);
        return result;
    }
}


interface EdgeLabelAccessor<E, T> {
    T getLabel(E edge);
    void setLabel(E edge, T label);
}

class EdgeLabelAccessorImpl<V, E, T>
    implements EdgeLabelAccessor<LabeledEdge<V, T>, T>
{
    @Override
    public T getLabel(LabeledEdge<V, T> edge) {
        T result = edge.getLabel();
        return result;
    }

    @Override
    public void setLabel(LabeledEdge<V, T> edge, T label) {
        edge.setLabel(label);
    }
}


public class MainSparqlPath2 {


    public static void main(String[] args) {
//        Prologue prologue = new Prologue();

        //Path path = PathParser.parse("((<p>/<z>)|<x>)*", PrefixMapping.Extended);
        Path path = PathParser.parse("!(<p>|(<p>|<p>))", PrefixMapping.Extended);

        /*
         * Some ugly set up of graph related stuff
         */
        EdgeFactoryLabeledEdge<Integer, P_Link> edgeFactory = new EdgeFactoryLabeledEdge<Integer, P_Link>();
        EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, P_Link>, P_Link> edgeLabelAccessor = new EdgeLabelAccessorImpl<Integer, LabeledEdge<Integer, P_Link>, P_Link>();
        DefaultDirectedGraph<Integer, LabeledEdge<Integer, P_Link>> graph = new DefaultDirectedGraph<Integer, LabeledEdge<Integer, P_Link>>(edgeFactory);
        VertexFactory<Integer> vertexFactory = new VertexFactoryInteger(graph);

        PathVisitorNfaCompilerImpl<Integer> nfaCompiler = new PathVisitorNfaCompilerImpl<Integer>(graph, vertexFactory, edgeLabelAccessor);

        /*
         * The actual nfa conversion step
         */
        path.visit(nfaCompiler);
        PartialNfa<Integer, P_Link> peek = nfaCompiler.peek();


        for(LabeledEdge<Integer, P_Link> edge : graph.edgeSet()) {
            System.out.println(edge);
        }

        System.out.println(peek);
//        System.out.println("Start state: " + peek.getStartVertex());
//        System.out.println("Final transitions: " + peek.getLooseEnds());

        System.out.println(path + " " + path.getClass());
    }



}
