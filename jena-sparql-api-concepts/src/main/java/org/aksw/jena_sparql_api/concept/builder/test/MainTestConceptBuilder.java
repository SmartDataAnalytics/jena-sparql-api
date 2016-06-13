package org.aksw.jena_sparql_api.concept.builder.test;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderFluent;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.vocabulary.RDFS;

public class MainTestConceptBuilder {
    public static void main(String[] args) {
        ConceptBuilder cb = ConceptBuilderFluent
             .from(
                ConceptBuilderFluent.union()
                    .addMember(null)
             )
            .newRestriction().on(RDFS.label).as("x").forAll()
            .getRoot();





        Concept concept = cb.get();
        System.out.println("CONCEPT: " + concept);
    }
}
