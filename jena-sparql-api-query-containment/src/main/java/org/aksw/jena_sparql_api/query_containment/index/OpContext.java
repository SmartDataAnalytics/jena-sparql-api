package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeImpl;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage2;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsageAnalyzer2Visitor;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.sparql.algebra.Op;

public class OpContext {
	protected Op originalOp;
	protected Op normalizedOp;	
	protected Tree<Op> normalizedOpTree;
	//protected OpGraph opAsGraph;
	protected Map<Op, VarUsage2> opToVarUsage;
	
	public OpContext(Op originalOp, Op normalizedOp, Tree<Op> noramlizedOpTree, Map<Op, VarUsage2> opToVarUsage) {
		super();
		this.originalOp = originalOp;
		this.normalizedOp = normalizedOp;
		this.normalizedOpTree = noramlizedOpTree;
		//this.opAsGraph = opAsGraph;
		this.opToVarUsage = opToVarUsage;
	}

	public Op getOriginalOp() {
		return originalOp;
	}

	public Op getNormalizedOp() {
		return normalizedOp;
	}

	public Tree<Op> getNormalizedOpTree() {
		return normalizedOpTree;
	}

//	public OpGraph getOpAsGraph() {
//		return opAsGraph;
//	}

	public Map<Op, VarUsage2> getOpToVarUsage() {
		return opToVarUsage;
	}

	public static OpContext create(Op op) {		
        Op normalizedOp = QueryToGraph.normalizeOp(op, true);
        Tree<Op> normalizedOpTree = TreeImpl.create(normalizedOp, OpUtils::getSubOps);
        
        //OpGraph opGraph = QueryContainmentIndexImpl.queryToOpGraph(normalizedOp);
        Map<Op, VarUsage2> opToVarUsage = VarUsageAnalyzer2Visitor.analyze(op);
        
        
        OpContext result = new OpContext(op, normalizedOp, normalizedOpTree, opToVarUsage);
        
        return result;
	}
}
