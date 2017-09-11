package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.tagmap.TagIndex;
import org.aksw.commons.collections.tagmap.TagIndexImpl;
import org.aksw.commons.graph.index.jena.transform.OpDistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.analysis.DistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.utils.ExprHolder;
import org.aksw.jena_sparql_api.unsorted.ExprMatcher;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TreeMatcher {

    public TreeMatching match(OpDisjunction view, OpDisjunction user, Map<Node, Node> iso) {
        return null;

    }




    public static TagIndex<Expr, Multimap<Expr, Expr>> indexDnf(Set<Set<Expr>> dnf) {
    //      if(dnf == null) {
    //          // A disjunction containing an empty conjunction (latter is generally treated as true - if i'm not mistaken)
    //          dnf = Collections.singleton(Collections.emptySet());
    //          //dnf = Collections.emptySet();
    //      }

        TagIndex<Expr, Multimap<Expr, Expr>> result = TagIndexImpl.create((x, y) -> ("" + x).compareTo("" + y));
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
     * Matching of conjunctive expressions.
     * Every viewExpr must be covered by a userExpr
     * If this is not possible for a single viewExpr, the result is unsatisfiable, indicated by a
     * return value of null
     *
     * @param viewExprs
     * @param userExprs
     * @return For each matching user expression the residual expression; null if no match
     */
    public static Entry<Set<Expr>, Set<Expr>> match(Collection<Expr> viewExprs, Collection<Expr> userExprs) {
        //Set<Expr> result = new LinkedHashSet<>();
        //Map<Expr, Expr> result = new LinkedHashMap<>();
        Set<Expr> residualExprs = new LinkedHashSet<>();
        Set<Expr> nonMatchedUserExprs = new HashSet<>(userExprs);

        Entry<Set<Expr>, Set<Expr>> result = new SimpleEntry<>(residualExprs, nonMatchedUserExprs);

        for(Expr viewExpr : viewExprs) {
            Expr tmp = NodeValue.FALSE;
            for(Expr userExpr : userExprs) {
                Expr matchResult = ExprMatcher.match(viewExpr, userExpr);

                nonMatchedUserExprs.remove(userExpr);

                if(!NodeValue.FALSE.equals(matchResult)) {
                    tmp = matchResult;
                    break;
                }
            }

            if(NodeValue.FALSE.equals(tmp)) {
                result = null;
                break;
            }

            if(!NodeValue.TRUE.equals(tmp)) {
                residualExprs.add(tmp);
            }
        }


        return result;
    }

    /**
     * Core algorithm
     * - Every user-clause must cover at least 1 view clause
     *   - A user-clause covers a view-clause if every view-expr _is covered by_ a user-expr (*)
     *     - An expr u covers another v if u is equal-or-more restrictive than v
     *     - All user-exprs that did not cover anything need to be retained
     *
     *
     * (*) We can iterate the user-exprs, and on any cover of a view-expr, we track that
     *
     *
     * @param viewIndex
     * @param userIndex
     */
    public static Set<Set<Expr>> covers(TagIndex<Expr, Multimap<Expr, Expr>> viewIndex, TagIndex<Expr, Multimap<Expr, Expr>> userIndex) {
        // The user clauses musts be subsets of the view clauses

        Set<Set<Expr>> nonCoveredViewExprs = new HashSet<>();
        for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> e : viewIndex) {
            Collection<Expr> exprs = new HashSet<>(e.getValue().iterator().next().values());
            nonCoveredViewExprs.add((Set<Expr>)exprs);
        }

        Set<Set<Expr>> residualDnf = new HashSet<>();

        // For every user clause
        for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> userClauseEntry : userIndex) {
            Set<Expr> userClauseSig = userClauseEntry.getKey();
            Multimap<Expr, Expr> userClauseIndex = userClauseEntry.getValue().iterator().next();

            System.out.println("Lookup with: " + userClauseSig);
            TagIndex<Expr, Multimap<Expr, Expr>> candViewClauses = viewIndex.getAllSubsetsOf(userClauseSig, false);
            //TagIndex<Expr, Multimap<Expr, Expr>> candViewClauses = viewIndex.getAllSubsetsOf(userClauseSig, false);

            if(candViewClauses.isEmpty()) {
                // no match
            }


            // For every candidate view clause: check whether any of them is covered
            for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> viewIndexEntry : candViewClauses) {
                Multimap<Expr, Expr> viewClauseIndex = viewIndexEntry.getValue().iterator().next();

                //Set<Expr> viewExprs = new HashSet<>(viewClauseIndex.values());
                //nonCoveredViewExprs.remove(viewSet);


                Set<Expr> residualClause = new HashSet<>();


                // Check whether all view clauses are covered; if so, then afterwards add all user clauses
                for(Entry<Expr, Collection<Expr>> viewSigToExprs : viewClauseIndex.asMap().entrySet()) {
                    Expr sig = viewSigToExprs.getKey();
                    // In order for a view clause to be covered, all its expressions must be covereduserSig
                    //Collection<Expr> viewExprs = viewSet;//viewClause.get(userSig);
                    Collection<Expr> viewExprs = viewSigToExprs.getValue();

                    // Check whether any of the candidate userExprs covers the view expr
                    Collection<Expr> userCandExprs = userClauseIndex.get(sig);//.getValue();


                    Entry<Set<Expr>, Set<Expr>> matchResult = match(viewExprs, userCandExprs);

                    if(matchResult != null) {
                        Set<Expr> residualExprs = matchResult.getKey();
                        Set<Expr> residualUserExprs = matchResult.getValue();

                        residualClause.addAll(residualExprs);
                        residualClause.addAll(residualUserExprs);

                    } else {
                        residualClause = null;
                        break;
                    }
                }

                if(residualClause != null) {
                    residualDnf.add(residualClause);
                    break;
                }
            }

        }

        return residualDnf;

        //System.out.println("Residual DNF" + residualDnf);
        //System.out.println("Non covered: " + nonCoveredViewExprs);
    }


    /**
     * The userVel may define more variables than the view,
     * however, all referenced variables need to be provided in the result set.
     *
     * @param viewVel
     * @param userVel
     * @return
     */
    public static Map<Var, Expr> matchVarExprList(Map<Var, Expr> viewVel, Map<Var, Expr> userVel) {
        Map<Var, Expr> result = new HashMap<>();

        for(Entry<Var, Expr> userVe : userVel.entrySet()) {
            Expr userExpr = userVe.getValue();
            Var userVar = userVe.getKey();
            Expr viewExpr = viewVel.get(userVar);

            if(Objects.equal(viewExpr, userExpr)) {
                //result.put(userVar, userExpr);
                result.put(userVar, new ExprVar(userVar));
            } else {
                result = null;
                break;
            }
        }

        return result;
    }


    /**
     *
     *
     * @param viewOp
     * @param userOp
     * @param iso
     * @return
     */
    public static DistinctExtendFilter match(OpDistinctExtendFilter viewOp, OpDistinctExtendFilter userOp, Map<Node, Node> iso) {
        DistinctExtendFilter viewDefRaw = viewOp.getDef();
        DistinctExtendFilter viewDef = viewDefRaw.applyNodeTransform(new NodeTransformRenameMap(iso));
        DistinctExtendFilter userDef = userOp.getDef();

        Set<Set<Expr>> viewDnf = viewDef.getFilter().getDnf();
        Set<Set<Expr>> userDnf = userDef.getFilter().getDnf();

        TagIndex<Expr, Multimap<Expr, Expr>> viewIndex = indexDnf(viewDnf);
        TagIndex<Expr, Multimap<Expr, Expr>> userIndex = indexDnf(userDnf);

        // Validate filters
        Set<Set<Expr>> residualDnf = covers(viewIndex, userIndex);

        System.out.println("Residual Dnf: " + residualDnf);

        if(residualDnf == null) {
            // No match
        }



        // Validate distinct
        // If view is distinct, and userOp is non distinct -> no match
        Map<Var, Expr> userPreDistinctVel = userDef.getPreDistinctVarDefs();

        Map<Var, Expr> actions;
        if(userDef.isDistinct()) {
            throw new UnsupportedOperationException();
            //Map<Var, Expr> userPreVel = userDef.getPreDistinctVarDefs();

//            if(viewDef.isDistinct()) {
//
//            } else {
//
//            }
        } else {
            if(viewDef.isDistinct()) {
//              // no match
                actions = null;
            } else {
                Map<Var, Expr> viewPreDistinctVel = viewDef.getPreDistinctVarDefs();

                actions = matchVarExprList(viewPreDistinctVel, userPreDistinctVel);

                //System.out.println("Actions: " + actions);
            }
        }

//        if(viewDef.isDistinct()) {
//            if(!userDef.isDistinct()) {
//                // no match
//            } else {
//                // track the pending action of making the view distinct
//            }
//        }


        // Validate user-mandatory variables and view-available variables

        // Note: Extensions may introduce new isomorphisms - however, we can make use of the defining expressions:




        // The user query may extend over the view


        //TreeMatching result = new TreeMatching(ExprHolder.fromDnf(residualDnf));



        DistinctExtendFilter result = actions != null && residualDnf != null
                ? new DistinctExtendFilter(actions, null, ExprHolder.fromDnf(residualDnf))
                : null;

        System.out.println("Result: " + result);

        return result;
    }
}







//
//                    Set<Expr> residualClause = new HashSet<>();
//                    for(Expr userExpr : userCandExprs) {
//                        Expr residualUserExpr = NodeValue.FALSE;
//                        // We just need one non-false residual match
//                        // TODO If there are multiple matches, we could try to chose the best one
//                        // based on subsumption
//                        for(Expr viewExpr : viewExprs) {
//                            residualUserExpr = ExprMatcher.match(viewExpr, userExpr);
//
//                            System.out.println("  Match: " + viewExprs + " -> " + userCandExprs + " rest: " + residualUserExpr);
//
//                            if(!residualUserExpr.equals(NodeValue.FALSE)) {
//                                break;
//                            }
//                        }
//
//                        // Clause match is not satisfiable, indicate by setting to null
//                        if(residualUserExpr.equals(NodeValue.FALSE)) {
//                            residualClause = null;
//                            break;
//                        }
//
//                        residualClause.add(residualUserExpr);
//                    }
//
//                    if(residualClause != null) {
//                        residualDnf.add(residualClause);
//                    }
                //System.out.println("  Cand: " + viewClause.getKey());
                ///System.out.println(viewClauseCands.size());
