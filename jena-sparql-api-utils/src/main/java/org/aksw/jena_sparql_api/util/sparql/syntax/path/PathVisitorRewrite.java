package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

public interface PathVisitorRewrite
    extends PathVisitor
{
    Path getResult();
}
