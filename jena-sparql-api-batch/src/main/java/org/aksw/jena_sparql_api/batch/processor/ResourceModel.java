package org.aksw.jena_sparql_api.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * Combines a resource with a model
 *
 * @author raven
 *
 */
public class ResourceModel {
    private Resource resource;
    private Model model;

    public ResourceModel(Resource resource, Model model) {
        super();
        this.resource = resource;
        this.model = model;
    }

    public Resource getResource() {
        return resource;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result
                + ((resource == null) ? 0 : resource.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceModel other = (ResourceModel) obj;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (resource == null) {
            if (other.resource != null)
                return false;
        } else if (!resource.equals(other.resource))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResourceModel [resource=" + resource + ", model=" + model + "]";
    }

}
