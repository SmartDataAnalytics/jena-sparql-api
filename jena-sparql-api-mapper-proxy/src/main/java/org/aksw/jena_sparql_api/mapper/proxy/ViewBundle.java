package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ViewBundle {
    /**
     * The raw yield yields the set of RDFNodes that back the java view
     * (predicate, isFwd) -> (subject -> rdfObjects)
     *
     */
    public BiFunction<Property, Boolean, Function<Resource, Collection<RDFNode>>> rawView;

    /**
     * The function that yields the appropriate java type
     * (predicate, isFwd) -> (subject -> javaObject)
     *
     * @return
     */
    public BiFunction<Property, Boolean, Function<Resource, Object>> javaView;


    public ViewBundle(BiFunction<Property, Boolean, Function<Resource, Collection<RDFNode>>> rawView,
            BiFunction<Property, Boolean, Function<Resource, Object>> javaView) {
        super();
        this.rawView = rawView;
        this.javaView = javaView;
    }
}
