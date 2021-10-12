package org.aksw.jena_sparql_api.algebra.path;

import org.aksw.commons.path.core.Path;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.TransformCopy;

public class TransformCopyWithPath
    extends TransformCopy
{
    protected PathState pathState;

    protected Path<String> path() {
        return pathState.getPath();
    }

    public TransformCopyWithPath(PathState pathState) {
        this(pathState, false);
    }

    public TransformCopyWithPath(PathState pathState, boolean alwaysDuplicate) {
        super(alwaysDuplicate);
        this.pathState = pathState;
    }

    public OpVisitor getBeforeVisitor() {
        return null;
    }

}
