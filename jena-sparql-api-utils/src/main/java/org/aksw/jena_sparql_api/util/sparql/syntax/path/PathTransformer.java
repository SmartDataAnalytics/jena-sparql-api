package org.aksw.jena_sparql_api.util.sparql.syntax.path;

import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathVisitor;

public class PathTransformer {


    /** Transform an algebra expression */
    public static Path transform(Path path, PathTransform pathTransform) {
        PathVisitorApplyTransform pathVisitor = new PathVisitorApplyTransform(pathTransform, null);
        Path result = applyTransformation(pathVisitor, path, null, null);
        return result;
    }

    /** The primitive operation to apply a transformation to an Op */
    public static Path applyTransformation(PathVisitorApplyTransform transformVisitor, Path path, PathVisitor beforeVisitor, PathVisitor afterVisitor) {

        PathWalker.walk(path, transformVisitor, beforeVisitor, afterVisitor);
        //PathWalker.walk(path, null, transformVisitor, afterVisitor);
        Path r = transformVisitor.getResult() ;
        return r ;
    }
}
