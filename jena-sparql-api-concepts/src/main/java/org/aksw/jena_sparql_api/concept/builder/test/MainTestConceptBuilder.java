package org.aksw.jena_sparql_api.concept.builder.test;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderFluent;
import org.aksw.jena_sparql_api.concept.builder.impl.NodeBuilderFluent;
import org.aksw.jena_sparql_api.concept.builder.utils.Exprs;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDFS;

public class MainTestConceptBuilder {
    public static void main(String[] args) {

        Map<Node, Relation> virtualPredicates = new HashMap<>();
        Node mypred = NodeFactory.createURI("http://transitive");
        virtualPredicates.put(mypred, Relation.create("?s foaf:knows+ ?o", "s", "o"));


        ConceptBuilder cb = ConceptBuilderFluent
             .from(ConceptBuilderFluent.union()
                     .addMember(null))
             //.unionMode() // whether multiple restrictions are interpreted as dis - or conjunctive - if disjunctive, the base concept is conjunctive which each restriction
            .newRestriction().on(RDFS.label).as("x").forAll()
            .getRoot();

        //cb.isUnion();
        //cb.isIntersection();
        //cb.asUnion();

        /**
         * Project those relations for which there exists rdfs:label predicates
         * I suppose this would work by stating that some in/out predicate is desired, which is assigned an alias,
         * and the alias is mapped to a concept builder
         *
         * So this means, that the projection is closely related to the concept builder, as the set of predicates
         * which to project can be expressed as a concept.
         *
         * Hence: C, D := C AND D, C OR D, NOT C, exists r.C, forAll r.C
         * becomes:
         * C, D := C AND D, C OR D, NOT C, exists R.C, forAll R.C
         * with
         * R := r, C
         * so a role can be either a primitive role, or a set of roles specified by a concept.
         * so essentially we can have role expressions analogous to class expressions.
         *
         *
         */

        NodeBuilder nb = NodeBuilderFluent.start()
            .out(RDFS.label).setOptional(true).getTarget().addExpr(Exprs.langMatches("en"))
            .out(mypred).setOptional(true).getSource()
            .getRoot();




//            .beginOut()
//             .end().star()




//        Concept concept = cb.get();
//        System.out.println("CONCEPT: " + concept);
    }
}
