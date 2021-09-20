package org.aksw.jena_sparql_api.path.util;

import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.apache.jena.graph.Node;

/** Utils for working with {@code Path<Node>} */
public class PathNodeUtils {
    /**
     * Collects all nodes in a {@code Path<Node>} object.
     * Descends into literals that represent property paths.
     * TODO Descend into literals representing queries
     *
     */
    public static Set<Node> collectNodes(Path<Node> path) {
        Set<Node> result = new LinkedHashSet<>();
        for (Path<Node> item : path) {
            Node node = item.toSegment();

            Object obj = node.isLiteral() ? node.getLiteralValue() : null;

            if (obj instanceof org.apache.jena.sparql.path.Path) {
                org.apache.jena.sparql.path.Path p = (org.apache.jena.sparql.path.Path)obj;
                Set<Node> contrib = PathUtils.collectNodes(p);
                result.addAll(contrib);
            } else {
                result.add(node);
            }
        }

        return result;
    }

}
