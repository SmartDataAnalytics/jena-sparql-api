package org.aksw.jena_sparql_api.algebra.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.walker.Walker;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprVar;


/**
 * Transform expressions to property functions.
 * 
 * 
 * Transform function calls in expressions, such as internalIdOf(?x) to a triple pattern
 * ?x internalIdOf ?y - with ?y being a fresh variable for the expressions result.
 * 
 * 
 * 
 */
public class TransformExprToBasicPattern
	extends TransformCopy
{
	public static Op transform(Op op, Function<Expr, Entry<String, Boolean>> testSubstitution) {
		Set<Var> mentionedVars = new HashSet<>(OpVars.mentionedVars(op));
		Generator<Var> varGen = VarGeneratorBlacklist.create(mentionedVars);
		TransformExprToBasicPattern xform = new TransformExprToBasicPattern(varGen, testSubstitution);
		Op result = Transformer.transform(xform, op);
		return result;
	}
	
	
	public static class ExprTransformFindLeafFunctions
		extends ExprTransformCopy
	{
		protected Generator<Var> varGen;
		protected Function<Expr, Entry<String, Boolean>> testSubstitution;
		protected Map<Expr, Var> substitutions = new HashMap<>();
		protected BasicPattern triples = new BasicPattern();
		
		
		
		
		public ExprTransformFindLeafFunctions(Generator<Var> varGen,
				Function<Expr, Entry<String, Boolean>> testSubstitution, Map<Expr, Var> substitutions,
				BasicPattern triples) {
			super();
			this.varGen = varGen;
			this.testSubstitution = testSubstitution;
			this.substitutions = substitutions;
			this.triples = triples;
		}



		public Expr transform(ExprFunction1 func, Expr arg) {
			Node argNode = arg.isVariable() 
					? arg.asVar()
					: arg.isConstant()
						? arg.getConstant().asNode()
						: null;

			Expr result = null;
			if(argNode != null) {						
				//String funcIri = func.getFunctionIRI();
				Entry<String, Boolean> substInfo = testSubstitution.apply(func);
				
				if(substInfo != null) {
					Var v = substitutions.computeIfAbsent(func, f -> varGen.next());
					Node p = NodeFactory.createURI(substInfo.getKey());
					
					boolean subjectAsOutput = substInfo.getValue();
					Triple t = TripleUtils.create(argNode, p, v, subjectAsOutput);
					triples.add(t);
					
					result = new ExprVar(v);
				}
			}
			
			if(result == null) {
				result = super.transform(func, arg);
			}
			
			return result;
		}		
	}


	
	
	public TransformExprToBasicPattern(Generator<Var> varGen, Function<Expr, Entry<String, Boolean>> testSubst) {
		super();
		this.varGen = varGen;
		this.testSubst = testSubst;
	}

	protected Generator<Var> varGen;
	protected Function<Expr, Entry<String, Boolean>> testSubst;

	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		ExprList els = opFilter.getExprs();

		//protected Function<Expr, Entry<String, Boolean>> testSubstitution;
		Map<Expr, Var> substitutions = new HashMap<>();
		BasicPattern triples = new BasicPattern();

		ExprList newExprs = new ExprList();
		for(Expr expr : els) {
			ExprTransformFindLeafFunctions xform = new ExprTransformFindLeafFunctions(
					varGen,
					testSubst,
					substitutions,
					triples);
			Expr x = Walker.transform(expr, xform);
			newExprs.add(x);
		}

		Op result = newExprs.equals(els)
			? super.transform(opFilter, subOp)
			: OpFilter.filterBy(newExprs, OpJoin.create(subOp, new OpBGP(triples)));
		

		return result;
	}
	
}
