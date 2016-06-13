package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;

public class ConceptBuilderFluent {
    public static ConceptBuilder start() {
        return new ConceptBuilderImpl();
    }

    public static ConceptBuilder from(ConceptBuilder base) {
        return new ConceptBuilderImpl();
    }

    public static ConceptBuilderUnion union() {
        return new ConceptBuilderUnion();
    }


    public static ConceptBuilderAnd and() {
        return new ConceptBuilderAnd();
    }

//    public static ConceptBuilder not(ConceptBuilder base) {
//
//    }

}
