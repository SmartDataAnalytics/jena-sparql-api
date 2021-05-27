package org.aksw.jena_sparql_api.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.ext.com.google.common.collect.SetMultimap;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;

public class QueryGenerationUtils {

    /**
     * A commonly needed query transformation needed for virtuoso: Using
     * order by with limit and/or offset fails for non-small limits/offsets.
     *
     * The solution implemented by this method is to rewrite a query such that a sub-query
     * does the order by and an outer query applies limit / offset:
     *
     *   SELECT { } OFFSET foo ORDER BY bar
     * becomes
     *   SELECT * { SELECT { } ORDER BY bar } OFFSET foo
     */
    public static Query virtuosoFixForOrderedSlicing(Query query) {
        long o = query.getOffset();
        long l = query.getLimit();

        boolean hasLimitOrOffset = o != Query.NOLIMIT || l != Query.NOLIMIT;
        boolean hasOrderBy = query.hasOrderBy();

        Query result;
        if(query.isConstructType() || hasLimitOrOffset && hasOrderBy) {
            Query clone = query.cloneQuery();
            clone.setQuerySelectType();
            clone.setOffset(Query.NOLIMIT);
            clone.setLimit(Query.NOLIMIT);

            result = new Query();
            result.setQuerySelectType();
//            result.setPrefixMapping(query.getPrefixMapping());
            result = QueryUtils.restoreQueryForm(result, query);
            result.setQueryPattern(new ElementSubQuery(clone));
            clone.getPrefixMapping().clearNsPrefixMap();

            result.setOffset(o);
            result.setLimit(l);
        } else {
            result = query;
        }

        return result;
    }


    public static Query createQueryQuad(Quad quad) {
        Query query = new Query();
        query.setQuerySelectType();

        Node g = quad.getGraph();
        Node s = quad.getSubject();
        Node p = quad.getPredicate();
        Node o = quad.getObject();

        s = g == null || g.equals(Node.ANY) ? Vars.g : g;
        s = s == null || s.equals(Node.ANY) ? Vars.s : s;
        p = p == null || p.equals(Node.ANY) ? Vars.p : p;
        o = o == null || o.equals(Node.ANY) ? Vars.o : o;

        Triple triple = new Triple(s, p, o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        Element element = new ElementTriplesBlock(bgp);

        element = new ElementNamedGraph(g, element);

        query.setQueryPattern(element);
        return query;
    }

    public static Query createQueryTriple(Triple m) {
        Query query = new Query();
        query.setQueryConstructType();

        /*
        Node s = m.getMatchSubject();
        Node p = m.getMatchPredicate();
        Node o = m.getMatchObject();
        */
        Node s = m.getSubject();
        Node p = m.getPredicate();
        Node o = m.getObject();

        s = s == null || s.equals(Node.ANY) ? Vars.s : s;
        p = p == null || p.equals(Node.ANY) ? Vars.p : p;
        o = o == null || o.equals(Node.ANY) ? Vars.o : o;

        Triple triple = new Triple(s, p, o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        Template template = new Template(bgp);
        Element element = new ElementTriplesBlock(bgp);

        query.setConstructTemplate(template);
        query.setQueryPattern(element);
        return query;
    }

    // Util for cerateQueryCount
    public static Query wrapAsSubQuery(Query query, Var v) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(v);
        result.setQueryPattern(esq);

        return result;
    }

    public static Query wrapAsSubQuery(Query query) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryResultStar(true);
        result.setQueryPattern(esq);

        return result;
    }


    /**
     * Given a query derives a new one that counts the bindings of the original one's graph pattern
     *
     * @param query
     * @return
     */
    public static Entry<Var, Query> createQueryCount(Query query) {
        return createQueryCount(query, null, null);
    }

    /**
     * Count the number of distinct binding for the given variables. If null, all variables are considered.
     *
     * For SELECT queries: SELECT COUNT(*) { SELECT DISTINCT partitionVars { original-select-query } }
     * For CONSTRUCT queries: SELECT COUNT(*) { SELECT DISTINCT partitionVars { query pattern } }
     *
     *
     * @param query
     * @param partitionVars
     * @param rowLimit
     * @param itemLimit
     * @return
     */
    public static Entry<Var, Query> createQueryCountPartition(Query query, Collection<Var> partitionVars, Long itemLimit, Long rowLimit) {
        if(partitionVars != null && partitionVars.isEmpty()) {
            throw new IllegalArgumentException("Empty collection of variables for which to count bindings not permitted.");
        }

        // TODO Validate that the partitionVars are actually distinguished variables of the query

        Query clone = query.cloneQuery();

        if(clone.isConstructType()) {
            // Try to count the distinct number of template instantiations - i.e.
            // the number of distinct bindings involving the variables
            // mentioned in the template
            Template template = clone.getConstructTemplate();
            Set<Var> vars = partitionVars == null
                    ? QuadPatternUtils.getVarsMentioned(template.getQuads())
                    : new LinkedHashSet<>(partitionVars);

            clone.setQuerySelectType();

//            System.out.println("Clone: " + clone);
            if(vars.isEmpty()) {
                //query.setQueryResultStar(true);
                // TODO The distinct number of template instantiations if there is no var in a template is
                // at most 1
                throw new RuntimeException("Variables required for counting");
            } else {
                clone.setQueryResultStar(false);
                clone.addProjectVars(vars);
                clone.setDistinct(true);
            }
//            System.out.println("Clone2: " + clone);
        } else {
            // TODO We need to check whether the partition variables are mapped to expressions in the projection
            Set<Var> allowedVars = partitionVars == null
                    ? new LinkedHashSet<>(clone.getProjectVars())
                    : new LinkedHashSet<>(partitionVars);

            removeNonProjectedVars(clone, allowedVars);
            // Remove all non-projected variables
//            List<Var> presentVars = new ArrayList<>(clone.getProjectVars());
//
//            for(Var v : presentVars) {
//                if(!allowedVars.contains(v)) {
//                    clone.getProject().remove(v);
//                }
//            }

        }

        Entry<Var, Query> result = createQueryCountCore(clone, itemLimit, rowLimit);
        return result;
    }

    public static Entry<Var, Query> createQueryCount(Query query, Long itemLimit, Long rowLimit) {
        Entry<Var, Query> result = createQueryCountPartition(query, null, itemLimit, rowLimit);
        return result;
    }

//    public static Entry<Var, Query> createQueryCount(Query query, Long itemLimit, Long rowLimit) {
//    	query = query.cloneQuery();
//
//    	if(query.isConstructType()) {
//        	Template template = query.getConstructTemplate();
//        	Set<Var> vars = QuadPatternUtils.getVarsMentioned(template.getQuads());
//        	// TODO Vars may be empty, in case we deal with a partitioned query
//
//        	query.setQuerySelectType();
//        	if(vars.isEmpty()) {
//        		query.setQueryResultStar(true);
//        	} else {
//	        	query.addProjectVars(vars);
//        	}
//        }
//
//    	Entry<Var, Query> result = createQueryCountCore(query, itemLimit, rowLimit);
//    	return result;
//    }


    public static boolean everyNeedleIsInAnyHaystack(Collection<? extends Collection<?>> haystacks, Collection<?> needles) {
        boolean result =
            needles.stream()
                .allMatch(needle -> anyHaystackHasTheNeedle(haystacks, needle));

        return result;
    }

    public static boolean anyHaystackHasTheNeedle(Collection<? extends Collection<?>> haystacks, Object needle) {
        boolean result = haystacks.stream()
            .anyMatch(haystack -> haystack.contains(needle));

        return result;
    }

    public static boolean everyHaystackHasAnyNeedle(Collection<? extends Collection<?>> haystacks, Collection<?> needles) {
        boolean result =
            haystacks.stream()
                .allMatch(haystack -> haystackHasAnyNeedle(haystack, needles));

        return result;
    }

    public static boolean haystackHasAnyNeedle(Collection<?> haystack, Collection<?> needles) {
        boolean result = needles.stream()
            .anyMatch(needle -> haystack.contains(needle));
        return result;
    }


    /**
     * Ensure that the query's result bindings are unique.
     * In the simplest case just adds DISTINT to the projection
     * but may do nothing if it is determined that the current projection already yields unique bindings.
     * This is the case if a super set of a group by's expression is projected.
     *
     * @param query
     * @return
     */
    public static Query distinct(Query query) {
        Query clone = query.cloneQuery();

        if (!clone.isSelectType()) {
            throw new IllegalArgumentException("Query form must be of select type");
        }

        optimizeGroupByToDistinct(clone);

        // If the projection is not a super set of the group keys we need to wrap

        // TODO This repeats a part of the group by computation - consolidate?
        Set<Var> projVars = SetUtils.asSet(clone.getProjectVars());

        Set<Set<Var>> distinctVarSets = analyzeDistinctVarSets(clone);

        if(query.hasGroupBy()) {
            boolean areAllDistinctVarsRequested = !distinctVarSets.isEmpty() && everyHaystackHasAnyNeedle(distinctVarSets, projVars);

            if (!areAllDistinctVarsRequested) {
                // Wrap
                Query tmp = wrapAsSubQuery(clone);
                tmp.setQueryResultStar(false);
                tmp.addProjectVars(projVars);
                tmp.setDistinct(true);

                clone = tmp;
            }
        } else {
//            // Preserve GROUP BY - disable superfluous distinct if it was present before
//            if(query.isDistinct()) {
                clone.setDistinct(true);
//                modified = true;
//            }
        }

        return clone;
    }

    public static Query project(Query query, Collection<Var> resultVars) {
        if (!query.isSelectType()) {
            throw new IllegalArgumentException("Query form must be of select type");
        }

        Query clone = query.cloneQuery();

        // Check that all result vars are visible
        Set<Var> projVars = new HashSet<>(query.getProjectVars());
        Set<Var> violations = Sets.difference(SetUtils.asSet(resultVars), projVars);

        if (!violations.isEmpty()) {
            throw new RuntimeException("Cannot project variables that are not mentioned in the query; violations: " + violations);
        }

        removeNonProjectedVars(clone, resultVars);

        optimizeGroupByToDistinct(clone);
        return clone;
    }

    /**
     * Return the original query or a copy of it depending on
     * Update a query in place to project only the given variables.
     *
     * If distinct == true: Will not apply distinct if the result for the underlying variables is already distinct, e.g.
     *
     * applying 'DISTINCT ?s ?p ?c' to 'SELECT (expr1 AS ?s) (expr2 AS ?p) (FOO AS ?c) { } GROUP BY expr1 expr2
     * does not have to wrap the underlying query, as ?s ?p ?c is already distinct (in fact ?s ?p is already distinct)
     *
     * So the main contribution of this method is taking care of the indirection via the expressions.
     *
     *
     * @param clone
     * @param distinct
     * @param vars
     */
    public static Query project(Query query, boolean distinct, Collection<Var> vars) {
        Query proj = project(query, vars);
        Query dist = distinct(proj);
        return dist;
    }

//    public static Query project(Query query, boolean distinct, Collection<Var> vars) {
//        Query clone = query.cloneQuery();
//        Query result;
//
//        // Cases:
//        // 1. distinct requested
//        //   query has distinct (may be implicit via group by): wrap unless the same or a super set of the distinct vars are projected
//        //   query w/o distinct: remove non-requested vars from the query and set distinct flag
//        // 2. distinct NOT requested
//        //   query has distinct: wrap to project only non-requested vars ; omit wrapping if all vars are projected
//        //   query w/o distinct: remove non-requested vars from the query
//
//        // If exactly all groupBy exprs are projected, the group by can be replaced with distinct
//
//        Set<Set<Var>> distinctVarSets = analyzeDistinctVarSets(clone);
//
//        if (distinct) {
//            // TODO We may want to use our VarUsageAnalyzer
//
//            boolean areAllDistinctVarsRequested = everyHaystackHasAnyNeedle(distinctVarSets, vars);
//
//            if (areAllDistinctVarsRequested) {
//                // Remove non-projected vars
//                removeNonProjectedVars(clone, vars);
//
//                boolean onlyGroupByVarsProjected = everyNeedleIsInAnyHaystack(distinctVarSets, vars);
//
//                if (onlyGroupByVarsProjected) {
//                    clone.getGroupBy().clear();
//                    clone.getAggregators().clear();
//                    clone.setDistinct(true);
//                }
//                result = clone;
//            } else {
//                // If group by variables are projected we can remove group by and change it to DISTINCT
//                // Non-group by variables are mapped to aggregation expressions which need to be evaluated
//                //   hence, in this case we have to apply wrapping
//
//                boolean canDoDistinct = everyNeedleIsInAnyHaystack(distinctVarSets, vars);
//
//                if(canDoDistinct) {
//                    removeNonProjectedVars(clone, vars);
//                    clone.getGroupBy().clear();
//                    clone.getAggregators().clear();
//                    clone.setDistinct(true);
//
//                    result = clone;
//                } else {
//                    result = wrapAsSubQuery(clone);
//                    result.setQueryResultStar(false);
//                    result.addProjectVars(vars);
//                    result.setDistinct(true);
//                }
//            }
//
//        } else {
//            // If the query has distinct we need to wrap it unless *exactly* the distinct vars are projected
//            if(clone.isDistinct()) {
//                boolean areAllDistinctVarsRequested =
//                        everyHaystackHasAnyNeedle(distinctVarSets, vars) && everyNeedleIsInAnyHaystack(distinctVarSets, vars);
//
//                if(areAllDistinctVarsRequested) {
//                    result = clone;
//                } else {
//                    result = wrapAsSubQuery(clone);
//                    result.setQueryResultStar(false);
//                    result.addProjectVars(vars);
//                }
//
//            } else {
//                removeNonProjectedVars(clone, vars);
//                result = clone;
//            }
//
//        }
//
//        return result;
//    }

    public static void removeNonProjectedVars(Query query, Collection<Var> allowedVars) {
        VarExprList proj = query.getProject();
        Set<Var> projVars = new HashSet<>(proj.getVars());
        allowedVars = SetUtils.asSet(allowedVars);

        for (Var v : projVars) {
            if(!allowedVars.contains(v)) {
                proj.remove(v);
            }
        }
    }


    /**
     * Checks whether a query uses DISTINCT and/or GROUP BY.
     * If so then a multimap from grouping expression to projection variable is constructed and the for
     * the set of each expression's related set of variables is returned.
     *
     * e AS ?x, e AS ?y will yield a {{?x ?y}}
     *
     *
     * @param query
     * @return The set of distinct var combinations; empty if there are none
     */
    public static Set<Set<Var>> analyzeDistinctVarSets(Query query) {
        Set<Set<Var>> result = new LinkedHashSet<>();

        VarExprList vel = query.getProject();

        // DISTINCT is redundant if there is a GROUP BY (at least for SPARQL 1.1)
        // So if query.hasGroupBy is true we do not have to consider distinct
        // Conversely, if hasGroupBy is false then all variables mentioned in the projection become part of the result


        SetMultimap<Expr, Var> exprToProjVars = VarExprListUtils.invert(vel);
        Set<Expr> projExprs = exprToProjVars.keySet();

        if (query.hasGroupBy()) {

            VarExprList groupBy = query.getGroupBy();
            Multimap<Expr, Var> exprToGroupVars = VarExprListUtils.invert(groupBy);
            Set<Expr> groupByExprs = exprToGroupVars.keySet();

            Set<Expr> diff = Sets.difference(groupByExprs, projExprs);

            if (diff.isEmpty()) {
                for(Expr groupByExpr : groupByExprs) {
                    Set<Var> projVars = (Set<Var>)exprToProjVars.get(groupByExpr);
                    result.add(projVars);
                }
            }
        } else if (query.isDistinct()) {

            for(Collection<Var> e : exprToProjVars.asMap().values()) {
                Set<Var> projVars = (Set<Var>)e;
                result.add(projVars);
            }

        }


        return result;

    }

//    public static boolean isInjective(Var var, Expr expr) {
//
//    }

    /**
     * Remove superfluous aggregation or distinct
     *
     * Aggregation: If the original query uses aggregation and only projects the group expressions,
     * then it is rewritten as a distinct query:
     * SELECT [DISTINCT] (eg1 AS v1) ... (egn AS vn) { } GROUP BY eg1 ... egn
     *   becomes
     * SELECT DISTINCT (eg1 AS v1) ... (egn AS vn)
     *
     *
     * @param query
     * @param itemLimit
     * @param rowLimit
     * @return
     */
//    public static boolean optimizeAggregationToDistinct(Query query) {
//        boolean result = false;
//
//        VarExprList vel = query.getProject();
//        List<ExprAggregator> aggs = query.getAggregators();
//
//        Set<Expr> effectiveProjExprs = new LinkedHashSet<>();
//        for(Var v : vel.getVars()) {
//            Expr expr = vel.getExpr(v);
//            Expr effectiveExpr = expr == null ? new ExprVar(v) : expr;
//            effectiveProjExprs.add(effectiveExpr);
//        }
//
//        Set<Expr> effectiveAggExprs = aggs.stream()
//                .map(ExprAggregator::getExpr)
//                .collect(Collectors.toSet());
//
//        Set<Expr> diff = Sets.difference(effectiveAggExprs, effectiveProjExprs);
//        if(diff.isEmpty()) {
//            query.setDistinct(true);
//            query.getAggregators().clear();
//            query.getGroupBy().clear();
//            result = true;
//        }
//
//        return result;
//    }


    /**
     *
     *
     * If discardNonGroupByProjection is true than the result is
     * NOT an equivalence transformation but a cardinality-preserving transformation:
     *
     * The result set size of a query transformed
     * with this procedure is equivalent to that of the original one.
     *
     * Converts GROUP BY to DISTINCT if all group by expressions appear in the projection
     * The projection of any non group-by expressions is removed.
     *
     * - If _exactly_ the group keys are projected the query can be turned to DISTINCT
     * - Removes superfluous DISTINCT - i.e. SELECT DISTINCT ?groupKey ?derived WHERE { ... } GROUP BY ?groupKey:
     *   If a superset of the group keys are projected then distinct is not needed (as each binding is uniquely identified by the group keys)
     *
     * @param query
     */
    public static boolean optimizeGroupByToDistinct(Query query) {
        return optimizeGroupByToDistinct(query, false);
    }

    /**
     * Cardinality-preserving transformation for queries with group by where all group by expressions are projected:
     * SELECT ?groupKeys ?nonGroupKeys { ... } GROUP BY ?groupKeys becomes
     * SELECT DISTINCT ?groupKeys { ... }
     *
     *
     * @param query
     * @return
     */
    public static boolean discard(Query query) {
        return optimizeGroupByToDistinct(query, false);
    }

    /**
     * SELECT ?v1 ?n { } GROUP BY ?v1 ?vn ===> SELECT DISTINCT ?v1 ?vN { }
     *
     * If discardAggregators then the resulting query becomes one with the same number of bindings using distinct:
     * SELECT ?v COUNT(DISTINCT ?o) {} GROUP BY ?v ===> SELECT DISTINCT ?v {}
     *
     * @param query
     * @param discardAggregators Discard all aggregators if applicable. The result is a query with the same number of bindings.
     * @return true iff the query was modified
     */
    public static boolean optimizeGroupByToDistinct(Query query, boolean discardAggregators) {
        boolean modified = false;

        // If the query being counted has a group by then check whether it can be transformed
        // to distinct instead.
        // Any 'having' expression blocks this transformation
        if (query.hasGroupBy() && !query.hasHaving()) {

            Set<Set<Var>> distinctVarSets = analyzeDistinctVarSets(query);

            // if (!distinctVarSets.isEmpty() && discardNonGroupByProjection) {
            if (discardAggregators) {
                Set<Expr> groupByExprs = VarExprListUtils.invert(query.getGroupBy()).keySet();

                // Discard all variables from the projection that do not match the group keys
                // Conversely, add all group keys to the projection

                VarExprList proj = query.getProject();

                Set<Var> projGroupKeyVars = new HashSet<>();
                Set<Var> removals = new HashSet<>();
                for (Var projVar : new ArrayList<>(query.getProjectVars())) {
                    Expr projExpr = proj.getExprs().getOrDefault(projVar, new ExprVar(projVar));

                    boolean isGroupKeyVar = groupByExprs.contains(projExpr);
                    if (isGroupKeyVar) {
                        projGroupKeyVars.add(projVar);
                    } else {
                        removals.add(projVar);
                    }

                    //boolean isGroupKeyVar = anyHaystackHasTheNeedle(distinctVarSets, projVar);
//                    if(!isGroupKeyVar) {
//                    	removalCandidates.add(projVar);
//                        proj.remove(projVar);
//                    }
                }

                // Corner case: group by may be composed only of complex expressions (not just simple ExprVars)
                // and/or none of the group keys is projected - in that case keep the original projection
                if (!projGroupKeyVars.isEmpty()) {
                    for (Var v : removals) {
                        proj.remove(v);
                    }
                }

            }

            Set<Var> projVars = SetUtils.asSet(query.getProjectVars());

            boolean areAllDistinctVarsRequested = !distinctVarSets.isEmpty() && everyHaystackHasAnyNeedle(distinctVarSets, projVars);

            if (areAllDistinctVarsRequested) {
                boolean onlyGroupByVarsProjected = everyNeedleIsInAnyHaystack(distinctVarSets, projVars);

                if (onlyGroupByVarsProjected) {
                    query.getGroupBy().clear();
                    query.getAggregators().clear();
                    query.setDistinct(true);
                    modified = true;
                } else {
                    // Preserve GROUP BY - disable superfluous distinct if it was present before
                    if(query.isDistinct()) {
                        query.setDistinct(false);
                        modified = true;
                    }
                }

//                    for(Entry<Expr, Collection<Var>> e : exprToProjVars.asMap().entrySet()) {
//                        Expr projExpr = e.getKey();
//                        boolean isProjected = groupByExprs.contains(projExpr);
//                        if (!isProjected) {
//                            for(Var var : e.getValue()) {
//                                proj.remove(var);
//                            }
//                        }
//                    }

            }
        }


        return modified;
    }

    /**
     * Test whether the queries distinguished variables are the same as those of SELECT * { ... }
     * Example:
     * SELECT * { ?s a ?t } is effectively the same as SELECT ?t ?s { ?s a ?t }
     * Variable order does not matter
     *
     * @return
     */
    public static boolean isEffectiveQueryResultStar(Query query) {
        boolean result;
        if (query.isQueryResultStar()) {
            result = true;
        } else {
            Set<Var> projectVars = new HashSet<>(query.getProjectVars());
            Element elt = query.getQueryPattern();
            Set<Var> patternVars = elt == null ? Collections.emptySet() : new HashSet<>(PatternVars .vars(query.getQueryPattern()));
            result = Sets.symmetricDifference(projectVars, patternVars).isEmpty();
        }

        return result;
    }

    public static Entry<Var, Query> createQueryCountCore(Query rawQuery, Long itemLimit, Long rowLimit) {
        // Allocate a variable for the resulting count that is not mentioned in the query
        Var resultVar = QueryUtils.freshVar(rawQuery); // Vars.c;

        Query query = createQueryCountCore(resultVar, rawQuery, itemLimit, rowLimit);

        return Maps.immutableEntry(resultVar, query);
    }

    
    /**
     * Returns true if the query uses features that prevents it from being
     * represented as a pair of graph pattern + projection
     * 
     * @param query
     * @return
     */
    public static boolean needsWrappingByFeatures(Query query) {
    	return needsWrappingByFeatures(query, true);
    }
    
    /**
     * Similar to {@link #needsWrapping(Query)} but includes a flag
     * whether to include slice information (limit / offset).
     * 
     * @param query
     * @return
     */
    public static boolean needsWrappingByFeatures(Query query, boolean includeSlice) {
        boolean result
	        =  query.hasGroupBy()
	        || query.hasAggregators()
	        || query.hasHaving()
	        || query.hasValues();
        
        if (includeSlice) {
        	result = result
        		|| query.hasLimit()
        		|| query.hasOffset()
        		;        		
        }
	    
        // Order is ignored

        return result;
    }
    
    /**
     * A stricter version of {@link #needsWrappingByFeatures(Query)}.
     * Returns true if the query uses features that cannot be represented 
     * 
     * @param query
     * @return
     */
    public static boolean needsWrapping(Query query) {

    	boolean needsWrappingByFeatures = needsWrappingByFeatures(query, true);
    	
        boolean isDistinct = query.isDistinct();
        // If the query uses distinct and there is just a single projected variable
        // then we can transform 'DISTINCT ?s' to 'COUNT(DISTINCT ?s)'.
        // However, if there is more than 1 variable then we need wrapping in any case
        int projVarCount = query.getProjectVars().size();
        boolean isEffectiveQueryResultStar = isEffectiveQueryResultStar(query);

		boolean result
		        = needsWrappingByFeatures
		        || (isDistinct && projVarCount > 1 && !isEffectiveQueryResultStar )
		        ;

		return result;
    }
    
    /**
     * Transform a SELECT query such that it yields the count of matching solution bindings within the given constraints
     *
     * The rowLimit parameter only affects query that make use of DISTINCT: It adds this limit to the graph pattern
     * such that the query pattern on which the distinct runs is limited to the given number of bindings.
     *
     * It does not make sense to use for queries with group by because that would alter the result
     *
     *
     * SELECT COUNT(*) {
     *   SELECT DISTINCT originalProjection {
     *     SELECT * {
     *       originalGraphPattern
     *     } LIMIT rowLimit
     *   } OFFSET originalOffset LIMIT min(originalLimit, itemLimit)
     * }
     *
     * @param resultVar The output variable (COUNT(...) AS ?resultVar)
     * @param rawQuery
     * @param itemLimit Number of bindings to consider returned by the given select query
     * @param rowLimit Number of rows to consider within the query's graph pattern
     * @return
     */
    public static Query createQueryCountCore(Var resultVar, Query rawQuery, Long itemLimit, Long rowLimit) {
        // SELECT proj {} GROUP BY exprs -> SELECT DISTINCT proj' {}

        Objects.requireNonNull(resultVar, "resultVar must not be null");
        Objects.requireNonNull(rawQuery, "query must not be null");

        Query query = rawQuery.cloneQuery();

        optimizeGroupByToDistinct(query, true);

        boolean isDistinct = query.isDistinct();
        // If the query uses distinct and there is just a single projected variable
        // then we can transform 'DISTINCT ?s' to 'COUNT(DISTINCT ?s)'.
        // However, if there is more than 1 variable then we need wrapping in any case
        int projVarCount = query.getProjectVars().size();

        boolean isEffectiveQueryResultStar = isEffectiveQueryResultStar(query);

//        boolean needsWrappingByFeatures
//                =  query.hasGroupBy()
//                || query.hasAggregators()
//                || query.hasHaving()
//                || query.hasValues()
//                ;
//
//
//        boolean needsWrapping
//                = needsWrappingByFeatures
//                || (isDistinct && projVarCount > 1 && !isEffectiveQueryResultStar )
//                ;

        
        // Check the features - but exclude slice here because special rules are applied
        boolean needsWrappingByFeatures = needsWrappingByFeatures(query, false);
	    boolean needsWrapping
	    	= needsWrappingByFeatures
	        || (isDistinct && projVarCount > 1 && !isEffectiveQueryResultStar )
	        ;

        
        if(rowLimit != null && isDistinct && !needsWrappingByFeatures) {
            Element elt = query.getQueryPattern();
            Query subQuery = new Query();
            subQuery.setQuerySelectType();
            subQuery.setQueryResultStar(true);
            subQuery.setQueryPattern(elt);
            subQuery.setLimit(rowLimit);

            query.setQueryPattern(new ElementSubQuery(subQuery));
        }


        // || query.isDistinct() || query.isReduced();

        long tmpItemLimit = itemLimit == null ? Query.NOLIMIT : itemLimit;
        long queryLimit = query.getLimit();
        long effectiveLimit = queryLimit == Query.NOLIMIT
                ? tmpItemLimit
                : tmpItemLimit == Query.NOLIMIT
                    ? queryLimit
                    : Math.min(queryLimit, itemLimit);

        if(effectiveLimit != Query.NOLIMIT) {
              query.setLimit(effectiveLimit);
              needsWrapping = true;
          }

      //Element esq = new ElementSubQuery(subQuery);

        Var singleResultVar = null;
        VarExprList project = query.getProject();
        if(project.size() == 1) {
            Var v = project.getVars().iterator().next();
            Expr e = project.getExpr(v);
//        	Entry<Var, Expr> tmp = project.getExprs().entrySet().iterator().next();
//        	Var v = tmp.getKey();
//        	Expr e = tmp.getValue();
            if(e == null || (e.isVariable() && e.asVar().equals(v))) {
                singleResultVar = v;
            }
        }

        boolean useCountDistinct = !needsWrapping && query.isDistinct() && (isEffectiveQueryResultStar || singleResultVar != null);
        // TODO If there is only a single result variable (without mapping to an expr)
        // we can also use count distinct



        Aggregator agg = useCountDistinct
                ? singleResultVar == null
                    ? new AggCountDistinct()
                    : new AggCountVarDistinct(new ExprVar(singleResultVar))
                : new AggCount();

        Query result = new Query();
        result.setQuerySelectType();
        result.setPrefixMapping(query.getPrefixMapping());
        //cQuery.getProject().add(Vars.c, new ExprAggregator(Vars.x, agg));
        Expr aggCount = result.allocAggregate(agg);
        result.getProject().add(resultVar, aggCount);

        Element queryPattern;
        if(needsWrapping) {
            Query q = query.cloneQuery();
            q.setPrefixMapping(new PrefixMappingImpl());
            queryPattern = new ElementSubQuery(q);
        } else {
            queryPattern = query.getQueryPattern();
        }


        result.setQueryPattern(queryPattern);

        return result;
    }

//    public static Query createQueryCount(Query query, Var outputVar, Long itemLimit, Long rowLimit) {
//        Query subQuery = query.cloneQuery();
//        subQuery.setQuerySelectType();
//        subQuery.setQueryResultStar(true);
//
//        if(rowLimit != null) {
//            subQuery.setDistinct(false);
//            subQuery.setLimit(rowLimit);
//
//            subQuery = QueryGenerationUtils.wrapAsSubQuery(subQuery);
//            subQuery.setDistinct(true);
//        }
//
//        if(itemLimit != null) {
//            subQuery.setLimit(itemLimit);
//        }
//
//        Element esq = new ElementSubQuery(subQuery);
//
//        Query result = new Query();
//        result.setQuerySelectType();
//        Expr aggCount = result.allocAggregate(new AggCount());
//        result.getProject().add(outputVar, aggCount);
//        result.setQueryPattern(esq);
//
//        return result;
//    }


}
