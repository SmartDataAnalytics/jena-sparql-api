package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.FeatureMapImpl;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.transform.TransformAddFilterFromExtend;
import org.aksw.jena_sparql_api.algebra.transform.TransformDeduplicatePatterns;
import org.aksw.jena_sparql_api.algebra.transform.TransformDistributeJoinOverUnion;
import org.aksw.jena_sparql_api.algebra.transform.TransformFilterFalseToEmptyTable;
import org.aksw.jena_sparql_api.algebra.transform.TransformFilterSimplify;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinOverLeftJoin;
import org.aksw.jena_sparql_api.algebra.transform.TransformMergeProject;
import org.aksw.jena_sparql_api.algebra.transform.TransformPromoteTableEmptyVarPreserving;
import org.aksw.jena_sparql_api.algebra.transform.TransformPruneEmptyLeftJoin;
import org.aksw.jena_sparql_api.algebra.transform.TransformPullExtend;
import org.aksw.jena_sparql_api.algebra.transform.TransformPullFiltersIfCanMergeBGPs;
import org.aksw.jena_sparql_api.algebra.transform.TransformPushFiltersIntoBGP;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantFilterRemoval;
import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.utils.ClauseUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.NfUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.algebra.optimize.RewriteFactory;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/*
class QueryRewrite {
    protected Query masterQuery;
    protected Map<Node, Query>
}
*/

/**
 * TODO Separate cache specific parts from query containment ones
 *
 * @author raven
 *
 */
public class AlgebraUtils {


    private static final Logger logger = LoggerFactory.getLogger(AlgebraUtils.class);


    // TODO Move to Query Utils
//	public static Query rewrite(Query query, Function<? super Op, ? extends Op> rewriter) {
//		Op op = Algebra.compile(query);
////System.out.println(op);
//		op = rewriter.apply(op);
//
//		Query result = OpAsQuery.asQuery(op);
//		result.getPrefixMapping().setNsPrefixes(query.getPrefixMapping());
//		return result;
//	}

    public static Rewrite createDefaultRewriter() {

        Context context = new Context();
        context.put(ARQ.optPromoteTableEmpty, false);

        // context.put(ARQ.optFilterImplicitJoin, true);

        // Path flatten creates these ??P variables which break the resulting query
        // I don't know yet of the proper way to get rid of these
        // Maybe I am accidently calling NodeValue.makeNode(var) at some point
        // context.put(ARQ.optPathFlatten, false);

        //context.put(ARQ.optReorderBGP, true);

        context.put(ARQ.optMergeBGPs, false); // We invoke this manually
        context.put(ARQ.optMergeExtends, true);

        // false; OpAsQuery throws Not implemented: OpTopN (jena 3.8.0)
        context.put(ARQ.optTopNSorting, false);

//        context.put(ARQ.optFilterPlacement, true);
        context.put(ARQ.optImplicitLeftJoin, false);
        context.put(ARQ.optFilterPlacement, true);
        context.put(ARQ.optFilterPlacementBGP, false);
        context.put(ARQ.optFilterPlacementConservative, false); // with false the result looks better

        // Retain E_OneOf expressions
        context.put(ARQ.optFilterExpandOneOf, false);

//
//
//        // optIndexJoinStrategy mut be off ; it introduces OpConditional nodes which
//        // cannot be transformed back into syntax
        context.put(ARQ.optIndexJoinStrategy, false);
//
        // It is important to keep optFilterEquality turned off!
        // Otherwise it may push constants back into the quads
        context.put(ARQ.optFilterEquality, false);
        context.put(ARQ.optFilterInequality, false);
        context.put(ARQ.optDistinctToReduced, false);
        context.put(ARQ.optInlineAssignments, false);
        context.put(ARQ.optInlineAssignmentsAggressive, false);

        // false; OpAsQuery throws Not implemented: OpDisjunction (jena 3.8.0)
        context.put(ARQ.optFilterDisjunction, false);
        context.put(ARQ.optFilterConjunction, true);

        context.put(ARQ.optExprConstantFolding, true);

//        Rewrite rewriter = Optimize.stdOptimizationFactory.create(context);
        RewriteFactory factory = Optimize.getFactory();
        Rewrite core = factory.create(context);


        // Wrap jena's rewriter with additional transforms
        Rewrite pushDown = op -> {

                op = core.rewrite(op);

                // Issue with Jena 3.8.0 (possibly other versions too)
                // Jena's rewriter returned by Optimize.getFactory() renames variables (due to scoping)
                // but does not reverse the renaming - so we need to do it explicitly here
                // TODO Is reversing really needed? If our code is correct, then Jena should just cope with it
                op = Rename.reverseVarRename(op, true);

                op = FixpointIteration.apply(op, x -> {
                    x = TransformJoinOverLeftJoin.transform(x);
                    return x;
                });

//        		op = FixpointIteration.apply(op, x -> {
//            		x = TransformPullFiltersIfCanMergeBGPs.transform(x);
//            		x = Transformer.transform(new TransformMergeBGPs(), x);
//            		return x;
//        		});

                op = TransformPushFiltersIntoBGP.transform(op);
                op = TransformDeduplicatePatterns.transform(op);
                op = TransformFilterSimplify.transform(op);
                op = TransformRedundantFilterRemoval.transform(op);


//        		op = TransformPushFiltersIntoBGP.transform(op);
//        		op = TransformDeduplicatePatterns.transform(op);
//        		op = TransformRedundantFilterRemoval.transform(op);
//        		op = TransformFilterSimplify.transform(op);

                op = TransformPruneEmptyLeftJoin.transform(op);

                op = TransformFilterFalseToEmptyTable.transform(op);
                op = TransformPromoteTableEmptyVarPreserving.transform(op);

                op = TransformMergeProject.transform(op);

//        		op = FixpointIteration.apply(op, x -> {
//        			x = TransformDistributeJoinOverUnion.transform(x);
//        			return x;
//        		});


                return op;
        };

        Rewrite pullUp = op -> {
            op = TransformPullExtend.transform(op);
            op = TransformPullFiltersIfCanMergeBGPs.transform(op);
            op = Transformer.transform(new TransformMergeBGPs(), op);
            return op;
        };


        Rewrite result = op -> {

//        	op = FixpointIteration.apply(op, x -> {
//        		x = TransformPullFilters.transform(x);
//        		return x;
//        	});



            // Extract filters only once from extend
            op = TransformAddFilterFromExtend.transform(op);

            op = FixpointIteration.apply(op, x -> {
                x = FixpointIteration.apply(x, pushDown::rewrite);
                x = pullUp.rewrite(x);
            //x = FixpointIteration.apply(x, pullUp::rewrite);
                return x;
            });
            return op;
        };
        return result;
        //return result;
    }


    /**
     * Wrap a node with a caching operation
     *
     * @param subOp
     * @param cacheRef The cache entry to create and where the result set will be stored
     *
     * @return
     */
    public static Op createCachingOp(Op subOp, Node storageRef) {
        boolean silent = false;

        //Query subQuery = OpAsQuery.asQuery(subOp);
        //Element subElement = new ElementSubQuery(subQuery);

        //ElementService elt = new ElementService(storageRef, subElement, silent);
        OpService result = new OpService(storageRef, subOp, silent);

        return result;
    }

    /**
     * Utility method to quickly create a canonical quad filter pattern from a query.
     *
     * @param query
     * @return
     */
    public static QuadFilterPatternCanonical fromQuery(Query query) {
        ProjectedOp op = AlgebraUtils.toProjectedOp(query);
        Op resOp = op.getResidualOp();
        QuadFilterPatternCanonical result = AlgebraUtils.extractQuadFilterPatternCanonical(resOp);
        return result;
    }

    public static ProjectedOp toProjectedOp(Query query) {
        Op op = Algebra.compile(query);
//		op = Transformer.transform(TransformUnionToDisjunction.fn, op);

        // Push down joins until there is no more change
        Op current;
        do {
            current = op;
            op = TransformDistributeJoinOverUnion.transform(current);
        } while(!current.equals(op));


//		op = Transformer.transform(TransformUnionToDisjunction.fn, op);
        op = Transformer.transform(new TransformMergeBGPs(), op);
        op = Algebra.toQuadForm(op);

        //System.out.println("asQuery: "+ OpAsQuery.asQuery(op));
        ProjectedOp result = AlgebraUtils.cutProjection(op);
        return result;
    }
    public static QuadFilterPatternCanonical removeDefaultGraphFilter(QuadFilterPatternCanonical qfpc) {
        Set<Quad> quads = qfpc.getQuads();
        Set<Set<Expr>> cnf = qfpc.getFilterCnf();

        Map<Var, Node> varToNode = CnfUtils.getConstants(cnf, true);
        Map<Var, Node> candMap = varToNode.entrySet().stream().filter(
                e -> Quad.isDefaultGraph(e.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<Var> candVars = candMap.keySet();

        // Remove all vars that occurr in positions other than the graph
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 1; i < 4; ++i) {
                Node node = nodes[i];
                candVars.remove(node);
            }
        }

        Set<Set<Expr>> newCnf = cnf.stream().filter(clause -> {
            Entry<Var, Node> e = CnfUtils.extractEquality(clause);
            boolean r = !candMap.entrySet().contains(e);
            return r;
        }).collect(Collectors.toSet());

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(quads, ExprHolder.fromCnf(newCnf));
        return result;
    }


    public static ProjectedQuadFilterPattern optimizeFilters(ProjectedQuadFilterPattern pqfp) {
        PatternSummary summary = summarize(pqfp.getQuadFilterPattern());
        QuadFilterPatternCanonical qfpc = summary.getCanonicalPattern();
        QuadFilterPatternCanonical optimized = optimizeFilters(qfpc.getQuads(), qfpc.getFilterCnf(), pqfp.getProjectVars());

        QuadFilterPattern qfp = optimized.toQfp();
        ProjectedQuadFilterPattern result = new ProjectedQuadFilterPattern(pqfp.getProjectVars(), qfp, false);

        return result;
    }


    public static QuadFilterPatternCanonical optimizeFilters(Collection<Quad> quads, Set<Set<Expr>> cnf, Set<Var> projection) {

        Map<Var, Node> varToNode = CnfUtils.getConstants(cnf, true);

        // A view on the set of variables subject the optimization
        Set<Var> optVars = varToNode.keySet();

        // Remove all equalities for projected variables
        optVars.remove(projection);

        Set<Quad> newQuads = new HashSet<Quad>();
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 0; i < 4; ++i) {
                Node node = nodes[i];
                Node subst = varToNode.get(node);

                // Update in place, because the array is a copy anyway
                nodes[i] = subst == null ? node : subst;
            }

            Quad newQuad = QuadUtils.arrayToQuad(nodes);
            newQuads.add(newQuad);
        }

        // Remove the clauses from which the mapping was obtained
        Set<Set<Expr>> newCnf = new HashSet<>();
        for(Set<Expr> clause : cnf) {
            Entry<Var, Node> equality = CnfUtils.extractEquality(clause);
            boolean retainClause = equality == null || !optVars.contains(equality.getKey());

            if(retainClause) {
                newCnf.add(clause);
            }
        }

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, ExprHolder.fromCnf(newCnf));
        return result;
    }



    // TODO Not used, can probably be removed
//    public static ResultSet executeCached(QueryExecutionFactory qef, Query query, ProjectedQuadFilterPattern pqfp, SparqlViewCache sparqlViewCache, long indexResultSetSizeThreshold) {
//        if(pqfp == null) {
//            throw new RuntimeException("Query is not indexable: " + query);
//        }
//
//        Set<Var> indexVars = new HashSet<>(query.getProjectVars());
//
//        QueryExecution qe = new QueryExecutionViewCacheFragment(query, pqfp, qef, sparqlViewCache, indexVars, indexResultSetSizeThreshold);
//        ResultSet result = qe.execSelect();
//        return result;
//    }




    /**
     * Create a service node with a union where the first member is to be interpreted as the
     * pattern that should be used for caching, and the second argument is the pattern to be
     * executed.
     *
     * @param patternOp
     * @param serviceNode
     * @param executionOp
     * @return
     */
    public static OpService wrapWithServiceOld(Op patternOp, Node serviceNode, Op executionOp) {
        boolean silent = false;
        OpUnion union = new OpUnion(patternOp, executionOp);

        Query subQuery = OpAsQuery.asQuery(union);
        Element subElement = new ElementSubQuery(subQuery);

        ElementService elt = new ElementService(serviceNode, subElement, silent);
        OpService result = new OpService(serviceNode, union, elt, silent);

        return result;
    }





    /**
     * Rename all variables to ?g ?s ?p ?o based on the given quad and the cnf
     * This is used for looking up triples having a certain expression over its components
     *
     * ([?g ?s ?s ?o], (fn(?s, ?o))
     *
     * @param quad
     * @param expr
     */
    public static Set<Set<Expr>> normalize(Quad quad, Set<Set<Expr>> nf) {
        List<Var> componentVars = Vars.gspo;

        Map<Var, Var> renameMap = new HashMap<Var, Var>();
        Set<Set<Expr>> extra = new HashSet<Set<Expr>>();
        for(int i = 0; i < 4; ++i) {
            Node tmp = QuadUtils.getNode(quad, i);

            if(i == 0 && (Quad.defaultGraphNodeGenerated.equals(tmp) || Quad.defaultGraphIRI.equals(tmp))) {
                continue;
            }

            if(!tmp.isVariable()) {
                throw new RuntimeException("Expected variable normalized quad, got: " + quad);
            }

            Var quadVar = (Var)tmp;
            Var componentVar = componentVars.get(i);

            Var priorComponentVar = renameMap.get(quadVar);
            // We need to rename
            if(priorComponentVar != null) {
                extra.add(Collections.<Expr>singleton(new E_Equals(new ExprVar(priorComponentVar), new ExprVar(componentVar))));
            } else {
                renameMap.put(quadVar, componentVar);
            }
        }

        NodeTransformRenameMap transform = new NodeTransformRenameMap(renameMap);
        Set<Set<Expr>> result = ClauseUtils.applyNodeTransformSet(nf, transform);
        result.addAll(extra);

        //System.out.println(result);
        return result;
    }

    public static Set<Set<Expr>> add(Quad quad, Set<Set<Expr>> cnf) {
        Set<Set<Expr>> result = new HashSet<Set<Expr>>();

        for(Set<Expr> clause : cnf) {
            Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
            Set<Var> exprVars = QuadUtils.getVarsMentioned(quad);

            boolean isApplicable = exprVars.containsAll(clauseVars);
            if(isApplicable) {
                result.add(clause);
            }
        }

        return result;
    }


    public static ProjectedOp cutProjectionAndNormalize(Op op, Rewrite opNormalizer) {
        // Before normalization, cut away the projection on the original op first
        ProjectedOp projectedOp = AlgebraUtils.cutProjection(op);

        Op normalizedOp = opNormalizer.rewrite(projectedOp.getResidualOp());
        ProjectedOp result = new ProjectedOp(projectedOp.getProjection(), normalizedOp);

        return result;
    }

    /**
     * Cut away the projection (TODO: and maybe extend) of an op (if any), and return
     * the projection as a standalone object together with the remaining op.
     *
     * @param residualOp
     * @return
     */
    public static ProjectedOp cutProjection(Op op) {
        Op residualOp = op;

        Set<Var> projectVars = null;

        int distinctLevel = 0;
        if(residualOp instanceof OpDistinct) {
            distinctLevel = 2;
            residualOp = ((OpDistinct)residualOp).getSubOp();
        }

        if(residualOp instanceof OpProject) {
            OpProject tmp = (OpProject)residualOp;
            projectVars = new LinkedHashSet<>(tmp.getVars());

            residualOp = tmp.getSubOp();
        }

        ProjectedOp result = projectVars == null
                ? new ProjectedOp(new VarInfo(OpUtils.visibleNamedVars(residualOp), 0), residualOp)
                : new ProjectedOp(new VarInfo(projectVars, distinctLevel), residualOp);

        return result;
    }

    public static ProjectedQuadFilterPattern transform(Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = TransformReplaceConstants.transform(op);
        ProjectedQuadFilterPattern result = transform(op);
        return result;
    }

    public static QuadFilterPatternCanonical transform2(Query query) {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        op = TransformReplaceConstants.transform(op);
        ProjectedQuadFilterPattern pqfp = transform(op);
        QuadFilterPatternCanonical result = pqfp == null ? null : canonicalize2(pqfp.getQuadFilterPattern(), VarGeneratorImpl2.create("v"));

        return result;
    }


    public static ProjectedQuadFilterPattern transform(Element element) {

        Op op = Algebra.compile(element);
        op = Algebra.toQuadForm(op);
        ProjectedQuadFilterPattern result = transform(op);
        return result;
    }

    public static QuadFilterPatternCanonical extractQuadFilterPatternCanonical(Op op) {
        QuadFilterPattern qfp = AlgebraUtils.extractQuadFilterPattern(op);
        QuadFilterPatternCanonical result;
        if(qfp != null) {
            Generator<Var> generator = VarGeneratorImpl2.create();
            result = AlgebraUtils.canonicalize2(qfp, generator);
        } else {
            result = null;
        }
        return result;
    }


    public static ConjunctiveQuery tryExtractConjunctiveQuery(Op op, Generator<Var> generator) {
        OpDistinct opDistinct = null;
        OpProject opProject = null;

        boolean consumeDistinct = false;
        boolean consumeProject = true;

        if(consumeDistinct && op instanceof OpDistinct) {
            opDistinct = (OpDistinct)op;
            op = opDistinct.getSubOp();
        }

        if(consumeProject && op instanceof OpProject) {
            opProject = (OpProject)op;
            op = opProject.getSubOp();
        }

        QuadFilterPattern qfp = extractQuadFilterPattern(op);

        ConjunctiveQuery result = null;
        if(qfp != null) {
            boolean isDistinct = opDistinct != null;
            Set<Var> projectVars = opProject == null
                    ? OpVars.visibleVars(op)
                    : new LinkedHashSet<>(opProject.getVars());

            VarInfo varInfo = new VarInfo(projectVars, isDistinct ? 2 : 0);

            QuadFilterPatternCanonical qfpc = canonicalize2(qfp, generator);
            result = new ConjunctiveQuery(varInfo, qfpc);


            // TODO canonicalize the pattern
//            result = new ConjunctiveQuery(varInfo, qfpc);
        }

        return result;
    }

    public static QuadFilterPattern extractQuadFilterPattern(Op op) {
        QuadFilterPattern result = null;

//        //
//        if(op instanceof OpQuadFilterPatternCanonical) {
//        	result = ((OpQuadFilterPatternCanonical)op).getQfpc().toQfp();
//        }

        OpFilter opFilter;
        // TODO allow nested filters
        if(op instanceof OpFilter) {
            opFilter = (OpFilter)op;
        } else {
            opFilter = (OpFilter)OpFilter.filter(NodeValue.TRUE, op);
        }

        op = opFilter.getSubOp();

        if(op instanceof OpGraph) {
            OpGraph opGraph = (OpGraph)op;
            Node graphNode = opGraph.getNode();


//            boolean retainDefaultGraphNode = true;
//            if(retainDefaultGraphNode && Quad.defaultGraphNodeGenerated.equals(graphNode)) {


            // The graphNode must be a variable which is not constrained by the filter

            Set<Var> filterVars = ExprVars.getVarsMentioned(opFilter.getExprs());
            if(graphNode.isVariable() && !filterVars.contains(graphNode)) {
                op = opGraph.getSubOp();
            } else {
                op = null;
            }
        }

        if(op instanceof OpQuadPattern) {
            OpQuadPattern opQuadPattern = (OpQuadPattern)opFilter.getSubOp();

            QuadPattern quadPattern = opQuadPattern.getPattern();
            List<Quad> quads = quadPattern.getList();

            ExprList exprs = opFilter.getExprs();
            Expr expr = ExprUtils.andifyBalanced(exprs);

            result = new QuadFilterPattern(quads, expr);
        }

        return result;
    }

    /**
     * Note assumes that this has been applied so far:
     *  op = Algebra.toQuadForm(op);
     * op = ReplaceConstants.replace(op);
     *
     * @param op
     * @return
     */
    public static ProjectedQuadFilterPattern transform(Op op) {

        ProjectedQuadFilterPattern result = null;

        Set<Var> projectVars = null;

        boolean isDistinct = false;
        if(op instanceof OpDistinct) {
            isDistinct = true;
            op = ((OpDistinct)op).getSubOp();
        }

        if(op instanceof OpProject) {
            OpProject tmp = (OpProject)op;
            projectVars = new HashSet<>(tmp.getVars());

            op = tmp.getSubOp();
        }

        QuadFilterPattern qfp = extractQuadFilterPattern(op);

        if(qfp != null) {
            if(projectVars == null) {
                projectVars = new HashSet<>(OpVars.mentionedVars(op));
            }

            result = new ProjectedQuadFilterPattern(projectVars, qfp, isDistinct);
        }


        return result;
    }

    public static Map<Quad, Set<Set<Expr>>> quadToCnf(QuadFilterPattern qfp) {
        Map<Quad, Set<Set<Expr>>> result = new HashMap<Quad, Set<Set<Expr>>>();

        Expr expr = qfp.getExpr();
        if(expr == null) {
            expr = NodeValue.TRUE;
        }

        Set<Set<Expr>> filterCnf = CnfUtils.toSetCnf(expr);

        Set<Quad> quads = new HashSet<Quad>(qfp.getQuads());

        for(Quad quad : quads) {

            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>();

            for(Set<Expr> clause : filterCnf) {
                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);

                boolean containsAll = quadVars.containsAll(clauseVars);
                if(containsAll) {
                    cnf.add(clause);
                }
            }


            //Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            result.put(quad, cnf);
        }

        return result;
    }



//    public static QuadFilterPattern canonicalize(QuadFilterPattern qfp, Generator<Var> generator) {
//        QuadFilterPatternCanonical tmp = replaceConstants(qfp.getQuads(), generator);
//        Set<Set<Expr>> cnf = CnfUtils.toSetCnf(qfp.getExpr());
//        cnf.addAll(tmp.getFilterCnf());
//        QuadFilterPatternCanonical canonical = new QuadFilterPatternCanonical(tmp.getQuads(), cnf);
//
//
//        //QuadFilterPatternCanonical qfpc = summarize(qfp).getCanonicalPattern();
//
////        QuadFilterPatternCanonical tmp = canonicalize(qfpc, generator);
//        QuadFilterPattern result = canonical.toQfp();
//
//        return result;
//    }

    public static OpExtConjunctiveQuery tryCreateCqfpOld(Op op, Generator<Var> generator) {
        ConjunctiveQuery cq = tryExtractConjunctiveQuery(op, generator);

        OpExtConjunctiveQuery result = cq == null
                ? null
                : new OpExtConjunctiveQuery(cq);

        return result;
    }

    public static Op tryCreateCqfp(Op op, Generator<Var> generator) {
        ConjunctiveQuery cq = tryExtractConjunctiveQuery(op, generator);

        // Idea:
        // Separate the purely conjunctive filter part from the disjunctive part:
        // The set of clauses having only 1 element remain part of the conjunctive query
        // the other clauses go into a separate filter node

        //Set<Expr> conjunctive = new LinkedHashSet<>();
        Op result = null;
        if(cq != null) {
            Set<Set<Expr>> conjunctive = new LinkedHashSet<>();
            Set<Set<Expr>> disjunctive = new LinkedHashSet<>();

            Set<Set<Expr>> cnf = cq.getPattern().getExprHolder().getCnf();

            for(Set<Expr> clause : cnf) {
                if(clause.size() == 1) {
                    //conjunctive.add(clause.iterator().next());
                    conjunctive.add(clause);
                } else {
                    disjunctive.add(clause);
                }
            }

            Set<Var> requiredVars = new LinkedHashSet<Var>(cq.getProjection().getProjectVars());

            Set<Var> vars = NfUtils.getVarsMentioned(disjunctive);
            requiredVars.addAll(vars);

            QuadFilterPatternCanonical qfpc = new QuadFilterPatternCanonical(cq.getPattern().getQuads(), ExprHolder.fromCnf(conjunctive));
            cq = new ConjunctiveQuery(new VarInfo(requiredVars, 0), qfpc);
            result = new OpExtConjunctiveQuery(cq);

            if(!disjunctive.isEmpty()) {
                Expr expr = DnfUtils.toExpr(disjunctive);
                result = OpFilter.filterDirect(expr, result);

                // Apply project
                if(!requiredVars.equals(cq.getProjection().getProjectVars())) {
                    result = new OpProject(result, new ArrayList<>(cq.getProjection().getProjectVars()));
                }
            }


            // Apply distinct
            if(cq.getProjection().getDistinctLevel() != 0) {
                result = new OpDistinct(result);
            }

        }

        return result;
    }

    // Assumes that ReplaceConstants has been called
    public static QuadFilterPatternCanonical canonicalize2(QuadFilterPattern qfp, Generator<Var> generator) {
        Set<Set<Expr>> dnf = DnfUtils.toSetDnf(qfp.getExpr());
        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(new LinkedHashSet<>(qfp.getQuads()), ExprHolder.fromDnf(dnf));

        return result;
    }

    public static QuadFilterPatternCanonical canonicalize2old(QuadFilterPattern qfp, Generator<Var> generator) {
        QuadFilterPatternCanonical tmp = replaceConstants(qfp.getQuads(), generator);
        tmp = removeDefaultGraphFilter(tmp);
        Set<Set<Expr>> cnf = CnfUtils.toSetCnf(qfp.getExpr());
        cnf.addAll(tmp.getFilterCnf());
        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(tmp.getQuads(), ExprHolder.fromCnf(cnf));

        return result;
    }

    public static QuadFilterPatternCanonical canonicalize(QuadFilterPatternCanonical qfpc, Generator<Var> generator) {
        QuadFilterPatternCanonical tmp = replaceConstants(qfpc.getQuads(), generator);

        Set<Set<Expr>> newCnf = new HashSet<>();
        newCnf.addAll(qfpc.getFilterCnf());
        newCnf.addAll(tmp.getFilterCnf());

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(tmp.getQuads(), ExprHolder.fromCnf(newCnf));
        return result;
    }

    public static QuadFilterPatternCanonical replaceConstants(Iterable<Quad> quads, Generator<Var> generator) {
        Set<Set<Expr>> cnf = new HashSet<>();

        Map<Node, Var> constantToVar = new HashMap<>();

        Set<Quad> newQuads = new LinkedHashSet<>();
        for(Quad quad : quads) {
            Node[] nodes = QuadUtils.quadToArray(quad);
            for(int i = 0; i < 4; ++i) {
                Node node = nodes[i];

                if(!node.isVariable()) {
                    Var v = constantToVar.get(node);
                    if(v == null) {
                        v = generator.next();
                        constantToVar.put(node, v);

                        Expr expr = new E_Equals(new ExprVar(v), NodeValue.makeNode(node));
                        cnf.add(Collections.singleton(expr));
                    }
                    nodes[i] = v;
                }
                // If it is a variable, just retain it
            }

            Quad newQuad = QuadUtils.arrayToQuad(nodes);
            newQuads.add(newQuad);
        }

        QuadFilterPatternCanonical result = new QuadFilterPatternCanonical(newQuads, ExprHolder.fromCnf(cnf));
        return result;
    }


    public static PatternSummary summarize(QuadFilterPattern originalPattern) {

        Expr expr = originalPattern.getExpr();
        Set<Quad> quads = new LinkedHashSet<Quad>(originalPattern.getQuads());


        Set<Set<Expr>> filterDnf = DnfUtils.toSetDnf(expr, true);

        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToDnf = createMapQuadsToFilters(quads, filterDnf);
        IBiSetMultimap<Var, VarOccurrence> varOccurrences = createMapVarOccurrences(quadToDnf, false);

        //System.out.println("varOccurrences: " + varOccurrences);
        //Set<Set<Set<Expr>>> quadCnfs = new HashSet<Set<Set<Expr>>>(quadCnfList);

        QuadFilterPatternCanonical canonicalPattern = new QuadFilterPatternCanonical(quads, ExprHolder.fromDnf(filterDnf));
        //canonicalPattern = canonicalize(canonicalPattern, generator);


        PatternSummary result = new PatternSummary(originalPattern, canonicalPattern, quadToDnf, varOccurrences);


        //for(Entry<Var, Collection<VarOccurrence>> entry : varOccurrences.asMap().entrySet()) {
            //System.out.println("Summary: " + entry.getKey() + ": " + entry.getValue().size());
            //System.out.println(entry);
        //}

        return result;
    }


    private static IBiSetMultimap<Var, VarOccurrence> createMapVarOccurrences(
            IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf, boolean pruneVarOccs) {
        Set<Quad> quads = quadToCnf.keySet();

        // Iterate the quads again, and for each variable map it to where it to the component where it occurs in
        IBiSetMultimap<Var, VarOccurrence> varOccurrences = new BiHashMultimap<Var, VarOccurrence>();
        //for(int i = 0; i < quads.size(); ++i) {
            //Quad quad = quads.get(i);
        for(Quad quad : quads) {
            Set<Set<Expr>> quadCnf = quadToCnf.get(quad).iterator().next(); //quadCnfList.get(i);

            for(int j = 0; j < 4; ++j) {
                Var var = (Var)QuadUtils.getNode(quad, j);

                VarOccurrence varOccurence = new VarOccurrence(quadCnf, j);

                varOccurrences.put(var, varOccurence);
            }
        }

        // Remove all variables that only occur in the same quad
        //boolean pruneVarOccs = false;
        if(pruneVarOccs) {
            Iterator<Entry<Var, Collection<VarOccurrence>>> it = varOccurrences.asMap().entrySet().iterator();

            while(it.hasNext()) {
                Entry<Var, Collection<VarOccurrence>> entry = it.next();

                Set<Set<Set<Expr>>> varQuadCnfs = new HashSet<Set<Set<Expr>>>();
                for(VarOccurrence varOccurrence : entry.getValue()) {
                    varQuadCnfs.add(varOccurrence.getQuadCnf());

                    // Bail out early
                    if(varQuadCnfs.size() > 1) {
                        break;
                    }
                }

                if(varQuadCnfs.size() == 1) {
                    it.remove();
                }
            }
        }

        return varOccurrences;
    }


    /**
     * Note: the result map contains all quads - quads without constraints map to an empty set
     *
     *
     *
     * @param quads
     * @param filterCnf
     * @return
     */
    public static IBiSetMultimap<Quad, Set<Set<Expr>>> createMapQuadsToFilters(QuadFilterPatternCanonical qfpc) {
        Set<Quad> quads = qfpc.getQuads();
        Set<Set<Expr>> filterDnf = qfpc.getFilterDnf();
        if(filterDnf == null) {
            filterDnf = Collections.singleton(Collections.emptySet());
        }

        IBiSetMultimap<Quad, Set<Set<Expr>>> result = createMapQuadsToFilters(quads, filterDnf);
        return result;
    }

//    public static IBiSetMultimap<Quad, Set<Set<Expr>>> createMapQuadsToFilters(
//            Set<Quad> quads, Set<Set<Expr>> filterCnf) {
//        // This is part of the result
//        //List<Set<Set<Expr>>> quadCnfList = new ArrayList<Set<Set<Expr>>>(quads.size());
//        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToCnf = new BiHashMultimap<Quad, Set<Set<Expr>>>();
//
//
//
//        for(Quad quad : quads) {
//            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);
//
//            Set<Set<Expr>> cnf = new HashSet<Set<Expr>>(); //new HashSet<Clause>();
//
//            for(Set<Expr> clause : filterCnf) {
//                Set<Var> clauseVars = ClauseUtils.getVarsMentioned(clause);
//
//                boolean containsAll = quadVars.containsAll(clauseVars);
//                if(containsAll) {
//                    cnf.add(clause);
//                }
//            }
//
//
//            Set<Set<Expr>> quadCnf = normalize(quad, cnf);
//            //quadCnfList.add(quadCnf);
//            quadToCnf.put(quad, quadCnf);
//        }
//        return quadToCnf;
//    }

    public static IBiSetMultimap<Quad, Set<Set<Expr>>> createMapQuadsToFilters(
            Set<Quad> quads, Set<Set<Expr>> filterDnf) {
        // This is part of the result
        //List<Set<Set<Expr>>> quadCnfList = new ArrayList<Set<Set<Expr>>>(quads.size());
        IBiSetMultimap<Quad, Set<Set<Expr>>> quadToDnf = new BiHashMultimap<Quad, Set<Set<Expr>>>();



        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);
            Set<Set<Expr>> dnf = new HashSet<>(); //new HashSet<Clause>();

            for(Set<Expr> clause : filterDnf) {
                Set<Expr> cnf = new HashSet<>();

                for(Expr expr : clause) {
                    Set<Var> exprVars = ExprVars.getVarsMentioned(expr);

                    boolean containsAll = quadVars.containsAll(exprVars);
                    if(containsAll) {
                        cnf.add(expr);
                    }
                }
                dnf.add(cnf);
            }

            dnf = AlgebraUtils.normalize(quad, dnf);

            //Set<Set<Expr>> quadCnf = normalize(quad, cnf);
            //quadCnfList.add(quadCnf);
            quadToDnf.put(quad, dnf);
        }
        return quadToDnf;
    }

    public static Expr createExpr(ResultSet rs, Map<Var, Var> varMap) {

        //ResultSet copy = ResultSetFactory.copyResults(rs);

        Expr result;

        if(rs.getResultVars().size() == 1) {
            String varName = rs.getResultVars().iterator().next();
            Var var = Var.alloc(varName);

            Set<Node> nodes = getResultSetCol(rs, var);
            ExprList exprs = nodesToExprs(nodes);

            Var inVar = varMap.get(var);
            ExprVar ev = new ExprVar(inVar);

            result = new E_OneOf(ev, exprs);
        } else {
            throw new RuntimeException("Not supported yet");
        }

        return result;
    }

    public static Set<Node> getResultSetCol(ResultSet rs, Var v) {
        Set<Node> result = new HashSet<Node>();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node node = binding.get(v);

            if(node != null) {
                result.add(node);
            }
        }

        return result;
    }

    public static ExprList nodesToExprs(Iterable<Node> nodes) {
        ExprList result = new ExprList();
        for(Node node : nodes) {
            Expr expr = NodeValue.makeNode(node);
            result.add(expr);
        }

        return result;
    }


    /**
     * TODO this has complexity O(n^2)
     * We can surely do better than that because joins are sparse and we
     * don't have to consider quads that do not join...
     *
     *
     * @param sub
     * @return
     */
    public static SetMultimap<Quad, Quad> quadJoinSummary(List<Quad> sub) {

        Node[] tmp = new Node[4];

        SetMultimap<Quad, Quad> result = HashMultimap.create();
        for(int i = 0; i < sub.size(); ++i) {
            Quad a = sub.get(i);
            for(int j = i + 1; j < sub.size(); ++j) {
                Quad b = sub.get(j);

                for(int k = 0; k < 4; ++k) {
                    Node na = QuadUtils.getNode(a, k);
                    Node nb = QuadUtils.getNode(b, k);
                    boolean isEqual = na.equals(nb);
                    Node c = isEqual ? NodeValue.TRUE.asNode() : NodeValue.FALSE.asNode();

                    tmp[k] = c;
                }
                Quad summary = QuadUtils.create(tmp);

                result.put(summary, a);
                result.put(summary, b);
            }
        }

        return result;
    }



    // Return the variables that we cannot optimize away, as they
    // are referenced in the following portions
    // - projection
    // - order
    // - group by
    public static Set<Var> getRefVars(Query query) {
        //query.getProjectVars();
        return null;
    }


    public static FeatureMap<Expr, Multimap<Expr, Expr>> indexDnf(Set<Set<Expr>> dnf) {
        if(dnf == null) {
            // A disjunction containing an empty conjunction (latter is generally treated as true - if i'm not mistaken)
            dnf = Collections.singleton(Collections.emptySet());
            //dnf = Collections.emptySet();
        }

        FeatureMap<Expr, Multimap<Expr, Expr>> result = new FeatureMapImpl<>();

        for(Set<Expr> clause : dnf) {
            Multimap<Expr, Expr> exprSigToExpr = HashMultimap.create();
            Set<Expr> clauseSig = new HashSet<>();
            for(Expr expr : clause) {
                Expr exprSig = org.aksw.jena_sparql_api.utils.ExprUtils.signaturize(expr);
                exprSigToExpr.put(exprSig, expr);
                clauseSig.add(exprSig);
            }

            //Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
            result.put(clauseSig, exprSigToExpr);
        }

        return result;
    }




    /**
     * So we need to know which variables of a quad pattern are either projected or required for evaluation (e.g. filters, joins) of the overall query.
     *
     *
     *
     *
     * For each quad filter pattern of the given algebra expression determine which variables are projected and
     * whether distinct applies.
     * So actually we want to push distinct down - but this also does not make sense, because distinct only applies to
     * after a projection...
     *
     *
     * The mean thing is, that variables are actually scoped:
     * Select ?x {
     *   { Select Distinct ?s As ?x{ // Within this subtree, unique(?x) applies
     *     ?s a foaf:Person
     *   } }
     *   Union {
     *     ?x a foaf:Agent
     *   }
     * }
     *
     *
     *
     *
     * @param opIndex
     */
//    public static VarUsage analyzeQuadFilterPatterns(OpIndex opIndex) {
//    	Tree<Op> tree = opIndex.getTree();
//    	List<Op> leafs = TreeUtils.getLeafs(tree);
//    	for(Op leaf : leafs) {
//    		VarUsage ps = OpUtils.analyzeVarUsage(tree, leaf);
//    		System.out.println(ps);
//    	}
//    	return null;
//    }
}


//// Variables that are projected in the current iteration
////Set<Var> projectedVars = new HashSet<>(availableVars);
//
//// Variables that are referenced
//Set<Var> referencedVars = new HashSet<>();
//
//// Any variable that is aggregated on must not be non-unique (otherwise it would distort the result)
//// Note that an overall query neither projects nor references a nonUnique variable
//Set<Var> nonUnique = new HashSet<>();
//
//// Maps variables to which other vars they depend on
//// E.g. Select (?x + 1 As ?y) { ... } will create the entry ?y -> { ?x } - i.e. ?y depends on ?x
//// Transitive dependencies are resolved immediately
//Multimap<Var, Var> varDeps = HashMultimap.create();
//availableVars.forEach(v -> varDeps.put(v, v));
//
//Op placeholder = new OpBGP();
//Op parent;
//while((parent = tree.getParent(current)) != null) {
//	// Class<?> opClass = current.getClass();
//	// System.out.println("Processing: " + parent);
//	// Compute referenced vars for joins (non-disjunctive multi argument expressions)
//	// boolean isDisjunction = parent instanceof OpUnion || parent instanceof OpDisjunction;
//	// boolean isJoin = parent instanceof OpJoin || parent instanceof OpSequence;
//	if(parent instanceof OpJoin || parent instanceof OpLeftJoin || parent instanceof OpSequence) {
//		List<Op> children = tree.getChildren(parent);
//		List<Op> tmp = new ArrayList<>(children);
//		Set<Var> visibleVars = new HashSet<>();
//		for(int i = 0; i < tmp.size(); ++i) {
//			Op child = tmp.get(i);
//			if(child != current) {
//				OpVars.visibleVars(child, visibleVars);
//			}
//		}
//
//		if(parent instanceof OpLeftJoin) {
//			OpLeftJoin olj = (OpLeftJoin)parent;
//			ExprList exprs = olj.getExprs();
//			if(exprs != null) {
//				Set<Var> vms = ExprVars.getVarsMentioned(exprs);
//				visibleVars.addAll(vms);
//			}
//		}
//
//		Set<Var> originalVars = getAll(varDeps, visibleVars);
//		//Set<Var> overlapVars = Sets.intersection(projectedVars, originalVars);
//		referencedVars.addAll(originalVars);
//
//	} else if(parent instanceof OpProject) {
//		OpProject o = (OpProject)parent;
//		Set<Var> vars = new HashSet<>(o.getVars());
//		Set<Var> removals = new HashSet<>(Sets.difference(varDeps.keySet(), vars));
//		varDeps.removeAll(removals);
//	} else if(parent instanceof OpExtend) { // TODO same for OpAssign
//		OpExtend o = (OpExtend)parent;
//		VarExprList vel = o.getVarExprList();
//
//		Multimap<Var, Var> updates = HashMultimap.create();
//		vel.forEach((v, ex) -> {
//			Set<Var> vars = ExprVars.getVarsMentioned(ex);
//			vars.forEach(w -> {
//				Collection<Var> deps = varDeps.get(w);
//				updates.putAll(w, deps);
//			});
//
//			updates.asMap().forEach((k, w) -> {
//				varDeps.replaceValues(k, w);
//			});
//		});
//
////	} else if(parent instanceof OpAssign) {
////		OpAssign o = (OpAssign)parent;
////		projectedVars.remove(o.getVarExprList().getVars());
//	} else if(parent instanceof OpGroup) {
//		// TODO: This is similar to a projection
//
//		OpGroup o = (OpGroup)parent;
//		// Original variables used in the aggregators are declared as non-unique and referenced
//		List<ExprAggregator> exprAggs = o.getAggregators();
//		exprAggs.forEach(ea -> {
//			Var v = ea.getVar();
//			ExprList el = ea.getAggregator().getExprList();
//			Set<Var> vars = ExprVars.getVarsMentioned(el);
//			Set<Var> origVars = getAll(varDeps, vars);
//			//referencedVars.addAll(origVars);
//			varDeps.putAll(v, origVars);
//			nonUnique.addAll(origVars);
//		});
//
//		// Original variables in the group by expressions are declared as referenced
//		VarExprList vel = o.getGroupVars();
//		vel.forEach((v, ex) -> {
//			Set<Var> vars = ExprVars.getVarsMentioned(ex);
//			Set<Var> origVars = MultiMaps.transitiveGetAll(varDeps.asMap(), vars);
//			referencedVars.addAll(origVars);
//			varDeps.putAll(v, origVars);
//		});
//
//	} else {
////		referencedVars.addAll(availableVars);
//////		isDistinct = false;
////
////
////		System.out.println("Unknown Op type: " + opClass);
////		projectedVars.clear();
//	}
//
//	current = parent;
//}
//
////referencedVars.addAll(varDeps.values());
