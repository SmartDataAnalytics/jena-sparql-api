package org.aksw.jena_sparql_api.utils;

import java.util.List;

import com.google.common.base.Joiner;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;

public class DatasetDescriptionUtils {

    public static void mergeInto(DatasetDescription target, DatasetDescription source) {
        target.addAllDefaultGraphURIs(source.getDefaultGraphURIs());
        target.addAllNamedGraphURIs(source.getNamedGraphURIs());
    }

    public static Node getSingleDefaultGraph(DatasetDescription datasetDescription) {
        String str = getSingleDefaultGraphUri(datasetDescription);
        Node result = NodeFactory.createURI(str);
        return result;
    }

    /**
     * If the argument is null or there is only one default graph, this graph IRI is returned; otherwise null.
     *
     * @param datasetDescription
     * @return
     */
    public static String getSingleDefaultGraphUri(DatasetDescription datasetDescription) {
        String result;

        if(datasetDescription == null) {
            result = Quad.defaultGraphIRI.getURI();
        } else {

            List<String> dgus = datasetDescription.getDefaultGraphURIs();

            result = datasetDescription != null && dgus.size() == 1
                    ? dgus.iterator().next()
                    : null
                    ;
        }

        return result;
    }

    public static DatasetDescription createDefaultGraph(Node defaultGraph) {
        DatasetDescription result = createDefaultGraph(defaultGraph.getURI());
        return result;
    }

    public static DatasetDescription createDefaultGraph(String defaultGraph) {
        DatasetDescription result = new DatasetDescription();
        result.addDefaultGraphURI(defaultGraph);
        return result;
    }

    public static String toString(DatasetDescription datasetDescription) {
        String result = datasetDescription == null
            ? null
            : "[defaultGraphs = " + Joiner.on(", ").join(datasetDescription.getDefaultGraphURIs()) + "]"
            + "[namedGraphs = " + Joiner.on(", ").join(datasetDescription.getNamedGraphURIs()) + "]";

        return result;
    }
}
