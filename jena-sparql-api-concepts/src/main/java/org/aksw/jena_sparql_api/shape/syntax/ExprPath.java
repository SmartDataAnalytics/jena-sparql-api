package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprNode;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.path.Path;

/**
 * Expression making use of a path
 *
 * @author raven
 *
 */
public class ExprPath
    extends ExprNode
{
    protected Path path;

    public ExprPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void visit(ExprVisitor visitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public NodeValue eval(Binding binding, FunctionEnv env) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean equals(Expr other, boolean bySyntax) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Expr copySubstitute(Binding binding) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform transform) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * This object behaves like a variable
     */
    @Override
    public boolean isVariable()         { return true ; }


}
