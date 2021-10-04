package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/** A class for (incomplete) enumerations - overengineering? */
@ResourceView
public interface Enumeration
    extends Resource
{
    Set<Node> getItems();
    Boolean isComplete();
}
