package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.KeyIri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * The metamodel captures for a set of resources
 * their ingoing/outgoing predicates and the number of values for each predicates.
 * The metamodel does not
 *
 * @author raven
 *
 */
@ResourceView
public interface DatasetMetamodel
    extends Resource
{
    @IriNs("eg")
    @KeyIri("urn:resource")
    // @ValueIri("urn:value")
    Map<Node, ResourceGraphMetamodel> getResourceMetamodels();

    default ResourceGraphMetamodel getOrCreateResourceMetamodel(String iri) {
        return getOrCreateResourceMetamodel(NodeFactory.createURI(iri));
    }

    default ResourceGraphMetamodel getOrCreateResourceMetamodel(Node key) {
        ResourceGraphMetamodel result = getResourceMetamodels()
                .computeIfAbsent(key, k -> getModel().createResource().as(ResourceGraphMetamodel.class));

        return result;
    }
}
