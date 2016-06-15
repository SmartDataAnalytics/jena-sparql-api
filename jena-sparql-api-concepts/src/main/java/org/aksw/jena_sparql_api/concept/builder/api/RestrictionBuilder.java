package org.aksw.jena_sparql_api.concept.builder.api;

import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

public interface RestrictionBuilder
    extends Supplier<RestrictionExpr>
{

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
    ConceptBuilder destroy();

    /*
     *  Convenience functions
     */

    default RestrictionBuilder as(String varName) {
        return as(Var.alloc(varName));
    }

    default RestrictionBuilder on(Resource resource) {
        return on(resource.asNode());
    }
    
    /**
     * Build a new range axiom from the current state of the builder.
     * 
     * This should not be called as part of the construction process, but by
     * visitors that e.g. construct the SPARQL concept / query
     * 
     * @return
     */
    RestrictionExprQuantor build();

    
//    static <T> T accept(RestrictionBuilderVisitor)
//    default RestrictionBuilder on(Concept concept) {
//
//    };

}
