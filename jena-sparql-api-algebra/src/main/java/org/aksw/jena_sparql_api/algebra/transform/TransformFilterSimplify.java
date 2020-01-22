package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.commons.collections.tagmap.TagMapSimple;
import org.aksw.commons.collections.tagmap.TagSet;
import org.aksw.commons.collections.tagmap.TagSetImpl;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformNormalizeVarConstant;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.optimize.TransformExpandOneOf;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprSystem;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

public class TransformFilterSimplify
	extends TransformCopy
{
    public static Op transform(Op op) {
        Transform transform = new TransformFilterSimplify();
        Op result = Transformer.transform(transform, op);
        return result;
    }

    public static Expr partialEvalCnf(Expr expr, Map<Var, Node> map) {
    	Set<Var> vars = expr.getVarsMentioned();
    	Set<Var> boundVars = map.keySet();

    	// TODO Skip evaluation of non-deterministic functions or those having side effects

    	Expr result;
    	
    	// FIXME Evaluation of ExprFunctionOp such as EXISTS and NOT EXIST
    	// yields an NPE - if the expression uses this feature anywhere, we need to skip the eval
    	if(!(expr instanceof ExprFunctionOp) && !(expr instanceof ExprSystem) && boundVars.containsAll(vars)) {
        	Binding binding = BindingUtils.fromMap(map);

        	result = ExprUtils.eval(expr, binding);
    	} else {
    		result = expr;
    	}
    	
    	return result;
    }
        
    public static Set<Set<Expr>> processClausesInPlace(Set<Set<Expr>> nf, Consumer<? super Set<Expr>> inPlaceTransform) {
    	
    	List<Set<Expr>> copy = new ArrayList<>(nf);
    	nf.clear();
    	for(Set<Expr> clause : copy) {
    		inPlaceTransform.accept(clause);
    		nf.add(clause);
    	}
    	return nf;
    }
    
    public static Set<Set<Expr>> applyExprTransform(Set<Set<Expr>> nf, ExprTransform exprTransform) {
    	processClausesInPlace(nf, c -> applyExprTransformC(exprTransform, c));
    	return nf;
    }
    
    public static <T, D extends Collection<T>, C extends Collection<D>> C applyExprTransform2(C nested, Function<? super T, ? extends T> fn) {
    	
    	ArrayList<D> copy = new ArrayList<>(nested);
    	nested.clear();

    	for(D clause : copy) {
    		mapItems(clause, fn);
    		nested.add(clause);
    	}
    	return nested;

    }

    
//    public static Set<Set<Expr>> applyExprTransform(Set<Set<Expr>> nf, Transform transform) {
//    	return processClausesInPlace(nf, TransformFilterSimplify::normalizeEqualityC);
//    }

    public static <C extends Collection<Expr>> C applyExprTransformC(ExprTransform transform, C clause) {
    	return mapItems(clause, e -> ExprTransformer.transform(transform, e));
    }
    
    public static <T, C extends Collection<T>> C mapItems(C collection, Function<? super T, ? extends T> fn) {
    	
    	List<T> copy = new ArrayList<>(collection);
    	collection.clear();
    	for(T item : copy) {
        	T mapped = fn.apply(item);

    		collection.add(mapped);
//        	if(!Objects.equal(item, mapped)) {
//        		collection.remove(item);
//        		collection.add(mapped);
//        	}
        }
    	
    	return collection;
    }

    
    public static Set<Expr> subsitutePositiveNegativeLiteralTrue(Set<Expr> clause) {
    	return subsitutePositiveNegativeLiteral(clause, NodeValue.TRUE);
    }
    
    /**
     * Check for clauses that contain both A and !A and substitute their occurrences with
     * the given value.
     * 
     * @param clause
     * @param value
     * @return
     */
    public static Set<Expr> subsitutePositiveNegativeLiteral(Set<Expr> clause, Expr value) {
    	for(Expr expr : new ArrayList<>(clause)) {
    		if(expr instanceof E_LogicalNot) {
    			E_LogicalNot neg = (E_LogicalNot)expr;
    			
    			Expr pos = neg.getArg();
    			

    			if(clause.contains(pos)) {
    				clause.remove(neg);
    				clause.remove(pos);
    				clause.add(value);
    			}
    		}
        }
    	
    	return clause;
    }

    
    public static Set<Set<Expr>> tidyBooleanConstants(Set<Set<Expr>> cnf) {

    	ArrayList<Set<Expr>> copy = new ArrayList<>(cnf);
    	cnf.clear();
		for(Set<Expr> clause : copy) {
			if(clause.contains(NodeValue.TRUE)) {
				clause.clear();
				clause.add(NodeValue.TRUE);
			} else if(clause.contains(NodeValue.FALSE)) {
				if(clause.size() > 1) {
					clause.remove(NodeValue.FALSE);
				}
			}
			
			cnf.add(clause);
		}
		
		// If any clause is FALSE, the whole result is false
		if(cnf.contains(ClauseUtils.FALSE)) {
			cnf.clear();
			// Mutable instance of FALSE required as otherwise in place ExprTransform may raise an exception
			cnf.add(ClauseUtils.newFalse());
		}

		return cnf;
    }

	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		OpFilter tmp = OpFilter.tidy(opFilter);
	
		TransformExpandOneOf expander = new TransformExpandOneOf();
		Op op = expander.transform(tmp, tmp.getSubOp());

		tmp = (OpFilter)op;
		
		Set<Set<Expr>> cnf = CnfUtils.toSetCnf(tmp.getExprs());

		// Any clause containing TRUE becomes TRUE
		// Conversely, remove FALSE from clauses that contain other conditions
		tidyBooleanConstants(cnf);

		
		applyExprTransform(cnf, new ExprTransformNormalizeVarConstant());
		processClausesInPlace(cnf, TransformFilterSimplify::subsitutePositiveNegativeLiteralTrue);
		Map<Var, Node> varMap = CnfUtils.getConstants(cnf, false);


		applyExprTransform2(cnf, e -> TransformFilterSimplify.partialEvalCnf(e, varMap));

		tidyBooleanConstants(cnf);
		
		// Add the constant constraints again
		for(Entry<Var, Node> e : varMap.entrySet()) {
			//new LinkedHashSet<>()
//			cnf.add(Collections.singleton(new E_Equals(new ExprVar(e.getKey()), NodeValue.makeNode(e.getValue()))));
			cnf.add(new LinkedHashSet<>(Collections.singleton(new E_Equals(new ExprVar(e.getKey()), NodeValue.makeNode(e.getValue())))));
		}
		
		removeSubsumedCnfClause(cnf);
		ExprList exprs = CnfUtils.toExprList(cnf);

		// Now try to substitute the remaining expressions with ther
		
		Op result = OpFilter.filterBy(exprs, tmp.getSubOp());
		return result;
		
		// TODO Auto-generated method stub
		//return super.transform(opFilter, subOp);
	}
	

	/**
	 * In-place modification of the given dnf which removes subsumed clauses
	 * based on equivalence of literals:
	 * For example:
	 * (A) AND (A OR B) -&gt; {{A}, {A, B}} -&gt; {{A}}
	 * 
	 * @param dnf
	 * @return
	 */
	public static Set<Set<Expr>> removeSubsumedCnfClause(Set<Set<Expr>> cnf) {
		//TagSet<Set<Expr>> tagSet = new TagSetImpl<>(new TagMapSetTrie<>(), x -> x);
		TagSet<Set<Expr>> tagSet = new TagSetImpl<>(new TagMapSimple<>(), x -> x);
		
		for(Set<Expr> clause : cnf) {
			tagSet.add(clause);
		}
		
		// Remove all subsumed clauses 
		Iterator<Set<Expr>> it = cnf.iterator();
		while(it.hasNext()) {
			Set<Expr> clause = it.next();
			TagSet<Set<Expr>> found = tagSet.getSubItemsOf(clause, true);
			
			if(!found.isEmpty()) {
				it.remove();
			}
		}

		return cnf;
	}
	
}
