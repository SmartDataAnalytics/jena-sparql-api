package org.aksw.jena_sparql_api.query_containment.core;

import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.query_containment.index.ResidualMatching;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.query_containment.index.SparqlTreeMapping;
import org.apache.derby.tools.sysinfo;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.junit.Test;

public class TestSparqlQueryContainmentSimple {

    @Test
    public void test() {
        printOutQueryContainments("SELECT * { ?a ?b ?c }", "SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) }");
    }

    /**
     * Adapted example from http://sparql-qc-bench.inrialpes.fr/UCQProj.html#p3
     * in qStr the variables were renamed
     */
    @Test
    public void testInrialpesQcUcqProj3() {

        String vStr = "PREFIX : <> \n" +
                "\n" +
                "SELECT ?x ?y ?z WHERE {\n" +
                "	?x a :Student .\n" +
                "	?x :registeredAt ?y .\n" +
                "	?x :placeOfBirth ?z .\n" +
                "	?y a :University .\n" +
                "	?y :locatedAt ?z .	\n" +
                "	?z a :City .\n" +
                "}";

        String qStr = "PREFIX : <> \n" +
                "\n" +
                "SELECT ?a ?b ?c WHERE {\n" +
                "	?a a :Student .\n" +
                "	?a :registeredAt ?b .\n" +
                "	?b a :University .\n" +
                "	?a :placeOfBirth ?c .\n" +
                "	?c a :City .\n" +
                "	?b :locatedAt ?c .	\n" +
                "}";

        printOutQueryContainments(vStr, qStr);
    }

    public static void printOutQueryContainments(String vStr, String qStr) {
        System.out.println("Lookup with " + qStr);
        Query v = QueryFactory.create(vStr, Syntax.syntaxSPARQL_10);
        Query q = QueryFactory.create(qStr, Syntax.syntaxSPARQL_10);

        Op vOp = Algebra.compile(v);
        Op qOp = Algebra.compile(q);

        // Insert the query in the '(v)iew' role into the index and perform a
        // lookup with the one in the '(q)uery/request/prototype' role.
        SparqlQueryContainmentIndex<String, ResidualMatching> index = SparqlQueryContainmentIndexImpl.create();
        index.put("v", vOp);

        Stream<Entry<String, SparqlTreeMapping<ResidualMatching>>> candidates = index.match(qOp);

        System.out.println("Start of containment mappings");
        candidates.forEach(e -> {
            String key = e.getKey();
            SparqlTreeMapping<ResidualMatching> mapping = e.getValue();

            System.out.println("Obtained the following mappings for index entry with key " + key + ": ");
            System.out.println(mapping);

            System.out.println("Normalized index entry algebra expression:");
            System.out.println(index.get(key));
            ResidualMatching rm = mapping.getNodeMappings().get(
                    mapping.getaTree().getRoot(), mapping.getbTree());
            System.out.println(rm);
        });
        System.out.println("End of containment mappings");
    }


    @Test
    public void testIssue35() {
        String q1 = ""
            + "SELECT  ?instancia ?instanciaLabel ?i0 ?predicate_i0 ?i0Label\n"
            + "FROM <http://dbpedia.org>\n"
            + "WHERE\n"
            + "  { ?instancia  a                   <http://dbpedia.org/ontology/Musical> ;\n"
            + "              ?predicate_i0         ?i0\n"
            + "    FILTER ( ?predicate_i0 = <http://dbpedia.org/ontology/musicBy> )\n"
            + "    \n"
            + "      { ?instancia  <http://www.w3.org/2000/01/rdf-schema#label>  ?instanciaLabel\n"
            + "        FILTER ( lang(?instanciaLabel) = \"en\" )\n"
            + "      }\n"
            + "    \n"
            + "      { ?i0  <http://www.w3.org/2000/01/rdf-schema#label>  ?i0Label\n"
            + "        FILTER ( lang(?i0Label) = \"en\" )\n"
            + "      }\n"
            + "  }\n";

        String q2 = ""
            + "SELECT  ?i0 ?predicate_i0 ?i0Label ?instancia ?instanciaLabel\n"
            + "FROM <http://dbpedia.org>\n"
            + "WHERE\n"
            + "  { ?instancia  a                   <http://dbpedia.org/ontology/Musical> ;\n"
            + "              ?predicate_i0         ?i0\n"
            + "    FILTER ( ?predicate_i0 = <http://dbpedia.org/ontology/musicBy> )\n"
            + "    \n"
            + "      { ?i0  <http://www.w3.org/2000/01/rdf-schema#label>  ?i0Label\n"
            + "        FILTER ( lang(?i0Label) = \"en\" )\n"
            + "      }\n"
            + "    \n"
            + "      { ?instancia  <http://www.w3.org/2000/01/rdf-schema#label>  ?instanciaLabel\n"
            + "        FILTER ( lang(?instanciaLabel) = \"en\" )\n"
            + "      }\n"
            + "  }\n";

        printOutQueryContainments(q1, q2);
    }

}



