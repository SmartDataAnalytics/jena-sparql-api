package org.aksw.jena_sparql_api.beans.json;

import java.util.Map;

public class BeanParserJson {
    /**
     * Special properties for global configuration
     *
     * beans: {
     *   $prefixes: {
     *     rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
     *   }
     * }
     */
    private Map<String, Void> specialProperties;


    /**
     * Properties that indicate the creation of an object
     *
     * beans: {
     *   myBean: { $myCtor: { key: val} }
     * }
     *
     */
    private Map<String, Void> propertyCtor;
}
