package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.backports.syntaxtransform.QueryTransformOps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class QueryUtils {

    public static Query randomizeVars(Query query) {
        Map<Var, Var> varMap = createRandomVarMap(query, "rv");
        Query result = QueryTransformOps.transform(query, varMap);
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
        long result = range == null ? 0 : range.lowerEndpoint();

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
