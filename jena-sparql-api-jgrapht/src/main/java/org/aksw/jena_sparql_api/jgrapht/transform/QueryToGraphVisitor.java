package org.aksw.jena_sparql_api.jgrapht.transform;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.graph.GraphFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class QueryToGraphVisitor
    extends OpVisitorBase
{
    protected Supplier<Node> nodeSupplier;
    protected BiMap<Var, Node> varToNode;
    protected GraphVar graph;

    protected Stack<Node> stack = new Stack<>();

    public QueryToGraphVisitor() {
        this(new GraphVarImpl(), () -> NodeFactory.createBlankNode());
    }

    public QueryToGraphVisitor(Supplier<Node> nodeSupplier) {
        this(new GraphVarImpl(), nodeSupplier);
    }


    public QueryToGraphVisitor(GraphVar graph, Supplier<Node> nodeSupplier) {
        super();
        this.graph = graph;
        this.nodeSupplier = nodeSupplier;// = new GeneratorBlacklist(generator, blacklist)
        this.varToNode = HashBiMap.create();
    }

    public BiMap<Var, Node> getVarToNode() {
        return varToNode;
    }

    public BiMap<Node, Var> getNodeToVar() {
        return varToNode.inverse();
    }


    public GraphVar getGraph() {
        return graph;
    }

    public Node getResult() {
        return stack.firstElement();
    }

    @Override
    public void visit(OpProject op) {
        op.getSubOp().visit(this);
    }

    @Override
    public void visit(OpFilter op) {
        Op subOp = op.getSubOp();
        subOp.visit(this);
        Node subNode = stack.pop();

        ExprList exprs = op.getExprs();
        Set<Set<Expr>> dnf = DnfUtils.toSetDnf(exprs);
        QueryToJenaGraph.equalExprsToGraph(graph, dnf, nodeSupplier, varToNode);


        Node result = nodeSupplier.get();
        QueryToJenaGraph.addEdge(graph, QueryToGraph.filtered, result, subNode, nodeSupplier, varToNode);
        stack.push(result);
    }

    @Override
    public void visit(OpQuadBlock op) {
        QuadPattern quadPattern = op.getPattern();
        List<Quad> quads = quadPattern.getList();
        handleQuads(quads);
    }

    @Override
    public void visit(OpBGP op) {
        List<Quad> quads = QuadPatternUtils.toQuadPattern(op.getPattern()).getList();
        handleQuads(quads);
    }

    @Override
    public void visit(OpQuadPattern op) {
        List<Quad> quads = op.getPattern().getList();
        handleQuads(quads);
    }

    public void handleQuads(List<Quad> quads) {
        Node result = QueryToJenaGraph.quadsToGraphNode(graph, quads, nodeSupplier, varToNode);
        stack.push(result);
        //QueryToJenaGraph.quadsToGraph(graph, quads, nodeSupplier, varToNode);
    }



    @Override
    public void visit(OpDisjunction op) {
        Node result = nodeSupplier.get();
        List<Op> ops = op.getElements();
        for(Op member : ops) {
            member.visit(this);
            Node memberNode = stack.pop();
            QueryToJenaGraph.addEdge(graph, QueryToGraph.unionMember, result, memberNode, nodeSupplier, varToNode);
        }

        stack.push(result);
    }

}
