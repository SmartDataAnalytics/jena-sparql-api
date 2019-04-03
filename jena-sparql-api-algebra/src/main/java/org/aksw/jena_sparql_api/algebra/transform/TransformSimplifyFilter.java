package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.collections.tagmap.TagMapSimple;
import org.aksw.commons.collections.tagmap.TagSet;
import org.aksw.commons.collections.tagmap.TagSetImpl;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.optimize.TransformExpandOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

public class TransformSimplifyFilter
	extends TransformCopy
{
    public static Op transform(Op op) {
        Transform transform = new TransformSimplifyFilter();
        Op result = Transformer.transform(transform, op);
        return result;
    }

	@Override
	public Op transform(OpFilter opFilter, Op subOp) {
		OpFilter tmp = OpFilter.tidy(opFilter);
	
		TransformExpandOneOf expander = new TransformExpandOneOf();
		Op op = expander.transform(tmp, tmp.getSubOp());

		tmp = (OpFilter)op;
		
		Set<Set<Expr>> cnf = CnfUtils.toSetCnf(tmp.getExprs());
		
		removeSubsumedCnfClause(cnf);
	
		ExprList exprs = CnfUtils.toExprList(cnf);
		
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
