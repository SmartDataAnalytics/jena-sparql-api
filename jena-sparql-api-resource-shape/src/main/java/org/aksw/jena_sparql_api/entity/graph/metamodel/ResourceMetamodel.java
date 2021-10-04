package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;


@HashId
@ResourceView
public interface ResourceMetamodel
    extends Resource
{
    @IriNs("eg")
    Resource getTargetResource();


    @HashId
    @IriType
    @IriNs("eg")
    String getTargetResourceIri();


    @IriNs("eg")
    @KeyIri("http://www.example.org/graph")
    Map<Node, ResourceGraphMetamodel> byGraph();


    // FIXME HashId cannot traverse into MapViews
    @Iri("http://www.example.org/byGraph")
    Set<ResourceGraphMetamodel> byGraphs();

    /** Whether the key set by byGraph covers all graphs w.r.t. an implicit context */
    @IriNs("eg")
    Boolean isGraphComplete();



    default ResourceGraphMetamodel getOrCreateResourceMetamodel(String iri) {
        return getOrCreateResourceMetamodel(NodeFactory.createURI(iri));
    }

    default ResourceGraphMetamodel getOrCreateResourceMetamodel(Node key) {
        ResourceGraphMetamodel result = byGraph()
                .computeIfAbsent(key, k -> getModel().createResource().as(ResourceGraphMetamodel.class));

        return result;
    }

    /**
     * Attempts to answer an request to the metamodel for whether it knows
     * about critical items. This method does not yield partial answers -
     * if there are critical items then the full set is returned, if there are none then the set is empty, and
     * if not all critical items are known the result is null to indicate that more information is needed
     * (even if some items critical items are already known)
     *
     * Matching is similar fashion to
     * {@link DatasetGraph#find()}. If the request cannot be fully nserved because the
     * meta model is not complete w.r.t. graphs or predicates the result is null.
     *
     *
     *
     * @param s
     * @param g
     * @param isFwd
     * @param p
     * @return
     */
    default Stream<PredicateStats> find(Node g, boolean isFwd, Node p) {

        boolean isGraphComplete = Optional.ofNullable(isGraphComplete()).orElse(false);

        Map<Node, ResourceGraphMetamodel> graphMap = byGraph();

        Stream<ResourceGraphMetamodel> gs = NodeUtils.isNullOrAny(g)
                ? (isGraphComplete ? graphMap.values().stream() : null)
                : Stream.ofNullable(graphMap.get(g));


        Stream<PredicateStats> result = gs.flatMap(gm -> gm.find(isFwd, p));
        return result;
    }

}
