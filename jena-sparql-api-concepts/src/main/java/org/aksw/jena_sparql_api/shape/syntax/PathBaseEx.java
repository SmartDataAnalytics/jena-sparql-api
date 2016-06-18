package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.PathBase;
import org.apache.jena.sparql.path.PathVisitor;

public abstract class PathBaseEx
    extends PathBase
{
    @Override
    public void visit(PathVisitor visitor) {
        if(visitor instanceof PathVisitorEx) {
            visit((PathVisitorEx)visitor);
        } else {
            throw new RuntimeException("Unsuitable path visitor; required " + PathVisitorEx.class.getName() + " but got " + (visitor == null ? "null" : visitor.getClass().getName()) + " instead");
        }
    }

    public abstract void visit(PathVisitorEx visitor);
}
