package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.LiteralInDatasetImpl;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.ResourceInDatasetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An RDFNode linked to a Dataset in addition to its model.
 *
 * @author raven
 *
 */
public interface RDFNodeInDataset
    extends RDFNode
{
    /**
     * The named graph such that
     * r.getDataset().getNamedGraph(r.getGraphName()).equals(r.getModel())
     * While the model and possibly graph instances may not be reference equal due to being different views,
     * they should still operate on the same underlying collection of triples.
     *
     * @return
     */
    String getGraphName();
    Dataset getDataset();

    RDFNodeInDataset inDataset(Dataset other);

    /**
     * Pass itself to a consumer and return this.
     * Useful for retaining the type when adding properties to a ResourceInDataset:
     *
     * flowOrStream.map(ResourceInDatasetImpl.createX().acceptResource(r -> r.addProperty(foo, bar));
     *
     *
     * @return
     */
    default RDFNodeInDataset mutateRDFNode(Consumer<? super RDFNode> resourceMutator) {
        resourceMutator.accept(this);
        return this;
    }

    /**
     * Create a new ResourceInDataset in the same graph/model as this resource
     *
     * @return
     */
//    default ResourceInDataset createResourceHere(Function<? super Model, ? extends Resource> fn) {
//        Dataset ds = getDataset();
//        String gn = getGraphName();
//        Model m = getModel();
//        Resource r = fn.apply(m);
//        Node n = r.asNode();
//
//        return new ResourceInDatasetImpl(ds, gn, n);
//    }

    default RDFNodeInDataset asRDFNodeInDatasetHere(Node node) {
        Dataset ds = getDataset();
        String gn = getGraphName();

        return create(ds, gn, node);
    }

    static RDFNodeInDataset create(Dataset dataset, String graphName, Node node) {
        RDFNodeInDataset result;
        if (node.isBlank() || node.isURI()) {
            result = new ResourceInDatasetImpl(dataset, graphName, node);
        } else if (node.isLiteral()) {
            result = new LiteralInDatasetImpl(dataset, graphName, node);
        } else {
            throw new IllegalArgumentException("Unsupported node type: " + node);
        }

        return result;
    }
}
