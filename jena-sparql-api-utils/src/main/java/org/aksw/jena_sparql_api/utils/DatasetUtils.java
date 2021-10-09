package org.aksw.jena_sparql_api.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.ResourceInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.ResourceInDatasetImpl;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;


public class DatasetUtils {

    /**
     * Returns an iterable over the models in the dataset - with the default graph being the first element
     *
     * @param dataset
     * @return
     */
    public static Iterable<Entry<String, Model>> listModels(Dataset dataset) {
        return () ->
                Stream.concat(
                        Stream.of(Quad.defaultGraphIRI.getURI()),
                        Streams.stream(dataset.listNames())
                ).map(graphName -> {
                        Model m = getDefaultOrNamedModel(dataset, graphName);
                        return (Entry<String, Model>)new SimpleEntry<>(graphName, m);
                })
                .filter(e -> e.getValue() != null)
                .iterator();
    }


    /**
     * The returned extended iterator does not yet support removal, as
     * there is no flatMap method.
     * The API is just there to be potentially future proof.
     *
     * @param dataset
     * @param p
     * @return
     */
    public static ExtendedIterator<ResourceInDataset> listResourcesWithProperty(Dataset dataset, Property p) {
        Iterator<String> it = Iterators.concat(
                Collections.singleton(Quad.defaultGraphIRI.getURI()).iterator(),
                dataset.listNames());

        Collection<String> graphNames = Lists.newArrayList(it);

        List<ResourceInDataset> list = graphNames.stream().flatMap(graphName -> {
            Model model = getDefaultOrNamedModel(dataset, graphName);
            List<ResourceInDataset> rs = model
                    .listResourcesWithProperty(p)
                    .<ResourceInDataset>mapWith(r ->
                        new ResourceInDatasetImpl(dataset, graphName, r.asNode()))
                    .toList();

            return rs.stream();
        }).collect(Collectors.toList());

        return WrappedIterator.create(list.iterator());
    }

    /**
     * Create a dataset from an IRI resource by placing its associated model
     * into a named model with that resource's IRI.
     *
     * @param resource The resource. Must be an IRI.
     * @return The dataset
     */
    public static Dataset createFromResource(Resource resource) {
        Dataset result = DatasetFactory.create();
        result.addNamedModel(resource.getURI(), resource.getModel());
        return result;
    }

//	public static Dataset createFromResourceInDefaultGraph(Resource resource) {
//		Dataset result = DatasetFactory.create();
//		result.getDefaultModel().add(resource.getURI(), resource.getModel());
//		return result;
//	}

    /**
     * Helper method that retrieves the default model if
     * Quad.isDefaultGraph's yields true for the given graphName
     *
     * @param dataset
     * @param graphName
     * @return
     */
    public static Model getDefaultOrNamedModel(Dataset dataset, Node graphNameNode) {
        String graphName = graphNameNode.getURI();
        Model result = getDefaultOrNamedModel(dataset, graphName);

        return result;
    }

    public static Model getDefaultOrNamedModel(Dataset dataset, String graphName) {
        Node g = NodeFactory.createURI(graphName);
        boolean isDefaultGraph = Quad.isDefaultGraph(g);

        Model result = isDefaultGraph
            ? dataset.getDefaultModel()
            : dataset.getNamedModel(graphName);

        return result;
    }

    public static boolean containsDefaultOrNamedModel(Dataset dataset, Node graphNameNode) {
        boolean result = Optional.ofNullable(getDefaultOrNamedModel(dataset, graphNameNode))
                .map(model -> !model.isEmpty())
                .orElse(false);

        return result;
    }

    public static boolean containsDefaultOrNamedModel(Dataset dataset, String graphName) {
        boolean result = Optional.ofNullable(getDefaultOrNamedModel(dataset, graphName))
                .map(model -> !model.isEmpty())
                .orElse(false);

        return result;
    }
}
