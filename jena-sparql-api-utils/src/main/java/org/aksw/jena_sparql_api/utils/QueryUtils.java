package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.backports.syntaxtransform.QueryTransformOps;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class QueryUtils {
	
	/**
	 * Restore a query form from a prototype.
	 * Typical use case is when a query form should be restored after
	 * it was compiled using Algebra.compile(). 
	 * 
	 * @param query
	 * @param proto
	 * @return
	 */
	public static Query restoreQueryForm(Query query, Query proto) {
		if(!query.isSelectType()) {
			throw new RuntimeException("SELECT query expected - got: " + query);
		}

		Query result;
		int tgtQueryType = proto.getQueryType();
		switch(tgtQueryType) {
		case Query.QueryTypeSelect:
			result = query.cloneQuery();
			break;
		case Query.QueryTypeConstruct:
			result = selectToConstruct(query, proto.getConstructTemplate());
			break;
		case Query.QueryTypeAsk:
			result = query.cloneQuery();
			query.setQueryAskType();
			break;
		case Query.QueryTypeDescribe:
			result = query.cloneQuery();
			query.setQueryDescribeType();
			for(Node node : proto.getResultURIs()) {
				query.addDescribeNode(node);
			}
			for(Var var : proto.getProjectVars()) {
				query.addDescribeNode(var);
			}
			break;
		default:
			throw new RuntimeException("unsupported query type");
			//proto.result
		}

		result.setPrefixMapping(proto.getPrefixMapping());

		return result;
	}

	// Create a construct query from a select query and a template
	public static Query selectToConstruct(Query query, Template template) {
		Query result = new Query();
		result.setQueryConstructType();
		result.setConstructTemplate(template != null ? template : new Template(new BasicPattern()));
		
		boolean canActAsConstruct = QueryUtils.canActAsConstruct(query);
		if(canActAsConstruct) {
			result.setQueryPattern(query.getQueryPattern());
		} else {
			result.setQueryPattern(new ElementSubQuery(query));
		}

		result.setLimit(query.getLimit());
		result.setOffset(query.getOffset());
		List<SortCondition> scs = query.getOrderBy();
		if(scs != null) {
			for(SortCondition sc : scs) {
				result.addOrderBy(sc);
			}
			scs.clear();
		}
		
		query.setLimit(Query.NOLIMIT);
		query.setOffset(Query.NOLIMIT);

		return result;
	}
	/**
	 * Rewrite a query based on an algebraic transformation; preserves the construct
	 * template
	 * 
	 * 
	 * @param beforeQuery
	 * @param xform
	 * @return
	 */
	public static Query rewrite(Query beforeQuery, Function<? super Op, ? extends Op> xform) {
		Op beforeOp = Algebra.compile(beforeQuery);
		Op afterOp = xform.apply(beforeOp);// Transformer.transform(xform, beforeOp);
		Query result = OpAsQuery.asQuery(afterOp);
		
		if(beforeQuery.isConstructType()) {
			result.setQueryConstructType();
			Template template = beforeQuery.getConstructTemplate();
			result.setConstructTemplate(template);
		}
		
		return result;
	}

	// Get a query pattern (of a select query) in a way that it can be injected as a query pattern of a construct query
	public static Element asPatternForConstruct(Query q) {
		Element result = canActAsConstruct(q)
			? q.getQueryPattern()
			: new ElementSubQuery(q);
			
		return result;
	}
	
	public static boolean canActAsConstruct(Query q) {
		boolean result = !q.hasAggregators() && !q.hasGroupBy() && !q.hasValues() && !q.hasHaving();
		return result;
	}
	
    public static Query applyNodeTransform(Query query, NodeTransform nodeTransform) {

        ElementTransform eltrans = new ElementTransformSubst2(nodeTransform) ;
        //NodeTransform nodeTransform = new NodeTransformSubst(nodeTransform) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans);
        
        
        Query result = org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps.transform(query, eltrans, exprTrans) ;

        // Fix prefixes in sub queries by clearing them
        ElementWalker.walk(result.getQueryPattern(), new ElementVisitorBase() {
        	@Override
        	public void visit(ElementSubQuery el) {
        		el.getQuery().getPrefixMapping().clearNsPrefixMap();
        	}
        });

//        Query result = tmp;
//       ElementVisitor
//        //tmp.getQueryPattern().vi
//        ElementTransform clearPrefixesInSubQuery = new ElementTransformCopyBase() {
//    		@Override
//    		public Element transform(ElementSubQuery el, Query query) {
//    			ElementSubQuery x = (ElementSubQuery)super.transform(el, query);
//    			x.getQuery().getPrefixMapping().clearNsPrefixMap();
//    			
//    			return x;
//    		}
//    	};
 
//        Query result = org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps.transform(tmp, clearPrefixesInSubQuery);
  
        
        return result;
    }

    /**
     * Scans the query for all occurrences of URI nodes and returns the applicable subset of its
     * prefix mapping.
     *
     * Note: In principle sub queries may define their own prefixes
     *
     * <pre>
     * {@code
     * PREFIX foo: <http://ex.org/foo/>
     * SELECT * {
     *   {
     *     PREFIX foo2: <http://ex.org/foo/>
     *     SELECT * { foo2:a ... }
     *   }
     * }
     * }
     * </pre>
     *
     * This method ignores 'inner' prefixes, so for the example above, the method will
     * incorrectly return foo as a used prefix.
     *
     * @param query
     * @return
     */
    public static PrefixMapping usedPrefixes(Query query) {
        NodeTransformCollectNodes nodeUsageCollector = new NodeTransformCollectNodes();

        applyNodeTransform(query, nodeUsageCollector);
        Set<Node> nodes = nodeUsageCollector.getNodes();

        PrefixMapping pm = query.getPrefixMapping();
        Map<String, String> usedPrefixes = nodes.stream()
                .filter(Node::isURI)
                .map(Node::getURI)
                .map(x -> {
                    String tmp = pm.shortForm(x);
                    String r = Objects.equals(x, tmp) ? null : tmp.split(":", 2)[0];
                    return r;
                })
                //.peek(System.out::println)
                .filter(x -> x != null)
                .distinct()
                .collect(Collectors.toMap(x -> x, pm::getNsPrefixURI));

        PrefixMapping result = new PrefixMappingImpl();
        result.setNsPrefixes(usedPrefixes);
        return result;
    }

    public static Query randomizeVars(Query query) {
        Map<Var, Var> varMap = createRandomVarMap(query, "rv");
        Query result = org.aksw.jena_sparql_api.backports.syntaxtransform.QueryTransformOps.transform(query, varMap);
        //System.out.println(query + "now:\n" + result);
        return result;
    }

    public static Map<Var, Var> createRandomVarMap(Query query, String base) {
        Collection<Var> vars = PatternVars.vars(query.getQueryPattern());
        Generator<Var> gen = VarGeneratorBlacklist.create(base, vars);

        Map<Var, Var> varMap = vars.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        v -> gen.next()));

        return varMap;
    }

//	public static Query applyVarMap(Query query, Map<Var, ? extends Node> varMap) {
////		Map<Var, Node> tmp = varMap.entrySet().stream()
////				.collect(Collectors.toMap(
////						e -> e.getKey(),
////						e -> (Node)e.getValue()));
//
//		Query result = QueryTransformOps.transform(query, varMap);
//        return result;
//	}


    public static void injectFilter(Query query, String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        injectFilter(query, expr);
    }

    public static void injectFilter(Query query, Expr expr) {
        injectElement(query, new ElementFilter(expr));
    }

    // public static void injectElement(Query query, String elementStr) {
    // ElementUtils.pa
    // }


    public static void injectElement(Query query, Element element) {
        Element queryPattern = query.getQueryPattern();
        Element replacement = ElementUtils.mergeElements(queryPattern, element);
        query.setQueryPattern(replacement);
    }


    public static Range<Long> toRange(OpSlice op) {
        Range<Long> result = toRange(op.getStart(), op.getLength());
        return result;
    }

    public static Op applyRange(Op op, Range<Long> range) {
        long start = rangeToOffset(range);
        long length = rangeToLimit(range);

        Op result = start == Query.NOLIMIT && length == Query.NOLIMIT
                ? op
                : new OpSlice(op, start, length);
        return result;
    }

    /**
     * Limit the query to the given range, relative to its own given range
     *
     * @param query
     * @param offset
     * @param limit
     * @param cloneOnChange
     * @return
     */
    public static Query applySlice(Query query, Long offset, Long limit, boolean cloneOnChange) {
        Range<Long> parent = toRange(query);
        Range<Long> child = toRange(offset, limit);
        Range<Long> subRange = subRange(parent, child);

        boolean isUnchanged =
                parent.lowerEndpoint().equals(subRange.lowerEndpoint()) &&
                parent.hasUpperBound() == subRange.hasUpperBound() &&
                (parent.hasUpperBound() ? parent.upperEndpoint().equals(subRange.upperEndpoint()) : true);

        boolean hasChanged = !isUnchanged;

        Query result = cloneOnChange && hasChanged ? query.cloneQuery() : query;

        if(hasChanged) {
            applyRange(result, subRange);
        }

        return result;

    }

    public static void applyRange(Query query, Range<Long> range) {
        long offset = rangeToOffset(range);
        long limit = rangeToLimit(range);

        query.setOffset(offset);
        query.setLimit(limit);
    }

    public static Range<Long> createRange(Long limit, Long offset) {
        long beginIndex = offset == null ? 0 : offset;
        Long endIndex = limit == null ? null : beginIndex + limit;

        Range<Long> result = endIndex == null
                ? Range.atLeast(beginIndex)
                : Range.closedOpen(beginIndex, endIndex)
                ;

        return result;
    }

    //public static LimitAndOffset rangeToLimitAndOffset(Range<Long> range)

    public static long rangeToOffset(Range<Long> range) {
        long result = range == null || !range.hasLowerBound() ? 0 : range.lowerEndpoint();

        result = result == 0 ? Query.NOLIMIT : result;
        return result;
    }

    /**
     *
     * @param range
     * @return
     */
    public static long rangeToLimit(Range<Long> range) {
        range = range == null ? null : range.canonical(DiscreteDomain.longs());

        long result = range == null || !range.hasUpperBound()
            ? Query.NOLIMIT
            : DiscreteDomain.longs().distance(range.lowerEndpoint(), range.upperEndpoint());

        return result;
    }

    public static Range<Long> toRange(Query query) {
        Range<Long> result = toRange(query.getOffset(), query.getLimit());
        return result;
    }

    public static Range<Long> toRange(Long offset, Long limit) {
        Long min = offset == null || offset.equals(Query.NOLIMIT) ? 0 : offset;
        Long delta = limit == null || limit.equals(Query.NOLIMIT) ? null : limit;
        Long max = delta == null ? null : min + delta;

        Range<Long> result = max == null
                ? Range.atLeast(min)
                : Range.closedOpen(min, max);

        return result;
    }

    public static Range<Long> subRange(Range<Long> parent, Range<Long> child) {
        long newMin = parent.lowerEndpoint() + child.lowerEndpoint();

        Long newMax = (parent.hasUpperBound()
            ? child.hasUpperBound()
                ? (Long)Math.min(parent.upperEndpoint(), child.upperEndpoint())
                : parent.upperEndpoint()
            : child.hasUpperBound()
                ? (Long)child.upperEndpoint()
                : null);

        Range<Long> result = newMax == null
                ? Range.atLeast(newMin)
                : Range.closed(newMin, newMax);

        return result;
    }

    public static void applyDatasetDescription(Query query,
            DatasetDescription dd) {
        DatasetDescription present = query.getDatasetDescription();
        if (present == null && dd != null) {
            {
                List<String> items = dd.getDefaultGraphURIs();
                if (items != null) {
                    for (String item : items) {
                        query.addGraphURI(item);
                    }
                }
            }

            {
                List<String> items = dd.getNamedGraphURIs();
                if (items != null) {
                    for (String item : items) {
                        query.addNamedGraphURI(item);
                    }
                }
            }
        }
    }

    public static Query fixVarNames(Query query) {
        Query result = query.cloneQuery();

        Element element = query.getQueryPattern();
        Element repl = ElementUtils.fixVarNames(element);

        result.setQueryPattern(repl);
        return result;
    }

    /**
     *
     *
     * @param pattern
     *            a pattern of a where-clause
     * @param resultVar
     *            an optional result variable (used for describe queries)
     * @return
     */
    public static Query elementToQuery(Element pattern, String resultVar) {

        if (pattern == null)
            return null;
        Query query = new Query();
        query.setQueryPattern(pattern);
        query.setQuerySelectType();

        if (resultVar == null) {
            query.setQueryResultStar(true);
        }

        query.setResultVars();

        if (resultVar != null) {
            query.getResultVars().add(resultVar);
        }

        return query;

    }

    public static Query elementToQuery(Element pattern) {
        return elementToQuery(pattern, null);
    }

    /**
     * This method does basically the same as
     * org.apache.jena.sparql.engine.QueryExecutionBase.execConstruct and
     * SparqlerBaseSelect note sure if it is redundant
     *
     * @param quads
     * @param binding
     * @return
     */
    public static Set<Quad> instanciate(Iterable<Quad> quads, Binding binding) {
        Set<Quad> result = new HashSet<Quad>();
        Node nodes[] = new Node[4];
        for (Quad quad : quads) {
            for (int i = 0; i < 4; ++i) {
                Node node = QuadUtils.getNode(quad, i);

                // If the node is a variable, then substitute it's value
                if (node.isVariable()) {
                    node = binding.get((Var) node);
                }

                // If the node is null, or any non-object position
                // gets assigned a literal then we cannot instanciate
                if (node == null || (i < 3 && node.isLiteral())) {
                    result.clear();
                    return result;
                }

                nodes[i] = node;
            }

            Quad inst = QuadUtils.create(nodes);
            result.add(inst);
        }

        return result;
    }

}
