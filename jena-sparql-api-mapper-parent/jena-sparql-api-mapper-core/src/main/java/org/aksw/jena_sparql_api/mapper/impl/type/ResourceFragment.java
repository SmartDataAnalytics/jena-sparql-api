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
public class ResourceFragment {
    protected Resource resource;
    protected Map<RDFNode, PlaceholderInfo> placeholders;

    public ResourceFragment() {
        this(ModelFactory.createDefaultModel().createResource());
    }

    public ResourceFragment(Resource resource) {
        this(resource, new HashMap<>());
    }

    public ResourceFragment(Resource resource,
            Map<RDFNode, PlaceholderInfo> placeholders) {
        super();
        this.resource = resource;
        this.placeholders = placeholders;
    }

    public Resource getResource() {
        return resource;
    }

    public Map<RDFNode, PlaceholderInfo> getPlaceholders() {
        return placeholders;
    }

    @Override
    public String toString() {
        return "UnresolvedResource [resource=" + resource + ", placeholders="
                + placeholders + "]";
    }
}
