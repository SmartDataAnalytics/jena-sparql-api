package org.aksw.jena_sparql_api.jgrapht.transform;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aksw.jena_sparql_api.jgrapht.wrapper.LabeledEdge;
import org.aksw.jena_sparql_api.jgrapht.wrapper.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

public class QueryToGraphVisitor
    extends OpVisitorBase
{
    protected DirectedGraph<Node, LabeledEdge<Node, Node>> graph;

    protected Stack<Node> stack = new Stack<>();

    public QueryToGraphVisitor() {
        this(new SimpleDirectedGraph<>((v, e) -> new LabeledEdgeImpl<>(v, e, null)));
    }

    public QueryToGraphVisitor(DirectedGraph<Node, LabeledEdge<Node, Node>> graph) {
        super();
        this.graph = graph;
    }

    public DirectedGraph<Node, LabeledEdge<Node, Node>> getGraph() {
        return graph;
    }

    public Node getResult() {
        return stack.firstElement();
    }


    @Override
    public void visit(OpFilter op) {
        Op subOp = op.getSubOp();
        subOp.visit(this);
        Node subNode = stack.pop();

        ExprList exprs = op.getExprs();
        Set<Set<Expr>> dnf = DnfUtils.toSetDnf(exprs);
        QueryToGraph.equalExprsToGraph(graph, dnf);


        Node result = NodeFactory.createBlankNode();
        QueryToGraph.addEdge(graph, QueryToGraph.filtered, result, subNode);
        stack.push(result);
    }

    @Override
    public void visit(OpQuadBlock op) {
        QuadPattern quadPattern = op.getPattern();
        List<Quad> quads = quadPattern.getList();

        Node result = QueryToGraph.quadsToGraphNode(graph, quads);
        stack.push(result);
    }

    @Override
    public void visit(OpBGP op) {
        List<Quad> quads = QuadPatternUtils.toQuadPattern(op.getPattern()).getList();
        Node result = QueryToGraph.quadsToGraphNode(graph, quads);
        stack.push(result);
    }

    @Override
    public void visit(OpQuadPattern op) {
        List<Quad> quads = op.getPattern().getList();
        Node result = QueryToGraph.quadsToGraphNode(graph, quads);
        stack.push(result);
    }



    @Override
    public void visit(OpDisjunction op) {
        Node result = NodeFactory.createBlankNode();
        List<Op> ops = op.getElements();
        for(Op member : ops) {
            member.visit(this);
            Node memberNode = stack.pop();
            QueryToGraph.addEdge(graph, QueryToGraph.unionMember, result, memberNode);
        }

        stack.push(result);
    }

}
