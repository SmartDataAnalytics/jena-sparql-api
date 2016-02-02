package org.aksw.jena_sparql_api_sparql_path2;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.aksw.jena_sparql_api.utils.ExprListUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_NegPropSet;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.PathVisitorBase;

public class PathVisitorResourceShapeBuilder
    extends PathVisitorBase
{
    protected ResourceShapeBuilder rsb;

    public PathVisitorResourceShapeBuilder() {
        this(new ResourceShapeBuilder());
    }

    public ResourceShapeBuilder getResourceShapeBuilder() {
        return rsb;
    }

    public PathVisitorResourceShapeBuilder(ResourceShapeBuilder rsb) {
        super();
        this.rsb = rsb;
    }

    @Override
    public void visit(P_NegPropSet path) {
        if(!path.getFwdNodes().isEmpty()) {
            rsb.outgoing(new E_NotOneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(path.getFwdNodes())));
        }

        if(!path.getBwdNodes().isEmpty()) {
            rsb.incoming(new E_NotOneOf(new ExprVar(Vars.p), ExprListUtils.nodesToExprs(path.getBwdNodes())));
        }
    }

    @Override
    public void visit(P_ReverseLink path) {
        rsb.incoming(path.getNode());
    }

    @Override
    public void visit(P_Link path) {
        rsb.outgoing(path.getNode());
    }

}
