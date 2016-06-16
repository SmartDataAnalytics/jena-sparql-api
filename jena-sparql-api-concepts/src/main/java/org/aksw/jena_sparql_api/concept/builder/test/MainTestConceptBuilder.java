package org.aksw.jena_sparql_api.concept.builder.test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concept.builder.api.ConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConcept;
import org.aksw.jena_sparql_api.concept.builder.api.ConceptExprConceptBuilder;
import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptBuilderFluent;
import org.aksw.jena_sparql_api.concept.builder.impl.ConceptExprVisitorSparql;
import org.aksw.jena_sparql_api.concept.builder.impl.NodeBuilderFluent;
import org.aksw.jena_sparql_api.concept.builder.utils.Exprs;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationOps;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDFS;

public class MainTestConceptBuilder {
    public static void main(String[] args) {

        PrefixMapping2 pm = new PrefixMapping2(PrefixMapping.Extended);
        pm.setNsPrefix("o", "http://fp7-pp.publicdata.eu/ontology/");
        pm.setNsPrefix("foaf", FOAF.NS);
        Prologue prologue = new Prologue(pm);

        Function<String, Element> elementParser = SparqlElementParserImpl.create(Syntax.syntaxARQ, prologue);


        Map<Node, Relation> virtualPredicates = new HashMap<>();
        Node mypred = NodeFactory.createURI("http://transitive");
        virtualPredicates.put(mypred, Relation.create("?s foaf:knows+ ?o", "s", "o", elementParser));


        //pm.getLocalPrefixMapping().



        Node allInTheSameCountry = NodeFactory.createURI("http://sameCountry");
        virtualPredicates.put(mypred, RelationOps.forAllHavingTheSameValue(
                RelationOps.from(PathParser.parse("o:partner/o:address/o:country", pm)), null));

        virtualPredicates.forEach((k, v) -> System.out.println(v.getSourceConcept().asQuery()));

        ConceptBuilder cb = ConceptBuilderFluent
             .from(ConceptBuilderFluent.union()
                     .addMember(new ConceptExprConcept(Concept.create("?s a o:Project", "s", pm))))
             //.unionMode() // whether multiple restrictions are interpreted as dis - or conjunctive - if disjunctive, the base concept is conjunctive which each restriction
            //.newRestriction().on(RDFS.label).as("x").forAll()
            .getRoot();


        ConceptExprConceptBuilder ce = new ConceptExprConceptBuilder(cb);
        ConceptExprVisitorSparql visitor = new ConceptExprVisitorSparql();
        Concept c = ce.accept(visitor);
        System.out.println(c);


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
