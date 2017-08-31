package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.tagmap.TagIndex;
import org.aksw.commons.collections.tagmap.TagIndexImpl;
import org.aksw.commons.graph.index.jena.transform.OpDistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.analysis.DistinctExtendFilter;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TreeMatcher {

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


    public static void covers(TagIndex<Expr, Multimap<Expr, Expr>> viewIndex, TagIndex<Expr, Multimap<Expr, Expr>> userIndex) {
        // The user clauses musts be subsets of the view clauses

        Set<Set<Expr>> nonCoveredViewExprs = new HashSet<>();
        for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> e : viewIndex) {
            Collection<Expr> exprs = new HashSet<>(e.getValue().iterator().next().values());
            nonCoveredViewExprs.add((Set<Expr>)exprs);
        }


        // For every user clause
        for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> userClauseEntry : userIndex) {
            Set<Expr> userClauseSig = userClauseEntry.getKey();
            Multimap<Expr, Expr> userClause = userClauseEntry.getValue().iterator().next();

            System.out.println("Lookup with: " + userClauseSig);
            TagIndex<Expr, Multimap<Expr, Expr>> candViewClauses = viewIndex.getAllSubsetsOf(userClauseSig, false);

            if(candViewClauses.isEmpty()) {
                // no match
            }

            for(Entry<Set<Expr>, Set<Multimap<Expr, Expr>>> candViewClause : candViewClauses) {
                Multimap<Expr, Expr> viewClause = candViewClause.getValue().iterator().next();

                Set<Expr> viewSet = new HashSet<>(viewClause.values());
                nonCoveredViewExprs.remove(viewSet);

                for(Entry<Expr, Collection<Expr>> userSigToExprs : userClause.asMap().entrySet()) {
                    Expr userSig = userSigToExprs.getKey();
                    Collection<Expr> userExprs = userSigToExprs.getValue();
                    Collection<Expr> viewExprs = viewClause.get(userSig);


                    System.out.println("  Match: " + viewExprs + " -> " + userExprs);
                }



                //System.out.println("  Cand: " + viewClause.getKey());
                ///System.out.println(viewClauseCands.size());
            }
        }

        System.out.println("Non covered: " + nonCoveredViewExprs);
    }



    public static void match(OpDistinctExtendFilter viewOp, OpDistinctExtendFilter userOp, Map<Node, Node> iso) {
        DistinctExtendFilter viewDefRaw = viewOp.getDef();
        DistinctExtendFilter viewDef = viewDefRaw.applyNodeTransform(new NodeTransformRenameMap(iso));
        DistinctExtendFilter userDef = userOp.getDef();


        Set<Set<Expr>> viewDnf = viewDef.getFilter().getDnf();
        Set<Set<Expr>> userDnf = userDef.getFilter().getDnf();

        TagIndex<Expr, Multimap<Expr, Expr>> viewIndex = indexDnf(viewDnf);
        TagIndex<Expr, Multimap<Expr, Expr>> userIndex = indexDnf(userDnf);

        covers(viewIndex, userIndex);



        // Validate distinct
        // If view is distinct, and userOp is non distinct -> no match
        if(viewDef.isDistinct()) {
            if(!userDef.isDistinct()) {
                // no match
            } else {
                // track the pending action of making the view distinct
            }
        }


        // Validate user-mandatory variables and view-available variables



        // Validate filters



        //


        // Compute residual filters

    }
}
