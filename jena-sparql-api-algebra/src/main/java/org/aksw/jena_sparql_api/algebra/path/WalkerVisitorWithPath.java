package org.aksw.jena_sparql_api.algebra.path;

import java.util.Iterator;

import org.aksw.commons.path.core.Path;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.walker.ExprVisitorFunction;
import org.apache.jena.sparql.algebra.walker.OpVisitorByTypeAndExpr;
import org.apache.jena.sparql.algebra.walker.WalkerVisitor;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprFunction0;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunction3;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprNone;
import org.apache.jena.sparql.expr.ExprTripleTerm;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.ExprVisitorBase;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Extension of {@link WalkerVisitor} which assigns paths to the encountered ops
 * Walk algebra and expressions */
public class WalkerVisitorWithPath implements OpVisitorByTypeAndExpr, ExprVisitorFunction {
    protected final ExprVisitor exprVisitor ;
    protected final OpVisitor   opVisitor ;
    protected int               opDepthLimit      = Integer.MAX_VALUE ;
    protected int               exprDepthLimit    = Integer.MAX_VALUE ;

    protected int               opDepth      = 0 ;
    protected int               exprDepth    = 0 ;

    private final OpVisitor     beforeVisitor ;
    private final OpVisitor     afterVisitor ;

    protected PathState         pathState;

    public static String getLabel(Op op) {
        return op.getName();
    }


    public void pushPath(String parentLabel, Op op) {
        Path<String> parent = pathState.getPath();
        Path<String> path = parent.resolve(parentLabel);

        pathState.setPath(path);
        pathState.getPathToOp().put(path, op);
        pathState.getParentToChildren().put(parent, path);
    }

    public void popPath() {
        pathState.setPath(pathState.getPath().getParent());
    }

    /**
     * A walker. If a visitor is null, then don't walk in. For
     * "no action but keep walking inwards", use {@link OpVisitorBase} and
     * {@link ExprVisitorBase}.
     *
     * @see OpVisitorBase
     * @see ExprVisitorBase
     */
    public WalkerVisitorWithPath(
            PathState pathState,
            OpVisitor opVisitor,
            ExprVisitor exprVisitor,
            OpVisitor before,
            OpVisitor after) {
        this.opVisitor = opVisitor ;
        this.exprVisitor = exprVisitor ;
        if ( opDepthLimit < 0 )
            opDepthLimit = Integer.MAX_VALUE ;
        if ( exprDepth < 0 )
            exprDepthLimit = Integer.MAX_VALUE ;
        opDepth = 0 ;
        exprDepth = 0 ;
        beforeVisitor = before ;
        afterVisitor = after ;

        this.pathState = pathState;
    }


    protected final void before(Op op) {
        if ( beforeVisitor != null )
            op.visit(beforeVisitor) ;
    }

    protected final void after(Op op) {
        if ( afterVisitor != null )
            op.visit(afterVisitor) ;
    }

    public void walk(Op op) {
        if ( op == null )
            return ;
        if ( opDepth == opDepthLimit )
            // No deeper.
            return ;
        opDepth++ ;
        try { op.visit(this); }
        finally { opDepth-- ; }
    }

    public void walk(Expr expr) {
        if ( expr == null )
            return ;
        if ( exprDepth == exprDepthLimit )
            return ;
        exprDepth++ ;
        try { expr.visit(this) ; }
        finally { exprDepth-- ; }
    }

    public void walk(ExprList exprList) {
        if ( exprList == null )
            return ;
        exprList.forEach(e->walk(e));
    }

    public void walk(VarExprList varExprList) {
        if ( varExprList == null )
            return ;
        // retains order.
        varExprList.forEachVarExpr((v,e) -> {
            Expr expr = (e!=null) ? e : Expr.NONE ;
            walk(expr) ;
        });
    }

    // ---- Mode swapping between op and expr. visit=>walk
    @Override
    public void visitExpr(ExprList exprList) {
        if ( exprVisitor != null )
            walk(exprList) ;
    }

    @Override
    public void visitVarExpr(VarExprList varExprList) {
        if ( exprVisitor != null )
            walk(varExprList);
    }

    // ----

    public void visitOp(Op op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(this);
        after(op) ;
    }

    @Override
    public void visit0(Op0 op) {
        before(op) ;
        if ( opVisitor != null ) {
            pushPath(getLabel(op), op);
            op.visit(opVisitor) ;
            popPath();
        }
        after(op) ;
    }

    @Override
    public void visit1(Op1 op) {
        before(op) ;
        visit1$(op) ;
        after(op) ;
    }

    // Can be called via different routes.
    private void visit1$(Op1 op) {
        String parentLabel = getLabel(op);
        if ( op.getSubOp() != null ) {
            pushPath(parentLabel, op.getSubOp());
            op.getSubOp().visit(this) ;
            popPath();
        }
        if ( opVisitor != null )
            op.visit(opVisitor) ;
    }

    @Override
    public void visit2(Op2 op) {
        String parentLabel = getLabel(op);

        before(op) ;
        if ( op.getLeft() != null ) {
            pushPath(parentLabel + "0", op.getLeft());
            op.getLeft().visit(this) ;
            popPath();
        }
        if ( op.getRight() != null ) {
            pushPath(parentLabel + "1", op.getRight());
            op.getRight().visit(this) ;
            popPath();
        }
        if ( opVisitor != null ) {
            op.visit(opVisitor) ;
        }
        after(op) ;
    }

    @Override
    public void visitN(OpN op) {
        String parentLabel = getLabel(op);

        before(op) ;
        int i = 0;
        for (Iterator<Op> iter = op.iterator(); iter.hasNext(); ++i) {
            Op sub = iter.next() ;
            String childLabel = parentLabel + i;

            pushPath(childLabel, sub);
            sub.visit(this) ;
            popPath();
        }
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visitExt(OpExt op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visit(OpOrder opOrder) {
        // XXX Why not this?
        // ApplyTransformVisitor handles the parts of OpOrder.
//        before(opOrder) ;
//        visitSortConditions(opOrder.getConditions()) ;
//        visitModifer(opOrder);
//        visit1$(opOrder);
//        after(opOrder) ;
        visit1(opOrder) ;
    }

    @Override
    public void visit(OpAssign opAssign) {
        before(opAssign) ;
        VarExprList varExpr = opAssign.getVarExprList() ;
        visitVarExpr(varExpr);
        visit1$(opAssign) ;
        after(opAssign) ;
    }

    @Override
    public void visit(OpExtend opExtend) {
        before(opExtend) ;
        VarExprList varExpr = opExtend.getVarExprList() ;
        visitVarExpr(varExpr);
        visit1$(opExtend) ;
        after(opExtend) ;
    }

    // Transforming to quads needs the graph node handled before doing the sub-algebra ops
    // so it has to be done as before/after by the Walker. By the time visit(OpGraph) is called,
    // the sub-tree has already been visited.

//    @Override
//    public void visit(OpGraph op) {
//        pushGraph(op.getNode()) ;
//        OpVisitorByTypeAndExpr.super.visit(op) ;
//        popGraph() ;
//    }
//
//    private Deque<Node> stack = new ArrayDeque<>() ;
//
//    public Node getCurrentGraph() { return stack.peek() ; }
//
//    private void pushGraph(Node node) {
//        stack.push(node) ;
//    }
//
//    private void popGraph() {
//        stack.pop() ;
//    }

    @Override
    public void visit(ExprFunction0 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction1 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction2 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction3 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunctionN func) { visitExprFunction(func) ; }

    @Override
    public void visitExprFunction(ExprFunction func) {
        for ( int i = 1 ; i <= func.numArgs() ; i++ ) {
            Expr expr = func.getArg(i) ;
            if ( expr == null )
                // Put a dummy in, e.g. to keep the transform stack aligned.
                Expr.NONE.visit(this) ;
            else
                expr.visit(this) ;
        }
        if ( exprVisitor != null )
            func.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprFunctionOp funcOp) {
        walk(funcOp.getGraphPattern());
        if ( exprVisitor != null )
            funcOp.visit(exprVisitor) ;
    }

    @Override
    public void visit(NodeValue nv) {
        if ( exprVisitor != null )
            nv.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprTripleTerm exTripleTerm) {
        if ( exprVisitor != null )
            exTripleTerm.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprVar v) {
        if ( exprVisitor != null )
            v.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprNone none) {
        if ( exprVisitor != null )
            none.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprAggregator eAgg) {
        // This is the assignment variable of the aggregation
        // not a normal variable of an expression.
        visitAssignVar(eAgg.getAggVar().asVar()) ;
        if ( exprVisitor != null )
            eAgg.visit(exprVisitor) ;
    }
}