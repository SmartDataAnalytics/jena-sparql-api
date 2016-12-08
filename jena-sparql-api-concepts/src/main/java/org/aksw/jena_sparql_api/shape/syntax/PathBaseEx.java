package org.aksw.jena_sparql_api.shape.syntax;

import org.apache.jena.sparql.path.PathBase;
import org.apache.jena.sparql.path.PathVisitor;

public abstract class PathBaseEx
    extends PathBase
{
    @Override
    public void visit(PathVisitor visitor) {
        if(visitor instanceof PathExVisitor) {
            visit((PathExVisitor)visitor);
        } else {
            throw new RuntimeException("Unsuitable path visitor; required " + PathExVisitor.class.getName() + " but got " + (visitor == null ? "null" : visitor.getClass().getName()) + " instead");
        }
    }

    public abstract void visit(PathExVisitor visitor);
}
