package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface GraphPredicateStats
    extends Resource
{
    @IriNs("eg")
    Node getGraph();
    GraphPredicateStats setGraph(Node node);

    @IriNs("eg")
    Long getDistinctValueCount();
    GraphPredicateStats setDistinctValueCount(Long count);

    @IriNs("eg")
    Boolean isDistinctValueCountMinimum();
    GraphPredicateStats setDistinctValueCountMinimum(Boolean noOrYes);
}
