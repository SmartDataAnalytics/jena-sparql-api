package org.aksw.jena_sparql_api.utils;

import java.util.List;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.Quad;

public class DatasetDescriptionUtils {
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
