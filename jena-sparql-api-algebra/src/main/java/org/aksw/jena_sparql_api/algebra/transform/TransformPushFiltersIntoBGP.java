package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.graph.NodeTransformLib;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Given filter expressions of form ?x = <const>, where ?x is an undistinguished (mandatory) variable
 * (i.e. neither indirectly referenced nor projected), push the constant into to BPG's triple patterns
 *
 * Example
 * Select Distinct ?s { ?s ?p ?o . Filter(?p = rdf:type) }
 * becomes
 * Select Distinct ?s { ?s a ?o }
 *
 *
 *
 *
 * @author raven
 *
 */
public class TransformPushFiltersIntoBGP
    extends TransformCopy
{
    //public static final TransformPushFiltersIntoBGP fn = new TransformPushFiltersIntoBGP();

    protected Tree<Op> tree;

    public static Op transform(Op op) {
        Tree<Op> tree = OpUtils.createTree(op);
        Transform transform = new TransformPushFiltersIntoBGP(tree);
        Op result = Transformer.transform(transform, op);
        return result;
    }

    public TransformPushFiltersIntoBGP(Tree<Op> tree) {
        this.tree = tree;
    }


    // TODO Move to a util function
    public static <I, O> Multimap<O, I> group(Iterable<I> items, Function<? super I, O> fn, Predicate<O> exclusions) {
        Multimap<O, I> result = HashMultimap.create();
        for(I item : items) {
            O out = fn.apply(item);
            boolean exclude = exclusions != null && exclusions.test(out);
            if(!exclude) {
                result.put(out, item);
            }
        }

        return result;
    }



    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op result;

        if(subOp instanceof OpQuadPattern || subOp instanceof OpQuadBlock || subOp instanceof OpBGP) {
            VarUsage varUsage = OpUtils.analyzeVarUsage(tree, opFilter);
            //System.out.println("varUsage: " + varUsage);
            Set<Var> mandatoryVars = VarUsage.getMandatoryVars(varUsage);

            ExprList exprs = opFilter.getExprs();

            // TODO: Move most of this analysis into a util function

            Set<Set<Expr>> cnf = CnfUtils.toSetCnf(exprs);

            // Extract all equalities and keep references to the originating clauses
            Multimap<Entry<Var, Node>, Set<Expr>> equalities = group(cnf, CnfUtils::extractEquality, Objects::isNull);

            // Map all variables to their equal constraints (var, constant)
            Multimap<Var, Entry<Var, Node>> varToEntry = group(equalities.keySet(), e -> e.getKey(), null);

            // Determine all consistent variables (those mapping to just a single equals constraint)
            Set<Var> consistentVars = varToEntry.asMap().entrySet().stream()
                    .filter(e -> e.getValue().size() == 1)
                    .map(e -> e.getKey())
                    .collect(Collectors.toSet());

            // Remove all mandatoryVars from the transformation
            consistentVars.removeAll(mandatoryVars);

            // Determine the clauses of all consistent vars
            Set<Set<Expr>> consistentClauses = consistentVars.stream()
                    .flatMap(v -> varToEntry.get(v).stream())
                    .flatMap(e -> equalities.get(e).stream())
                    .collect(Collectors.toSet());

            // Determine the residual clauses which we need to filter by
            Set<Set<Expr>> residualClauses = cnf.stream()
                    .filter(clause -> !consistentClauses.contains(clause))
                    .collect(Collectors.toSet());

            // All variables mentioned in the residual clauses cannot be removed
            // TODO We could substitute them with their constant

            Set<Var> blockedVars = residualClauses.stream()
                .flatMap(clause -> clause.stream().flatMap(expr -> ExprVars.getVarsMentioned(expr).stream()))
                .collect(Collectors.toSet());

            consistentVars.removeAll(blockedVars);

            Set<Set<Expr>> blockedClauses = blockedVars.stream()
                    .flatMap(v -> varToEntry.get(v).stream())
                    .flatMap(e -> equalities.get(e).stream())
                    .collect(Collectors.toSet());

            residualClauses.addAll(blockedClauses);

            // Re-add all forbidden var clauses to the residual clauses


            // Map all consistent variables to their respective value
            Map<Var, Node> map = consistentVars.stream()
                    .collect(Collectors.toMap(
                            v -> v,
                            v -> varToEntry.get(v).iterator().next().getValue()));



            Op newSubOp = map.isEmpty() ? subOp : NodeTransformLib.transform(new NodeTransformRenameMap(map), subOp);

//            System.out.println("OLD SUBOP: " + subOp);
//            System.out.println("NEW SUBOP: " + newSubOp);
            
            ExprList exprList = CnfUtils.toExprList(residualClauses);
            result = OpFilter.filterBy(exprList, newSubOp);

        } else {
            result = super.transform(opFilter, subOp);
        }

        return result;
    }

}


