package org.aksw.jena_sparql_api.algebra.path;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathOpsStr;
import org.apache.jena.sparql.algebra.Op;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;


public class PathState {
    protected Path<String> path;
    protected Map<Path<String>, Op> pathToOp;

    // Parent to child map can actually be computed on demand
    protected Multimap<Path<String>, Path<String>> parentToChildren;

    public PathState() {
        super();
        this.path = PathOpsStr.newAbsolutePath();
        this.pathToOp = new LinkedHashMap<>();
        this.parentToChildren = LinkedHashMultimap.create();
    }

    public static PathState create(Op rootOp) {
        PathState result = new PathState();
        result.getPathToOp().put(result.getPath(), rootOp);
        return result;
    }

    public Path<String> getPath() {
        return path;
    }

    public Map<Path<String>, Op> getPathToOp() {
        return pathToOp;
    }

    public Multimap<Path<String>, Path<String>> getParentToChildren() {
        return parentToChildren;
    }

    public void setPath(Path<String> path) {
        this.path = path;
    }

    public void setPathToOp(Map<Path<String>, Op> pathToOp) {
        this.pathToOp = pathToOp;
    }

    public void setParentToChildren(Multimap<Path<String>, Path<String>> parentToChildren) {
        this.parentToChildren = parentToChildren;
    }

}
