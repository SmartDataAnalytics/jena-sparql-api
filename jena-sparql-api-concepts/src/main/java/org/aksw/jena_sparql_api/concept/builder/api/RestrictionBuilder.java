package org.aksw.jena_sparql_api.concept.builder.api;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

public interface RestrictionBuilder {

    ConceptBuilder getParent();

    /**
     * The predicate the restriction applies to
     */
    //protected Quantor quantor;

    //ConceptBuilder ();
    RestrictionBuilder on(Node node);

    //RestrictionBuilder on(ConceptBuilder conceptBuilder);

    ConceptBuilder forAll();
    ConceptBuilder exists();
    RestrictionBuilder as(Var var);


    /**
     * Remove this restriction from the corresponding conceptBuilder
     *
     */
    void destroy();

    /*
     *  Convenience functions
     */

    default RestrictionBuilder as(String varName) {
        return as(Var.alloc(varName));
    }

    default RestrictionBuilder on(Resource resource) {
        return on(resource.asNode());
    }

//    default RestrictionBuilder on(Concept concept) {
//
//    };

}
