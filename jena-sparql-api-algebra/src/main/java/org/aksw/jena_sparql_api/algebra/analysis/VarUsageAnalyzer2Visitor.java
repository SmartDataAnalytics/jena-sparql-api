package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class VarUsageAnalyzer2Visitor
    extends OpVisitorBase
{
    /**
     * Tree structure over the algebra expression - allows traversal to parent nodes.
     * (internally uses an IdentityHashMap to discriminate between equal sub-trees)
     */
//    protected Tree<Op> tree;
//
//    /**
//     * The node for which the current information holds.
//     * For correct functioning, the next visited node must be the current node's parent.
//     */
//    protected Op current;
//
//    /**
//     * Variables visible at the current node
//     */
//    protected Set<Var> availableVars;
//
//    /**
//     * Variables of the current node that are referenced either in expressions (e.g. filters) or needed for joins
//     */
//    protected Set<Var> referencedVars;
//    protected Set<Var> nonUnique;
//
//    /**
//     * Map for tracking assignments, i.e. which variables are required to compute the value of another variable.
//     * Newly introduced variables (such as by graph patterns) depend on themselves.
//     */
//    //protected Multimap<Var, Var> varDeps;
//    protected Set<Set<Var>> uniqueSets;


    protected Map<Op, VarUsage2> opToVarUsage = new IdentityHashMap<>();


//    public VarUsageAnalyzer2Visitor(Tree<Op> tree, Op current) {
//        this(tree, current, OpVars.visibleVars(current));
//    }

    public VarUsageAnalyzer2Visitor() {
//        this.tree = tree;
//        this.current = current;
//        this.availableVars = availableVars;
//        this.referencedVars = new HashSet<>();
//        this.nonUnique = new HashSet<>();
//        //this.varDeps = HashMultimap.create();
//        this.uniqueSets = new HashSet<>();
//
//        availableVars.forEach(v -> varDeps.put(v, v));
    }


    public Map<Op, VarUsage2> getResult() {
        //VarUsage result = new VarUsage(referencedVars, nonUnique, varDeps, uniqueSets);
        return opToVarUsage;
    }

//    public void setCurrent(Op current) {
//        this.current = current;
//    }

    public void processExprs(Op op, ExprList exprs) {
        if(exprs != null) {
            Set<Var> vars = ExprVars.getVarsMentioned(exprs);
            markEssential(op, vars);
            //Set<Var> originalVars = MultimapUtils.getAll(varDeps, vms);
            //referencedVars.addAll(originalVars);
        }
    }

    public void processExpr(Op op, Expr expr) {
        if(expr != null) {
            Set<Var> vars = ExprVars.getVarsMentioned(expr);
            //Set<Var> originalVars = MultimapUtils.getAll(varDeps, vms);
            //referencedVars.addAll(originalVars);
            markEssential(op, vars);
        }
    }


    public void processJoin(Op op, Collection<Op> subOps) {
        for(Op subOp : subOps) {
            subOp.visit(this);
        }

        VarUsage2 varUsage = allocate(op);

        Set<Var> joinVars = null;

        Set<Var> visibleVars = new LinkedHashSet<>();
        for(Op subOp : subOps) {
            VarUsage2 subVarUsage = opToVarUsage.get(subOp);
            if(subVarUsage == null) {
                throw new NullPointerException();
            }

            Set<Var> argVars = subVarUsage.getVisibleVars();
            visibleVars.addAll(argVars);

            joinVars = joinVars == null ? visibleVars : Sets.intersection(joinVars, visibleVars);
        }

        joinVars = new LinkedHashSet<>(joinVars);


        varUsage.setVisibleVars(visibleVars);

        // Mark the join vars as essential on all sub expressions
        //for(Op subOp : subOps) {
            markEssential(op, joinVars);
        //}

        //Set<Var> originalVars = MultimapUtils.getAll(varDeps, visibleVars);
        //Set<Var> overlapVars = Sets.intersection(projectedVars, originalVars);
        //referencedVars.addAll(originalVars);
    }


    public void pushDownDistinct(Op op) {
        VarUsage2 varUsage = opToVarUsage.get(op);

        varUsage.setDistinct(true);

        for(Op subOp : OpUtils.getSubOps(op)) {

            if(subOp instanceof OpGroup) {
                // Do not push the distinct flag into aggregations;
                // in general these depend on duplicates
            } else {
                // TODO We can only push into EXTEND/ASSIGN if the expressions are deterministic

                pushDownDistinct(subOp);
            }
        }
    }

    /**
     * Descend into the sub tree and mark
     *
     */
    public void processDistinct(Op op, Op subOp) {
        subOp.visit(this);
        VarUsage2 varUsage = allocate(op);
        //VarUsage2 subVarUsage = opToVarUsage.get(subOp);

        reuseVisibleVars(varUsage, subOp);
        //varUsage.setVisibleVars(subVarUsage.getVisibleVars());


        // Push down distinct
        pushDownDistinct(op);


//    	Set<Var> vars = varDeps.keySet();
//
//        Set<Var> origVars = MultimapUtils.getAll(varDeps, vars);
//        nonUnique.forEach(origVars::remove);
//
//        uniqueSets.add(origVars);
    }

    @Override
    public void visit(OpJoin op) {
        Collection<Op> subOps = Arrays.asList(op.getLeft(), op.getRight());
        processJoin(op, subOps);
    }

    @Override
    public void visit(OpLeftJoin op) {
        processJoin(op, Arrays.asList(op.getLeft(), op.getRight()));
        processExprs(op, op.getExprs());


//    	throw new UnsupportedOperationException();
//        Collection<Op> subOps = tree.getChildren(op);
//        processJoin(subOps);
//        processExprs(op.getExprs());
    }

    @Override
    public void visit(OpSequence op) {
//    	for(Op subOp : op.getElements()) {
//    		subOp.visit(this);
//    	}
//
//        Collection<Op> children = tree.getChildren(op);
        processJoin(op, op.getElements());
    }


    public void processUnion(Op op, List<Op> subOps) {
        List<VarUsage2> subVarUsages = new ArrayList<>(subOps.size());

        for(Op subOp : subOps) {
            subOp.visit(this);

            VarUsage2 varUsage = opToVarUsage.get(subOp);
            subVarUsages.add(varUsage);
        }

        VarUsage2 varUsage = allocate(op);

        Set<Var> visibleVars = new LinkedHashSet<>();
        for(VarUsage2 subVarUsage : subVarUsages) {
            visibleVars.addAll(subVarUsage.getVisibleVars());
        }

        varUsage.setVisibleVars(visibleVars);
    }

    @Override
    public void visit(OpDisjunction op) {
        List<Op> subOps = OpUtils.getSubOps(op);
        processUnion(op, subOps);
    }

    @Override
    public void visit(OpUnion op) {
        List<Op> subOps = Arrays.asList(op.getLeft(), op.getRight());
        processUnion(op, subOps);
    }


    @Override
    public void visit(OpGroup op) {
        throw new UnsupportedOperationException();
//        List<ExprAggregator> exprAggs = op.getAggregators();
//        Multimap<Var, Var> updates = HashMultimap.create();
//        exprAggs.forEach(ea -> {
//            Var v = ea.getVar();
//            ExprList el = ea.getAggregator().getExprList();
//            Set<Var> vars = ExprVars.getVarsMentioned(el);
//            Set<Var> origVars = MultimapUtils.getAll(varDeps, vars);
//            // It is possible that a variable did not occurr in the sub tree under investigation
//            // in that case add it to the mapping
//            if(origVars.isEmpty()) {
//                origVars = Collections.singleton(v);
//            }
//            //referencedVars.addAll(origVars);
//            updates.putAll(v, origVars);
//        });
//
//        updates.asMap().forEach((k, w) -> {
//            nonUnique.addAll(w);
//            varDeps.replaceValues(k, w);
//        });
//
//
//        VarExprList groupVars = op.getGroupVars();
//        processExtend(groupVars);
    }

    @Override
    public void visit(OpExtend op) {
        processExtend(op, op.getSubOp(), op.getVarExprList());
    }

    @Override
    public void visit(OpExt opExt) {
        Op effectiveOp = opExt.effectiveOp();
        if(effectiveOp == null) {
            throw new IllegalArgumentException("Default handing of OpExt requires an effectiveOp");
        }

        effectiveOp.visit(this);

        VarUsage2 varUsage = opToVarUsage.get(effectiveOp);

        opToVarUsage.put(opExt, varUsage);
    }

    @Override
    public void visit(OpAssign op) {
        processExtend(op, op.getSubOp(), op.getVarExprList());
    }

    @Override
    public void visit(OpOrder op) {
        op.getSubOp().visit(this);
        for(SortCondition sc : op.getConditions()) {
            processExpr(op, sc.getExpression());
        }
    }







    public void processExtend(Op op, Op subOp, VarExprList vel) {
        subOp.visit(this)
        ;
        VarUsage2 varUsage = opToVarUsage.put(op, new VarUsage2());
        VarUsage2 subVarUsage = opToVarUsage.get(subOp);

        Multimap<Var, Var> varDeps = HashMultimap.create();

        Multimap<Var, Var> updates = HashMultimap.create();

        for(Entry<Var, Expr> e : vel.getExprs().entrySet()) {
            Var v = e.getKey();
            Expr ex = e.getValue();

            Set<Var> vars = ex == null ? Collections.singleton(v) : ExprVars.getVarsMentioned(ex);
            for(Var w : vars) {
                Collection<Var> deps = varDeps.get(w);
                updates.putAll(v, deps);
            }
        }

        updates.asMap().forEach((k, w) -> {
            varDeps.replaceValues(k, w);
        });

    }


    public void reuseVisibleVars(VarUsage2 target, Op subOp) {
        VarUsage2 subVarUsage = opToVarUsage.get(subOp);
        Set<Var> vars = subVarUsage.getVisibleVars();
        target.setVisibleVars(vars);
    }

    @Override
    public void visit(OpFilter op) {
        op.getSubOp().visit(this);

        VarUsage2 varUsage = new VarUsage2();
        opToVarUsage.put(op, varUsage);

        reuseVisibleVars(varUsage, op.getSubOp());

        processExprs(op, op.getExprs());
    }

    @Override
    public void visit(OpDistinct op) {
        processDistinct(op, op.getSubOp());
    }

    @Override
    public void visit(OpReduced op) {
        processDistinct(op, op.getSubOp());
    }

    /***
     * Descend into to op-tree and mark all non-required supplied variables as non-required
     *
     * @param op
     * @param clearVars
     */
//    public void clearVars(Op op, Set<Var> clearVars) {
//    	VarUsage2 varUsage = opToVarUsage.get(op);
//
//
//    	//varUsage.
//
//    	for(Op subOp : OpUtils.getSubOps(op)) {
//    		clearVars(subOp, clearVars);
//    	}
//    }


    public void markEssential(Op op, Set<Var> vars) {
        markEssential(opToVarUsage, op, vars);
    }

    /**
     * Marks variables as essential
     *
     * If variables were created through assignment, the dependent variables are marked
     * as essential as well.
     *
     *
     * @param op
     * @param vars
     */
    public static void markEssential(Map<Op, VarUsage2> opToVarUsage, Op op, Set<Var> vars) {
        VarUsage2 varUsage = opToVarUsage.get(op);

        Multimap<Var, Var> varDeps = varUsage.getVarDeps();

        Set<Var> nextVars = varDeps == null ? vars : MultimapUtils.getAll(varDeps, vars);

        varUsage.getEssentialVars().addAll(vars);

        for(Op subOp : OpUtils.getSubOps(op)) {
            markEssential(opToVarUsage, subOp, nextVars);
        }
    }


    public VarUsage2 allocate(Op op) {
        VarUsage2 result = new VarUsage2();
        opToVarUsage.put(op, result);
        return result;
    }

    @Override
    public void visit(OpProject op) {
        op.getSubOp().visit(this);

        VarUsage2 varUsage = allocate(op);
        Set<Var> outVars = new LinkedHashSet<>(op.getVars());

        Set<Var> inVars = opToVarUsage.get(op.getSubOp()).getVisibleVars();

        outVars = Sets.intersection(outVars, inVars);

        varUsage.setVisibleVars(outVars);


//        Set<Var> clearVars = Sets.difference(inVars, outVars);

        //clearVars(op, clearVars);


        // Remove all vars except for the projected ones
//        Set<Var> removals = new HashSet<>(varDeps.keySet());
//        removals.removeAll(opVars);
//
//        removals.forEach(varDeps::removeAll);
    }


    @Override
    public void visit(OpQuadPattern op) {
        VarUsage2 varUsage = allocate(op);

        Set<Var> suppliedVars = OpVars.visibleVars(op);

        varUsage.setVisibleVars(suppliedVars);
    }


    public static Map<Op, VarUsage2> analyze(Op op) {
        VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
        Map<Op, VarUsage2> result = analyze(op, varUsageAnalyzer);
        return result;
    }

    public static Map<Op, VarUsage2> analyze(Op op, VarUsageAnalyzer2Visitor varUsageAnalyzer) {
        //VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
        op.visit(varUsageAnalyzer);

        Map<Op, VarUsage2> result = varUsageAnalyzer.getResult();

        VarUsage2 varUsage = result.get(op);
        Set<Var> visibleVars = varUsage.getVisibleVars();
        markEssential(result, op, visibleVars);

        return result;
    }
}


