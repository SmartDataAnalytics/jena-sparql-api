package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.concept_cache.core.VarUsage;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

/**
 * Given filter expressions of form ?x = <const>, where ?x is not a mandatory variable
 * (i.e. neither indirectly referenced nor projected), push the constant into to BPG's triple patterns
 *
 * Example
 * Select Distinct ?s { ?s ?p ?o . Filter(?p = rdf:type) }
 * becomes
 * Select Distinct ?s { ?s a ?o }
 *
 *
 *
 *
 * @author raven
 *
 */
public class TransformPushFiltersIntoBGP
	extends TransformCopy
{
	//public static final TransformPushFiltersIntoBGP fn = new TransformPushFiltersIntoBGP();

	protected Tree<Op> tree;

	public static Op transform(Op op) {
		Tree<Op> tree = OpUtils.createTree(op);
		Transform transform = new TransformPushFiltersIntoBGP(tree);
        Op result = Transformer.transform(transform, op);
        return result;
	}

	public TransformPushFiltersIntoBGP(Tree<Op> tree) {
		this.tree = tree;
	}

	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		Op result;

		if(subOp instanceof OpQuadPattern || subOp instanceof OpQuadBlock || subOp instanceof OpBGP) {
			VarUsage varUsage = OpUtils.analyzeVarUsage(tree, opFilter);
			System.out.println("varUsage: " + varUsage);
			Set<Var> mandatoryVars = VarUsage.getMandatoryVars(varUsage);

			ExprList exprs = opFilter.getExprs();
			Set<Set<Expr>> dnf = DnfUtils.toSetDnf(exprs);
			Map<Var, NodeValue> tmpMap = DnfUtils.extractConstantConstraints(dnf);
			Map<Var, Node> map = tmpMap.entrySet().stream()
					.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().asNode()));

			// Remove all mandatoryVars from the transformation
			map.keySet().removeAll(mandatoryVars);


			NodeTransform nodeTransform = new NodeTransformRenameMap(map);
			// Analyze the var usage of the subOp

			result = NodeTransformLib.transform(nodeTransform, subOp);
			
			// 

		} else {
			throw new RuntimeException("should not happen");
		}

		return result;
	}

}


