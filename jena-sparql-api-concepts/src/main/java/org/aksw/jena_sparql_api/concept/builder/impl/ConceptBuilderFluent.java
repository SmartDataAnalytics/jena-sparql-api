package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExpr;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprList;

public class ConceptBuilderFluent {
    public static ConceptBuilder start() {
        return new ConceptBuilderImpl();
    }

    public static ConceptBuilder from(ConceptExpr conceptExpr) {
        ConceptBuilder result = new ConceptBuilderImpl();
        result.setBaseConceptExpr(conceptExpr);
        return result;
    }

    public static ConceptBuilder from(ConceptBuilder conceptBuilder) {
        return from(new ConceptExprConceptBuilder(conceptBuilder));
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
