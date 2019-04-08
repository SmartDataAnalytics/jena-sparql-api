package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.multimaps.MultimapUtils;
import org.aksw.commons.collections.trees.Tree;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class VarUsageAnalyzerVisitor
    extends OpVisitorBase
{
    /**
     * Tree structure over the algebra expression - allows traversal to parent nodes.
     * (internally uses an IdentityHashMap to discriminate between equal sub-trees)
     */
    protected Tree<Op> tree;

    /**
     * The node for which the current information holds.
     * For correct functioning, the next visited node must be the current node's parent.
     */
    protected Op current;

    /**
     * Variables visible at the current node
     */
    protected Set<Var> availableVars;

    /**
     * Variables of the current node that are referenced either in expressions (e.g. filters) or needed for joins
     */
    protected Set<Var> referencedVars;
    protected Set<Var> nonUnique;

    /**
     * Map for tracking assignments, i.e. which variables are required to compute the value of another variable.
     * Newly introduced variables (such as by graph patterns) depend on themselves.
     */
    protected Multimap<Var, Var> varDeps;
    protected Set<Set<Var>> uniqueSets;

    public VarUsageAnalyzerVisitor(Tree<Op> tree, Op current) {
        this(tree, current, OpVars.visibleVars(current));
    }

    public VarUsageAnalyzerVisitor(Tree<Op> tree, Op current, Set<Var> availableVars) {
        this.tree = tree;
        this.current = current;
        this.availableVars = availableVars;
        this.referencedVars = new HashSet<>();
        this.nonUnique = new HashSet<>();
        this.varDeps = HashMultimap.create();
        this.uniqueSets = new HashSet<>();

        availableVars.forEach(v -> varDeps.put(v, v));
    }


    public VarUsage getResult() {
        VarUsage result = new VarUsage(referencedVars, nonUnique, varDeps, uniqueSets);
        return result;
    }

    public void setCurrent(Op current) {
        this.current = current;
    }

    public void processExtend(VarExprList vel) {
        Multimap<Var, Var> updates = HashMultimap.create();

        vel.forEachVarExpr((v, ex) -> {
            Set<Var> vars = ex == null ? Collections.singleton(v) : ExprVars.getVarsMentioned(ex);
            vars.forEach(w -> {
                Collection<Var> deps = varDeps.get(w);
                updates.putAll(v, deps);
            });
        });

        updates.asMap().forEach((k, w) -> {
            varDeps.replaceValues(k, w);
        });

    }

    public void processExprs(ExprList exprs) {
        if(exprs != null) {
            Set<Var> vms = ExprVars.getVarsMentioned(exprs);
            Set<Var> originalVars = MultimapUtils.getAll(varDeps, vms);
            referencedVars.addAll(originalVars);
        }
    }

    public void processExpr(Expr expr) {
        if(expr != null) {
            Set<Var> vms = ExprVars.getVarsMentioned(expr);
            Set<Var> originalVars = MultimapUtils.getAll(varDeps, vms);
            referencedVars.addAll(originalVars);
        }
    }


    public void processJoin(Collection<Op> children) {
        Set<Var> visibleVars = new HashSet<>();
        for(Op child : children) {
            if(child != current) {
                OpVars.visibleVars(child, visibleVars);
            }
        }

        Set<Var> originalVars = MultimapUtils.getAll(varDeps, visibleVars);
        //Set<Var> overlapVars = Sets.intersection(projectedVars, originalVars);
        referencedVars.addAll(originalVars);
    }

    public void processDistinct() {
        Set<Var> vars = varDeps.keySet();

        Set<Var> origVars = MultimapUtils.getAll(varDeps, vars);
        nonUnique.forEach(origVars::remove);

        uniqueSets.add(origVars);
    }

    @Override
    public void visit(OpProject op) {
        List<Var> opVars = op.getVars();

        // Remove all vars except for the projected ones
        Set<Var> removals = new HashSet<>(varDeps.keySet());
        removals.removeAll(opVars);

        removals.forEach(varDeps::removeAll);
    }

    @Override
    public void visit(OpJoin op) {
        Collection<Op> children = tree.getChildren(op);
        processJoin(children);
    }

    @Override
    public void visit(OpLeftJoin op) {
        Collection<Op> children = tree.getChildren(op);
        processJoin(children);
        processExprs(op.getExprs());
    }

    @Override
    public void visit(OpSequence op) {
        Collection<Op> children = tree.getChildren(op);
        processJoin(children);
    }

    @Override
    public void visit(OpFilter op) {
        processExprs(op.getExprs());
    }


    @Override
    public void visit(OpGroup op) {
        List<ExprAggregator> exprAggs = op.getAggregators();
        Multimap<Var, Var> updates = HashMultimap.create();
        exprAggs.forEach(ea -> {
            Var v = ea.getVar();
            ExprList el = ea.getAggregator().getExprList();
            
            // el will be null e.g. for COUNT(*)
            if(el != null) {
                Set<Var> vars = ExprVars.getVarsMentioned(el);
                Set<Var> origVars = MultimapUtils.getAll(varDeps, vars);
                // It is possible that a variable did not occurr in the sub tree under investigation
                // in that case add it to the mapping
                if(origVars.isEmpty()) {
                    origVars = Collections.singleton(v);
                }
                //referencedVars.addAll(origVars);
                updates.putAll(v, origVars);
            }
        });

        updates.asMap().forEach((k, w) -> {
            nonUnique.addAll(w);
            varDeps.replaceValues(k, w);
        });


        VarExprList groupVars = op.getGroupVars();
        processExtend(groupVars);
    }

    @Override
    public void visit(OpExtend op) {
        processExtend(op.getVarExprList());
    }

    @Override
    public void visit(OpAssign op) {
        processExtend(op.getVarExprList());
    }

    @Override
    public void visit(OpOrder op) {
        for(SortCondition sc :op.getConditions()) {
            processExpr(sc.getExpression());
        }
    }


    @Override
    public void visit(OpDistinct op) {
        processDistinct();
    }

    @Override
    public void visit(OpReduced op) {
        processDistinct();
    }

}