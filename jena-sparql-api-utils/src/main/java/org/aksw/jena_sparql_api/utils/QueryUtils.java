package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ExprTransformNodeElement;
import org.aksw.jena_sparql_api.util.iri.PrefixUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryShallowCopyWithPresetPrefixes;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.PrefixMapping2;

import com.google.common.collect.BoundType;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class QueryUtils {

    public static Query applyOpTransform(Query beforeQuery, Function<? super Op, ? extends Op> transform) {
        Op beforeOp = Algebra.compile(beforeQuery);
        Op afterOp = transform.apply(beforeOp);

        //Set<Var> afterOpVars = OpVars.visibleVars(afterOp);
//		Op op = NodeTransformLib.transform(new NodeTransformBNodesToVariables(), afterOp);

        Collection<Var> mentionedVars = OpVars.mentionedVars(beforeOp);
        Query afterQueryTmp = OpAsQuery.asQuery(afterOp);
//		Query afterQuery = fixVarNames(afterQueryTmp);

        Generator<Var> vargen = VarGeneratorBlacklist.create(mentionedVars);
        Element eltBefore = afterQueryTmp.getQueryPattern();

        // Fix blank nodes introduced as graph names by e.g. Algebra.unionDefaultGraph
        Element eltAfter = org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer.transform(eltBefore, new ElementTransformCopyBase() {
            protected Map<Node, Var> map = new HashMap<>();

            @Override
            public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
                Element result;
                if(gn.isBlank() || (gn.isVariable() && gn.getName().startsWith("?"))) {
                    Var v = map.get(gn);
                    if(v == null) {
                        v = vargen.next();
                        map.put(gn, v);
                    }
                    result = new ElementNamedGraph(v, elt1);
                } else {
                    result = super.transform(el, gn, elt1);
                }
                return result;
            }
        });
        afterQueryTmp.setQueryPattern(eltAfter);

        Query result = QueryUtils.restoreQueryForm(afterQueryTmp, beforeQuery);

        return result;
    }

// Seems like Query.getResultVars already does what I wanted to do here
//	public Set<Var> visibleVars(Query query) {
//		Set<Var> result;
//		if(query.isQueryResultStar()) {
//			Op op = Algebra.compile(query);
//			result = OpVars.visibleVars(op);
//		} else {
//			query.getPro
//		}
//	}

    /**
     * Restore a query form from a prototype.
     * Typical use case is when a CONSTRUCT query should be restored after a
     * it was compiled using Algebra.compile() (which discards the template part).
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

            Set<Var> expectedVars = new LinkedHashSet<>(proto.getProjectVars());
            VarExprList replacement = new VarExprList();

            Set<Var> actualVars = new LinkedHashSet<>(result.getProjectVars());

            Set<Var> missingVars = Sets.difference(expectedVars, actualVars);
            Set<Var> exceedingVars = Sets.difference(actualVars, expectedVars);
            if(!missingVars.isEmpty()) {
                throw new RuntimeException("Missing vars: " + missingVars + ", expected: " + expectedVars + ", actual: " + actualVars);
            }

            if(!exceedingVars.isEmpty()) {
                VarExprList actual = result.getProject();
                for(Var expectedVar : expectedVars) {
                    Expr expr = actual.getExpr(expectedVar);
                    VarExprListUtils.add(replacement, expectedVar, expr);
                }

                VarExprListUtils.replace(result.getProject(), replacement);
                result.setQueryResultStar(false);
                result.setResultVars();
            }
            break;
        case Query.QueryTypeConstruct:
            // If the projection uses expressions, create a sub query
            result = selectToConstruct(query, proto.getConstructTemplate());
            break;
        case Query.QueryTypeAsk:
            result = query.cloneQuery();
            result.setQueryAskType();
            break;
        case Query.QueryTypeDescribe:
            result = query.cloneQuery();
            result.setQueryDescribeType();
            for(Node node : proto.getResultURIs()) {
                result.addDescribeNode(node);
            }
            for(Var var : proto.getProjectVars()) {
                result.addDescribeNode(var);
            }
            break;
        case Query.QueryTypeJson:
            result = query.cloneQuery();
            result.setQueryJsonType();
            proto.getJsonMapping().entrySet()
                .forEach(e -> result.addJsonMapping(e.getKey(), e.getValue()));
            break;

        default:
            throw new RuntimeException("unsupported query type");
            //proto.result
        }

        result.setSyntax(proto.getSyntax());
        result.setPrefixMapping(proto.getPrefixMapping());


        // TODO We may want to move (named) graph URI copying to a separate function
//		result.getGraphURIs().addAll(proto.getGraphURIs());
//		result.getNamedGraphURIs().addAll(proto.getNamedGraphURIs());

        return result;
    }


    /**
     * Combine multiple construct queries into a single query whose
     * template and query pattern is the union of those of the provided queries
     * This method does NOT perform any renaming of variables.
     *
     *
     * @param queries
     * @return
     */
    public static Query unionConstruct(Query ... queries) {
        return unionConstruct(Arrays.asList(queries));
    }

    /**
     * Combine multiple construct queries into a single query whose
     * template and query pattern is the union of those of the provided queries
     * This method does NOT perform any renaming of variables.
     *
     *
     * @param queries
     * @return
     */
    public static Query unionConstruct(Iterable<Query> queries) {
        Query result = new Query();

        // BasicPatten bgp = new BasicPattern();
        Set<Quad> quadPatterns = new LinkedHashSet<>();
        Set<Element> elements = new LinkedHashSet<>();

        for (Query query : queries) {
            result.getPrefixMapping().setNsPrefixes(query.getPrefixMapping());

            Template tmp = query.getConstructTemplate();

            quadPatterns.addAll(tmp.getQuads());
            elements.add(query.getQueryPattern());
        }

        result.setQueryConstructType();
        result.setConstructTemplate(new Template(new QuadAcc(new ArrayList<>(quadPatterns))));
        result.setQueryPattern(ElementUtils.unionIfNeeded(elements));

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
        Query afterQuery = OpAsQuery.asQuery(afterOp);
        afterQuery.getPrefixMapping().setNsPrefixes(beforeQuery.getPrefixMapping());

        Query result = restoreQueryForm(afterQuery, beforeQuery);
//		if(beforeQuery.isConstructType()) {
//			result.setQueryConstructType();
//			Template template = beforeQuery.getConstructTemplate();
//			result.setConstructTemplate(template);
//		}

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
        boolean result = true;
        result = result && !q.hasAggregators();
        result = result && !q.hasGroupBy();
        result = result && !q.hasValues();
        result = !q.hasHaving();
        result = result && !VarExprListUtils.hasExprs(q.getProject());

        return result;
    }

    public static Set<Var> mentionedVars(Query query) {
        Set<Node> nodes = mentionedNodes(query);
        Set<Var> result = NodeUtils.getVarsMentioned(nodes);
        return result;
    }

    public static Set<Node> mentionedNodes(Query query) {
        NodeTransformCollectNodes xform = new NodeTransformCollectNodes();
        QueryUtils.applyNodeTransform(query, xform);
        Set<Node> result = xform.getNodes();
        return result;
    }

    public static Var freshVar(Query query) {
        Var result = freshVar(query, null);
        return result;
    }

    public static Var freshVar(Query query, String baseVarName) {
        baseVarName = baseVarName == null ? "c" : baseVarName;

        Set<Var> varsMentioned = mentionedVars(query);

        Generator<Var> varGen = VarUtils.createVarGen(baseVarName, varsMentioned);
        Var result = varGen.next();

        return result;
    }



    /**
     * Transform json mapping obtained via Query.getJsonMapping
     * TODO Actually this should be added to Jena's NodeTransformLib.
     *
     * @param jsonMapping
     * @param nodeTransform
     * @return
     */
    public static Map<String, Node> applyNodeTransform(Map<String, Node> jsonMapping, NodeTransform nodeTransform) {
        Map<String, Node> result = jsonMapping.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> nodeTransform.apply(e.getValue())));
        return result;
    }

    public static Query applyNodeTransform(Query query, NodeTransform nodeTransform) {

        ElementTransform eltrans = new ElementTransformSubst2(nodeTransform) ;
        //NodeTransform nodeTransform = new NodeTransformSubst(nodeTransform) ;
        ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans);

        Template template = null;
        if(query.isConstructType()) {
            Template tmp = query.getConstructTemplate();
            if(tmp.containsRealQuad()) {
                QuadPattern before = QuadPatternUtils.create(tmp.getQuads());
//        	BasicPattern before = tmp.getBGP();
                QuadPattern after = NodeTransformLib.transform(nodeTransform, before);
                template = new Template(new QuadAcc(after.getList()));
            } else {
                BasicPattern before = tmp.getBGP();
                BasicPattern after = NodeTransformLib.transform(nodeTransform, before);
                template = new Template(after);
            }
        }

        Map<String, Node> jsonMapping = null;
        if(query.isJsonType()) {
             Map<String, Node> before = query.getJsonMapping();
            jsonMapping = applyNodeTransform(before, nodeTransform);
        }


        //Query result = org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps.transform(query, eltrans, exprTrans) ;
        Query result = org.aksw.jena_sparql_api.backports.syntaxtransform.QueryTransformOps.transform(query, eltrans, exprTrans) ;

        // QueryTransformOps creates a shallow copy of the query which causes problems
        // if a PrefixMapping2 is used; the PM2 is materialized into a PM
        // Fix prefixes in sub queries by clearing them
        Element resultEl = result.getQueryPattern();
        if(resultEl != null) {
            ElementWalker.walk(resultEl, new ElementVisitorBase() {
                @Override
                public void visit(ElementSubQuery el) {
                    el.getQuery().getPrefixMapping().clearNsPrefixMap();
                }
            });
        }

        if(template != null) {
            result.setQueryConstructType();
            result.setConstructTemplate(template);
        }

        if(jsonMapping != null) {
            result.setQueryJsonType();
            jsonMapping.entrySet()
                .forEach(e -> result.addJsonMapping(e.getKey(), e.getValue()));
        }

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
     * Determines the used prefixes w.r.t the query's local prefixes and
     * a global prefix map (may be null).
     * The local prefixes take precedence.
     *
     * @param query
     * @param global
     * @return
     */
    public static PrefixMapping usedPrefixes(Query query, PrefixMapping global) {
        PrefixMapping local = query.getPrefixMapping();
        PrefixMapping pm = global == null ? local : new PrefixMapping2(global, local);
        PrefixMapping result = usedReferencePrefixes(query, pm);
        return result;
    }

    /**
     * Scans the query for all occurrences of URI nodes and returns the applicable subset of its
     * prefix mapping.
     *
     * Note: In principle Jena allows sub queries to define their own prefixes
     * However, this is non-standard and jena raises syntax exception when trying to clone such a query
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
     * This method ignores non-standard 'inner' prefixes, so for the example above, the method will
     * "incorrectly" return foo as a used prefix.
     *
     * @param query
     * @return
     */
    public static PrefixMapping usedPrefixes(Query query) {
        PrefixMapping result = usedPrefixes(query, null);
        return result;
    }

    /**
     * Determine used prefixes within the given prefix mapping.
     * The query's own prefixes are ignored.
     *
     * @param query
     * @param pm
     * @return
     */
    public static PrefixMapping usedReferencePrefixes(Query query, PrefixMapping pm) {
        NodeTransformCollectNodes nodeUsageCollector = new NodeTransformCollectNodes();

        Query shallowCopy = QueryShallowCopyWithPresetPrefixes.shallowCopy(query, null);

        applyNodeTransform(shallowCopy, nodeUsageCollector);
        Set<Node> nodes = nodeUsageCollector.getNodes();

        PrefixMapping result = PrefixUtils.usedPrefixes(pm, nodes);
        return result;
    }

    /**
     * In-place optimize a query's prefixes to only used prefixes
     *
     * @param query
     * @param pm
     * @return
     */
    public static Query optimizePrefixes(Query query, PrefixMapping globalPm) {
        PrefixMapping usedPrefixes = QueryUtils.usedPrefixes(query, globalPm);
        query.setPrefixMapping(usedPrefixes);
        return query;
    }

    public static Query optimizePrefixes(Query query) {
        optimizePrefixes(query, null);
        return query;
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
     * Transform a range w.r.t. a discrete domain such that any lower bound is closed and the upper bound
     * is open. As a result, a zero-length range is represented by [x..x)
     *
     * @param <T>
     * @param range
     * @param domain
     * @return
     */
    public static <T extends Comparable<T>> Range<T> makeClosedOpen(Range<T> range, DiscreteDomain<T> domain) {
        ContiguousSet<T> set = ContiguousSet.create(range, domain);

        Range<T> result = set.isEmpty()
                ? Range.closedOpen(range.lowerEndpoint(), range.lowerEndpoint())
                : ContiguousSet.create(range, domain).range(BoundType.CLOSED, BoundType.OPEN);
//
//        T lower = closedLowerEndpointOrNull(range, domain);
//        T upper = openUpperEndpointOrNull(range, domain);
//
//        Range<T> result = lower == null
//                ? upper == null
//                    ? Range.all()
//                    : Range.upTo(upper, BoundType.OPEN)
//                : upper == null
//                    ? Range.atLeast(lower)
//                    : Range.closedOpen(lower, upper);

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

        boolean isUnchanged = subRange.equals(parent);
//                parent.lowerEndpoint().equals(subRange.lowerEndpoint()) &&
//                parent.hasUpperBound() == subRange.hasUpperBound() &&
//                (parent.hasUpperBound() ? parent.upperEndpoint().equals(subRange.upperEndpoint()) : true);

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

    public static <T extends Comparable<T>> T closedLowerEndpointOrNull(Range<T> range, DiscreteDomain<T> domain) {
        T result = !range.hasLowerBound()
                ? null
                : range.lowerBoundType().equals(BoundType.CLOSED)
                    ? range.lowerEndpoint()
                    : domain.next(range.lowerEndpoint());

        return result;
    }

    public static <T extends Comparable<T>> T openUpperEndpointOrNull(Range<T> range, DiscreteDomain<T> domain) {
        T result = !range.hasUpperBound()
                ? null
                : range.upperBoundType().equals(BoundType.CLOSED)
                    ? domain.next(range.upperEndpoint())
                    : range.upperEndpoint();

        return result;
    }


    public static long rangeToOffset(Range<Long> range) {
        Long tmp = range == null
                ? null
                : closedLowerEndpointOrNull(range, DiscreteDomain.longs());

        long result = tmp == null || tmp == 0 ? Query.NOLIMIT : tmp;
        return result;
    }

    /**
     *
     * @param range
     * @return
     */
    public static long rangeToLimit(Range<Long> range) {
        range = range == null ? null : makeClosedOpen(range, DiscreteDomain.longs());

        long result = range == null || !range.hasUpperBound()
            ? Query.NOLIMIT
            : DiscreteDomain.longs().distance(range.lowerEndpoint(), range.upperEndpoint())
                // If the upper bound is closed such as [x, x] then the result is the distance plus 1
                + (range.upperBoundType().equals(BoundType.CLOSED) ? 1 : 0);

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

    /**
     * Returns the absolute range for a child range relative to a parent range
     * Assumes that both ranges have a lower endpoint
     *
     * @param _parent
     * @param _child
     * @return
     */
    public static Range<Long> subRange(Range<Long> _parent, Range<Long> _child) {
        Range<Long> parent = makeClosedOpen(_parent, DiscreteDomain.longs());
        Range<Long> child = makeClosedOpen(_child, DiscreteDomain.longs());

        Range<Long> shiftedChild = org.aksw.commons.util.range.RangeUtils.transform(child, e -> e + parent.lowerEndpoint());

        Range<Long> result = shiftedChild.intersection(parent);
//        long newMin = parent.lowerEndpoint() + child.lowerEndpoint();
//
//        Long newMax = (parent.hasUpperBound()
//            ? child.hasUpperBound()
//                ? (Long)Math.min(parent.upperEndpoint(), newMin + child.upperEndpoint())
//                : parent.upperEndpoint()
//            : child.hasUpperBound()
//                ? newMin + (Long)child.upperEndpoint()
//                : null);
//
//        Range<Long> result = newMax == null
//                ? Range.atLeast(newMin)
//                : Range.closedOpen(newMin, newMax);
//
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

        Element cleanElement = pattern instanceof ElementGroup || pattern instanceof ElementSubQuery
                ? pattern
                : ElementUtils.createElementGroup(pattern);

        query.setQueryPattern(cleanElement);
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
