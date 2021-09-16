package org.aksw.jena_sparql_api.path.core;

import java.util.List;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Dedicated path implementation compatible with {@link Path}&lt;Node&gt;.
 *
 * @author raven
 *
 */
public class PathNode
    extends PathBase<Node, PathNode>
{
    public PathNode(PathOps<Node, PathNode> pathOps, boolean isAbsolute, List<Node> segments) {
        super(pathOps, isAbsolute, segments);
    }

    /** Convenience method for {@link Resource} */
    public PathNode resolve(Resource other) {
        return resolve(other.asNode());
    }
}
