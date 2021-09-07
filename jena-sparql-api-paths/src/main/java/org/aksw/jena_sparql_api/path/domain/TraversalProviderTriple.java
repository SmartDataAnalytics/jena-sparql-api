package org.aksw.jena_sparql_api.path.domain;

import org.aksw.commons.path.core.Path;
import org.apache.jena.graph.Node;


/**
 * The idea is to have domain specific traversal classes which however
 * are backed by conventional traversal */
public interface TraversalProviderTriple<
    B extends TraversalNode<?>,
    TA extends TraversalAlias<TD>,
    TD extends TraversalDirection<TA, TP>,
    TP extends TraversalProperty<TA>
    >
{
    TD root();

    TP toProperty(TD x, Node direction);
    TA toAlias(TP x, Node property);
    TD toValues(TA x, Node alias);

    B resolve(Path<Node> path);
}
