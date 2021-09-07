package org.aksw.jena_sparql_api.path.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOps;
import org.aksw.jena_sparql_api.concepts.UnaryXExpr;
import org.aksw.jena_sparql_api.concepts.UnaryXExprImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Coalesce;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;


/**
 * Implementation of {@link PathOps} which allows for using the path machinery
 *e used should be "?x".
 *
 * Allows paths of the form
 * / ?x IN (a b c) / REGEX(?x, "foo") / ?x = TRUE / ...
 *
 * @author raven
 *
 */
public class PathOpsPE
    implements PathOps<UnaryXExpr, PathPE>
{
    public static final Var VAR_X = Var.alloc("x");
    public static final ExprVar EXPR_X = new ExprVar(VAR_X);

    public static final UnaryXExpr PARENT = UnaryXExprImpl.create(
            new E_Equals(new ExprVar("PARENT"), NodeValue.TRUE));

    public static final UnaryXExpr SELF = UnaryXExprImpl.create(
            new E_Equals(new ExprVar("SELF"), NodeValue.TRUE));

    private static PathOpsPE INSTANCE = null;

    public static PathOpsPE get() {
        if (INSTANCE == null) {
            synchronized (PathOpsPE.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PathOpsPE();
                }
            }
        }
        return INSTANCE;
    }


    /** Convenience static shorthand for .get().newRoot() */
    public static PathPE newAbsolutePath() {
        return get().newRoot();
    }

    public static PathPE newRelativePath() {
        return get().newPath(false, Collections.emptyList());
    }

    @Override
    public PathPE upcast(Path<UnaryXExpr> path) {
        return (PathPE)path;
    }

    @Override
    public List<UnaryXExpr> getBasePathSegments() {
        return Collections.emptyList();
    }

    @Override
    public Comparator<UnaryXExpr> getComparator() {
        return Comparator.comparing(Object::toString);
    }

    @Override
    public PathPE newPath(boolean isAbsolute, List<UnaryXExpr> segments) {
        return new PathPE(this, isAbsolute, segments);
    }

    @Override
    public PathPE newPath(UnaryXExpr element) {
        return newPath(false, Collections.singletonList(element));
    }

    @Override
    public UnaryXExpr getSelfToken() {
        return SELF;
    }

    @Override
    public UnaryXExpr getParentToken() {
        return PARENT;
    }

    @Override
    public String toString(PathPE path) {
        ExprList el = new ExprList();
        for(UnaryXExpr ue : path.getSegments()) {
            el.add(ue.getExpr());
        }

        // We use an n-ary 'wrapper' expression to serialize the list of segments
        // FIXME exploiting coalesce is obviously a hack; use some custom function IRI
        Expr wrapper = new E_Coalesce(el);

        String result = (path.isAbsolute() ? "/" : "") + ExprUtils.fmtSPARQL(wrapper);

        return result;
    }

    @Override
    public PathPE fromString(String str) {
        str = str.trim();

        boolean isAbsolute = false;

        if (str.startsWith("/")) {
            isAbsolute = true;
            str = str.substring(1);
        }

        Expr expr = ExprUtils.parse(str);

        ExprFunctionN fn = (ExprFunctionN)expr;
        List<Expr> args = fn.getArgs();
        List<UnaryXExpr> l = new ArrayList<>(args.size());
        for (Expr e : fn.getArgs()) {
            l.add(UnaryXExprImpl.create(e));
        }

        return newPath(isAbsolute, l);
    }


}
