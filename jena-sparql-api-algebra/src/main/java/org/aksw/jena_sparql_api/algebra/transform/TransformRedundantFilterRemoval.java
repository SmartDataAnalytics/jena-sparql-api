package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Remove filters in patterns such as

 * 
 * OpFilter(OpExtend(constant AS ?x, (subOp)), ?x = constant)
 * 
 * Currently this works only if the OpExtend is an immediate child of OpFilter
 * 
 * 
 * Mainly needed as a workaround for Virtuoso...
 * 
 * 
 * @author Claus Stadler, Jan 13, 2019
 *
 */
public class TransformRedundantFilterRemoval
	extends TransformCopy
{
    public static Op transform(Op op) {
        Transform transform = new TransformRedundantFilterRemoval();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	
	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		Op result;
		
		if(subOp instanceof OpExtend) {
			OpExtend so = (OpExtend)subOp;
			
			VarExprList vel = so.getVarExprList();

			Map<Var, Expr> map = vel.getExprs();
			

//			// Convert the vel to constraints
//			Set<Expr> constraints = new LinkedHashSet<Expr>();
//			for(Entry<Var, Expr> e : map.entrySet()) {
//				E_Equals c = new E_Equals(new ExprVar(e.getKey()), e.getValue());
//				constraints.add(c);
//			}
			
			// Check if the constraints of the filters are redundant
			Set<Set<Expr>> cnf = CnfUtils.toSetCnf(opFilter.getExprs());
			
			// TODO Add a util function to normalize argument orders
			
			// For each single element clause, check whether it's the same as in the bind
			boolean modified = false;
			
			Set<Set<Expr>> newCnf = new LinkedHashSet<>();

			for(Set<Expr> clause : cnf) {
				boolean modifiedClause = false;
				if(clause.size() == 1) {
										
					Expr expr = clause.iterator().next();

					// In FILTER IN expressions, remove all constants
					// that match that of a BIND
					if(expr instanceof E_OneOf) {
						E_OneOf eoo = (E_OneOf)expr;
						
						Expr lhs = eoo.getLHS();
						if(lhs.isVariable()) {
							Var var = lhs.asVar();
							
							// If there is a defining expression, which appears
							// in the one-of list, then evaluate to true
							Expr def = map.get(var);

							if(def != null) {
							
								ExprList args = eoo.getRHS();
		
								
								boolean isTrue = args.getList().contains(def);
								
								if(isTrue) {
									modifiedClause = true;
								} else if (def.isConstant()) {
									// Remove all (at this point implicitly unequal) constants from the arg list
									ExprList newArgs = new ExprList();
									for(Expr arg : args) {
										if(!arg.isConstant()) {
											newArgs.add(arg);
										}
									}

									if(newArgs.size() != args.size()) {								
										modifiedClause = true;
		
										// If new newArgs is empty we have removed all possble values
										// Note, that ?p IN () means FALSE!
										if(!newArgs.isEmpty()) {
											Expr newExpr = new E_OneOf(lhs, newArgs);
											newCnf.add(Collections.singleton(newExpr));
										} else {
											newCnf.add(Collections.singleton(NodeValue.FALSE));											
										}
									}
								}
							}
						}
						
					} else if (expr instanceof E_Equals) {
						// If the expr is e_equals with var and constant arguments...
						Entry<Var, NodeValue> vc = ExprUtils.extractVarConstant(expr);
				
						if(vc != null) {
							// and the same entry exists in the BIND's map
							if(Objects.equals(map.get(vc.getKey()), vc.getValue())) {
								// ... remove the clause
								modifiedClause = true;
							}
						}
					}
										
				}
				
				if(!modifiedClause) {
					newCnf.add(clause);
				}						

				modified = modified || modifiedClause;
			}

			
			if(modified) {
				result = newCnf.isEmpty() ? subOp : OpFilter.filter(CnfUtils.toExpr(newCnf), subOp);
			} else {
				result = opFilter;
			}
			
		} else {
			result = opFilter;
		}
		
		
		return result;
	}
	
}

//
//class ConstraintAnalyzer
//	extends OpVisitorBase
//{
//    protected Map<Op, Set<Set<Expr>>> opToConstraints = new IdentityHashMap<>();
//
//    // Bind is treated as an implicit constraint
//    @Override
//    public void visit(OpExtend op) {
//    	VarExprList vel = op.getVarExprList();
//    	
//    	
//    }
//    
//    @Override
//    public void visit(OpFilter op) {    	
//    	op.getSubOp().visit(this);
//
//    	VarUsage2 varUsage = new VarUsage2();
//    	opToVarUsage.put(op, varUsage);
//
//    	reuseVisibleVars(varUsage, op.getSubOp());
//
//        processExprs(op, op.getExprs());
//    }
//
//    
//    
//    public static Map<Op, Set<Set<Expr>>> analyze(Op op) {
//    	VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
//    	Map<Op, VarUsage2> result = analyze(op, varUsageAnalyzer);
//    	return result;
//    }
//
//    public static Map<Op, Set<Set<Expr>>> analyze(Op op, VarUsageAnalyzer2Visitor varUsageAnalyzer) {
//        //VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
//        op.visit(varUsageAnalyzer);
//        
//        Map<Op, VarUsage2> result = varUsageAnalyzer.getResult();
//
//        VarUsage2 varUsage = result.get(op);
//        Set<Var> visibleVars = varUsage.getVisibleVars();
//        markEssential(result, op, visibleVars);
//        
//        return result;
//    }
//
//}