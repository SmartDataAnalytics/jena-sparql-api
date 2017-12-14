package org.aksw.commons.graph.index.jena.transform;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import org.aksw.commons.jena.graph.GraphVar;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class QueryToGraphVisitor
    extends OpVisitorBase
{
    protected Supplier<Node> nodeSupplier;
    protected BiMap<Var, Node> varToNode;
    protected BiMap<Quad, Node> quadToNode;
    
    
    protected GraphVar graph;
    
    // During the graph conversion we keep track of the original expression of each node
    protected BiMap<Node, Expr> nodeToExpr;
    

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
        this.nodeToExpr = HashBiMap.create();
        this.quadToNode = HashBiMap.create();
    }

    public BiMap<Var, Node> getVarToNode() {
        return varToNode;
    }

    public BiMap<Node, Var> getNodeToVar() {
        return varToNode.inverse();
    }

    public BiMap<Quad, Node> getQuadToNode() {
    	return quadToNode;
    }

    public BiMap<Node, Quad> getNodeToQuad() {
    	return quadToNode.inverse();
    }
    
    public BiMap<Node, Expr> getNodeToExpr() {
		return nodeToExpr;
	}

	public GraphVar getGraph() {
        return graph;
    }

    public Node getResult() {
        return stack.firstElement();
    }

    @Override
    public void visit(OpDistinct op) {
        op.getSubOp().visit(this);
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
        QueryToJenaGraph.dnfToGraph(graph, nodeToExpr, dnf, nodeSupplier);


        Node result = nodeSupplier.get();
        QueryToJenaGraph.addEdge(graph, result, QueryToGraph.filtered, subNode);
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
        Node result = QueryToJenaGraph.quadsToGraphNode(graph, quadToNode, quads, nodeSupplier);
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
            QueryToJenaGraph.addEdge(graph, result, QueryToGraph.unionMember, memberNode);
        }

        stack.push(result);
    }

}
