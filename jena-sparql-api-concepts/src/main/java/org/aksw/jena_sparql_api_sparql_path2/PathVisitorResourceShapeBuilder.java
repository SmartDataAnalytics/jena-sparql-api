package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Set;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.PathVisitorBase;

public class PathVisitorResourceShapeBuilder
    extends PathVisitorBase
{
    protected ResourceShapeBuilder rsb;
    protected boolean reverse;

    public PathVisitorResourceShapeBuilder() {
        this(new ResourceShapeBuilder(), false);
    }

    public PathVisitorResourceShapeBuilder(boolean reverse) {
        this(new ResourceShapeBuilder(), reverse);
    }

    public PathVisitorResourceShapeBuilder(ResourceShapeBuilder rsb, boolean reverse) {
        super();
        this.rsb = rsb;
        this.reverse = reverse;
    }

    public ResourceShapeBuilder getResourceShapeBuilder() {
        return rsb;
    }

    @Override
    public void visit(P_ReverseLink path) {
        rsb.nav(path.getNode(), !reverse);
    }

    @Override
    public void visit(P_Link path) {
        rsb.nav(path.getNode(), reverse);
    }

    @Override
    public void visit(P_NegPropSet path) {
        if(!path.getFwdNodes().isEmpty()) {
            Expr expr = new E_NotOneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(path.getFwdNodes()));
            rsb.nav(expr, reverse);
        }

        if(!path.getBwdNodes().isEmpty()) {
            Expr expr = new E_NotOneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(path.getBwdNodes()));
            rsb.nav(expr, !reverse);
        }
    }


    public static void apply(ResourceShapeBuilder rsb, ValueSet<Node> valueSet, boolean reverse) {
        boolean isPositive = valueSet.isPositive();
        Set<Node> nodes = valueSet.getValues();
        if(isPositive) {
            for(Node value : nodes) {
                rsb.nav(value, reverse);
            }
        } else {
            Expr expr = new E_NotOneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(nodes));
            rsb.nav(expr, !reverse);
        }
    }


    public static void apply(ResourceShapeBuilder rsb, PredicateClass predicateClass, boolean reverse) {
       apply(rsb, predicateClass.getFwdNodes(), reverse);
       apply(rsb, predicateClass.getBwdNodes(), !reverse);
    }
}
