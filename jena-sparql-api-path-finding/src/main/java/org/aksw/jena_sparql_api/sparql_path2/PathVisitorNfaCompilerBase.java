package org.aksw.jena_sparql_api.sparql_path2;

import java.util.Stack;
import java.util.function.Supplier;

import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Distinct;
import org.apache.jena.sparql.path.P_FixedLength;
import org.apache.jena.sparql.path.P_Mod;
import org.apache.jena.sparql.path.P_Multi;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_Shortest;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;
import org.jgrapht.Graph;

abstract class PathVisitorNfaCompilerBase<V, E, T>
    implements PathVisitor
{
    protected Graph<V, E> graph;
    protected Supplier<V> vertexFactory;
    protected EdgeLabelAccessor<E, T> edgeLabelAccessor;

    protected Stack<PartialNfa<V, T>> stack;

    public PartialNfa<V, T> peek() {
        PartialNfa<V, T> result = stack.peek();
        return result;
    }


    public PathVisitorNfaCompilerBase(Graph<V, E> graph,
            Supplier<V> vertexFactory,
            EdgeLabelAccessor<E, T> edgeLabelAccessor) {
        this(graph, vertexFactory, edgeLabelAccessor, new Stack<PartialNfa<V, T>>());
    }

    public PathVisitorNfaCompilerBase(Graph<V, E> graph,
            Supplier<V> vertexFactory, EdgeLabelAccessor<E, T> edgeLabelAccessor, Stack<PartialNfa<V, T>> stack) {
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
    public void visit(P_OneOrMore1 path) {
        PartialNfa<V, T> left = process(path.getSubPath());

        PartialNfa<V, T> next = NfaOps.oneOrMore(graph, vertexFactory, left, edgeLabelAccessor);
        stack.push(next);
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