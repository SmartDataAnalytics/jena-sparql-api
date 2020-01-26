package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

/**
 * Pull filters up so that they may be distributed over a join
 * 
 * Example:
 * Join(
 *   Filter({_?v1_}), ?v1=_),
 *   { _?v1_ })
 * 
 * Becomes:
 * Filter(Join({_?v1_}, {_?v1_},?v1=_)) 
 * 
 * Allows: Subsequent filter placement:
 * Join(
 *   Filter({ _?v1_}), ?v1=_),
 *   Filter({ _?v1_ }, ?v1=_)
 * 
 */
public class TransformPullFilters
	extends TransformCopy
{	
    public static Op transform(Op op) {
        Transform transform = new TransformPullFilters();
        Op result = Transformer.transform(transform, op);
        return result;
    }

    public boolean condition(Op op) {
    	return true;
    }
	
	public static void todoMoveToUnitTest(String[] args) {
		
		String input =
			"SELECT * {\n" + 
			"    {\n" + 
			"       ?s ?p ?o\n" + 
			"       FILTER(?s = 'a' || ?o = 'b')\n" + 
			"       FILTER(?p = 'x')\n" + 
			"    }  UNION\n" + 
			"    {\n" + 
			"       ?s ?p ?o\n" + 
			"       FILTER(?s = 'a' || ?o = 'b')\n" + 
			"       FILTER(?p = 'y')\n" + 
			"    }\n" + 
			"}";
		
		
		Op expected = Algebra.compile(QueryFactory.create("SELECT  *\n" + 
				"WHERE\n" + 
				"  {   { ?s  ?p  ?o\n" + 
				"        FILTER ( ?p = \"x\" )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?s  ?p  ?o\n" + 
				"        FILTER ( ?p = \"y\" )\n" + 
				"      }\n" + 
				"    FILTER ( ( ?s = \"a\" ) || ( ?o = \"b\" ) )\n" + 
				"  }"));
		
		Op a = Algebra.compile(QueryFactory.create(input));
		Op actual = transform(a);
		Query bq = OpAsQuery.asQuery(actual);
		System.out.println(bq);
		System.out.println(Objects.equals(expected, actual));
		
		
	}
	
	
	public static Op xtransformUnion(
			Collection<? extends Op> subOps, Function<? super List<Op>, ? extends Op> unionCtor,
			Predicate<?super Op> applyCondition) {
		// We can only distribute those those parts of filters over a union
		// that occurr in every element
		
		// For every subOp tidy the filters
		List<Op> newSubOps = new ArrayList<>();
		
		
		List<Set<Expr>> els = new ArrayList<>(subOps.size());
		for(Op subOp : subOps) {
			
			if(subOp instanceof OpFilter && applyCondition.test(subOp)) {
				OpFilter tidied = OpFilter.tidy((OpFilter)subOp);
				
				// If the filter contains special variables, such as introduced by group by, we need to skip this
				boolean containsSpecialVar = TransformPullFiltersIfCanMergeBGPs.containsSpecialVar(tidied.getExprs().getVarsMentioned());
				
				if(containsSpecialVar) {
					newSubOps = null;
				} else {				
					els.add(new LinkedHashSet<>(tidied.getExprs().getList()));
				}
			} else {
				newSubOps = null;
			}
		}

		Op result = null;
		if(newSubOps != null) {
			Set<Expr> common = els.isEmpty()
					? Collections.emptySet()
					: new LinkedHashSet<Expr>(els.get(0));
	
			for(int i = 1; i < els.size(); ++i) {
				Set<Expr> e = els.get(i);
				common.retainAll(e);
			}
	
			if(!common.isEmpty()) {
				int i = 0;
				for(Op subOp : subOps) {
					Set<Expr> e = els.get(i++);
					
					Set<Expr> remain = Sets.difference(e, common);
					Op newSubOp = ((OpFilter)subOp).getSubOp();
					if(!remain.isEmpty()) {
						ExprList tmp = new ExprList(new ArrayList<>(remain));
						newSubOp = OpFilter.filterBy(tmp, newSubOp);
					}
					
					newSubOps.add(newSubOp);
				}
				result = OpFilter.filterBy(new ExprList(new ArrayList<>(common)), unionCtor.apply(newSubOps));
			}

		}

		if(result == null) {
			result = unionCtor.apply(new ArrayList<>(subOps));
		}
			
		return result;
	}
 
	@Override
	public Op transform(OpJoin opJoin, Op left, Op right) {
		Op tmp = TransformPullFiltersIfCanMergeBGPs.xtransform(
				Arrays.asList(left, right),
				subOps -> OpJoin.create(subOps.get(0), subOps.get(1)),
				this::condition
				);
	
		Op result = tmp == null
				? super.transform(opJoin, left, right)
				: tmp;
				
		return result;
	}

	@Override
	public Op transform(OpUnion opUnion, Op left, Op right) {
		Op tmp = xtransformUnion(
				Arrays.asList(left, right),
				subOps -> OpUnion.create(subOps.get(0), subOps.get(1)),
				this::condition
				);
	
		Op result = tmp == null
				? super.transform(opUnion, left, right)
				: tmp;
				
		return result;
	}


//	@Override
//	public Op transform(OpSequence opSequence, List<Op> elts) {
//		Op tmp = TransformPullFiltersIfCanMergeBGPs.xtransform(elts, opSequence::copy, this::condition);
//
//		Op result = tmp == null
//				? super.transform(opSequence, elts)
//				: tmp;
//				
//		return result;
//	}
}
