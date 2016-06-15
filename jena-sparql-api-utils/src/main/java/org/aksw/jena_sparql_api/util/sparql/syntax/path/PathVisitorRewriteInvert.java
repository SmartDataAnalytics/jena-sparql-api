package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.Path;

public class PathVisitorRewriteInvert
    extends PathVisitorRewriteBase
{
    @Override
    public void visit(P_Inverse path) {
        Path subPath = path.getSubPath();
        result = PathVisitorInvert.apply(subPath);
    }
}
