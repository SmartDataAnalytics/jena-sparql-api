package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Returns an RDF resource (together with its model) and a map from
 * placeholder nodes to associated information
 *
 *
 * @author raven
 *
 */
public class UnresolvedResource {
    protected Resource resource;
    protected Map<RDFNode, Object> placeholders;

    public UnresolvedResource() {
        this(ModelFactory.createDefaultModel().createResource());
    }

    public UnresolvedResource(Resource resource) {
        this(resource, new HashMap<>());
    }

    public UnresolvedResource(Resource resource,
            Map<RDFNode, Object> placeholders) {
        super();
        this.resource = resource;
        this.placeholders = placeholders;
    }

    public Resource getResource() {
        return resource;
    }

    public Map<RDFNode, Object> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String toString() {
        return "UnresolvedResource [resource=" + resource + ", placeholders="
                + placeholders + "]";
    }
}
