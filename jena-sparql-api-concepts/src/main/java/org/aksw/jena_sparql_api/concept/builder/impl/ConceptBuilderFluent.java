package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprList;

public class ConceptBuilderFluent {
    public static ConceptBuilder start() {
        return new ConceptBuilderImpl();
    }

    public static ConceptBuilder from(ConceptBuilder base) {
        return new ConceptBuilderImpl();
    }

    public static ConceptExprList union() {
        return new ConceptExprListImpl();
    }


    public static ConceptExprList and() {
        return new ConceptExprListImpl();
    }

//    public static ConceptBuilder not(ConceptBuilder base) {
//
//    }

}
