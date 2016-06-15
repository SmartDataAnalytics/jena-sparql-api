package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementWalker.Walker;

public class PathWalker
{
    public static void walk(Path path, PathVisitor visitor)
    {
        walk(path, visitor, null, null) ;
    }

    public static void walk(Path path, PathVisitor visitor, PathVisitor beforeVisitor, PathVisitor afterVisitor)
    {
        PathVisitorWalker w = new PathVisitorWalker(visitor, beforeVisitor, afterVisitor) ;
        path.visit(w) ;
    }

    protected static void walk$(Element el, Walker walker)
    {
        el.visit(walker) ;
    }
}
