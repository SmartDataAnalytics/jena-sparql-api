package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import java.util.function.Consumer;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;

/**
 * A Resource linked to a Dataset in addition to its model.
 *
 * @author raven
 *
 */
public interface ResourceInDataset
    extends Resource, RDFNodeInDataset
{
    @Override
    ResourceInDataset inDataset(Dataset other);

    @Override
    default ResourceInDataset asResource() {
        return this;
    }

    /**
     * Pass itself to a consumer and return this.
     * Useful for retaining the type when adding properties to a ResourceInDataset:
     *
     * flowOrStream.map(ResourceInDatasetImpl.createX().acceptResource(r -> r.addProperty(foo, bar));
     *
     *
     * @return
     */
    default ResourceInDataset mutateResource(Consumer<? super Resource> resourceMutator) {
        resourceMutator.accept(this);
        return this;
    }


    //default ResourceInDataset yieldFromModel(Function<? super Model, Resource> fn) {
}
