package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;
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
import org.apache.jena.sparql.path.PathVisitor;

public class PathVisitorApplyTransform
    implements PathVisitor
{
    protected PathTransform pathTransform;
    protected NodeTransform nodeTransform;
    protected Deque<Path> stack = new ArrayDeque<Path>();

    public PathVisitorApplyTransform(PathTransform pathTransform, NodeTransform nodeTransform) {
        this.pathTransform = pathTransform;
        this.nodeTransform = nodeTransform;
    }

    public Path getResult() {
        if(stack.size() != 1) {
            throw new RuntimeException("Stack not aligned");
        }

        Path result = stack.pop();
        return result;
    }

    public Node applyNodeTransform(Node node) {
        Node result = nodeTransform == null ? node : nodeTransform.apply(node);
        return result;
    }

    @Override
    public void visit(P_Link path) {
        Node newNode = applyNodeTransform(path.getNode());
        Path newPath = pathTransform.transform(path, newNode);
        stack.push(newPath);
    }

    @Override
    public void visit(P_ReverseLink path) {
        Node newNode = applyNodeTransform(path.getNode());
        Path newPath = pathTransform.transform(path, newNode);
        stack.push(newPath);
    }

    @Override
    public void visit(P_NegPropSet path) {
        List<Node> newFwdNodes = path.getFwdNodes();
        List<Node> newBwdNodes = path.getBwdNodes();
        Path newPath = pathTransform.transform(path, newFwdNodes, newBwdNodes);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Inverse path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Mod path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath, path.getMin(), path.getMax());
        stack.push(newPath);
    }

    @Override
    public void visit(P_FixedLength path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath, path.getCount());
        stack.push(newPath);
    }

    @Override
    public void visit(P_Distinct path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Multi path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Shortest path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_ZeroOrOne path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_ZeroOrMore1 path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_ZeroOrMoreN path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_OneOrMore1 path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_OneOrMoreN path) {
        Path subPath = stack.pop() ;
        Path newPath = pathTransform.transform(path, subPath);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Alt path) {
        Path newRight = stack.pop() ;
        Path newLeft = stack.pop() ;
        Path newPath = pathTransform.transform(path, newLeft, newRight);
        stack.push(newPath);
    }

    @Override
    public void visit(P_Seq path) {
        Path newRight = stack.pop() ;
        Path newLeft = stack.pop() ;
        Path newPath = pathTransform.transform(path, newLeft, newRight);
        stack.push(newPath);
    }
}