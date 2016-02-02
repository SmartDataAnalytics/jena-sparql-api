package org.aksw.jena_sparql_api_sparql_path2;

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
