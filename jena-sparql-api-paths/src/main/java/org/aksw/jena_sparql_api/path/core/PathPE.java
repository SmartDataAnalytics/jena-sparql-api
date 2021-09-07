package org.aksw.jena_sparql_api.path.core;

import java.util.List;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.aksw.jena_sparql_api.concepts.UnaryXExprImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.util.ExprUtils;

/**
 * Dedicated path implementation for SPARQL predicate expressions (SPARQL expressions only having a single variable).
 * Expressions should uniformly use the variable "?x".
 *
 *
 * @author raven
 *
 */
public class PathPE
    extends PathBase<UnaryXExpr, PathPE>
{
    public PathPE(PathOps<UnaryXExpr, PathPE> pathOps, boolean isAbsolute, List<UnaryXExpr> segments) {
        super(pathOps, isAbsolute, segments);
    }

    public PathPE resolve(Resource other) {
        return resolve(other.asNode());
    }

    public PathPE resolve(Node node) {
        return resolve(new E_Equals(PathOpsPE.EXPR_X, ExprLib.nodeToExpr(node)));
    }

    /** Resolves a single segment from str; default resolves can resolve a full path serialized as a string */
    public PathPE resolveSegment(String str) {
        Expr expr = ExprUtils.parse(str);
        if (expr.isConstant()) {
            expr = new E_Equals(PathOpsPE.EXPR_X, expr);
        }

        return resolve(expr);
    }

    public PathPE resolve(Expr expr) {

        UnaryXExpr ue = UnaryXExprImpl.create(expr);
        return resolve(ue);
    }

    /** Append an ?x = ?x step which matches all values at this segment */
    public PathPE resolveAll() {
        return resolve(PathOpsPE.VAR_X);
    }
}
