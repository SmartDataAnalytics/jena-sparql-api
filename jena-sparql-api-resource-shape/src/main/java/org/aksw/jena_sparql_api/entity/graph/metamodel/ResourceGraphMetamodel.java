package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Information about the ingoing/outgoing properties of a set of resources.
 * The set can be a singleton set
 *
 * @author raven
 *
 */
@HashId
@ResourceView
public interface ResourceGraphMetamodel
    extends Resource
{
    @HashId
    @Inverse
    @Iri("http://www.example.org/byGraph")
    ResourceMetamodel getResourceMetamodel();

    @HashId
    @IriType
    @Iri("http://www.example.org/graph")
    String getGraphIri();
    ResourceGraphMetamodel setGraphIri(String graphIri);


    @Iri("urn:fwd")
    RGDMetamodel getOutgoingStats();

    @Iri("urn:bwd")
    RGDMetamodel getIngoingStats();


    default RGDMetamodel getStats(boolean isFwd) {
        return isFwd ? getOutgoingStats() : getIngoingStats();
    }

    default Stream<PredicateStats> find(boolean isFwd, Node p) {

        RGDMetamodel stats = getStats(isFwd);
        Stream<PredicateStats> result = stats != null
                ? stats.find(p)
                : Stream.empty();
        return result;
    }
}
