package org.aksw.jena_sparql_api.utils;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class DatasetDescriptionUtils {
    public static String toString(DatasetDescription datasetDescription) {
        String result
            = "[defaultGraphs = " + Joiner.on(", ").join(datasetDescription.getDefaultGraphURIs()) + "]"
            + "[namedGraphs = " + Joiner.on(", ").join(datasetDescription.getNamedGraphURIs()) + "]";

        return result;
    }
}
