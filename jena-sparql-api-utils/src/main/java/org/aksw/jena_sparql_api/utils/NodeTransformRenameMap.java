package org.aksw.jena_sparql_api.utils;

import java.util.Map;

import org.aksw.commons.collections.MapUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

public class NodeTransformRenameMap
    implements NodeTransform {

    private final Map<? extends Node, ?extends Node> map;

    public NodeTransformRenameMap(Map<? extends Node, ? extends Node> map)
    {
        this.map = map;
    }

    public final Node convert(Node node)
    {
        return MapUtils.getOrElse(map, node, node);
    }
}
