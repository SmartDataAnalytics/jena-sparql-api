package org.aksw.jena_sparql_api.mapper.examples;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;


public class ExampleMapperDBpedia {


    public static void main(String[] args) throws Exception {

        /*
         * Boiler plate code for setup
         */

        SparqlEntityManagerFactory emFactory = new SparqlEntityManagerFactory();

        emFactory.getPrefixMapping()
            .setNsPrefix("schema", "http://schema.org/")
            .setNsPrefix("dbo", "http://dbpedia.org/ontology/")
            .setNsPrefix("dbr", "http://dbpedia.org/resource/")
            .setNsPrefix("nss", "http://example.org/nss/");

        //
        emFactory.addScanPackageName(ExampleMapperDBpedia.class.getPackage().getName());

        emFactory.setSparqlService(FluentSparqlService
            .http("http://dbpedia.org/sparql", "http://dbpedia.org")
                .config().configQuery()
                    .withParser(SparqlQueryParserImpl.create())
                    .withPagination(50000)
                .end().end().create());

        EntityManager em = emFactory.getObject();

        /*
         * Query 1: Companies founded after 1955 with more than 36000 locations
         */

        List<Company> matches = JpaUtils.getResultList(em, Company.class, (cb, cq) -> {
            Root<Company> r = cq.from(Company.class);
            cq.select(r)
                    .where(cb.greaterThanOrEqualTo(r.get("foundingYear"), 1955))
                    .where(cb.greaterThanOrEqualTo(r.get("numberOfLocations"), 36000))
                    ;
        });

        for(Company c : matches) {
            System.out.println("Matched: " + c);
        }

        /*
         * Extra: Put the entities into an in-memory backed SPARQL endpoint for modification
         */
        Model model = ModelFactory.createDefaultModel();
        emFactory.setSparqlService(FluentSparqlService.from(model).create());
        EntityManager emLocal = emFactory.getObject();

        matches.forEach(emLocal::persist);
        matches.forEach(m -> m.setNumberOfLocations(1000));
        matches.forEach(emLocal::merge);

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);



        /*
         * Query 2: Avg number of locations of all companies
         */

        Double avg = JpaUtils.getSingleResult(em, Double.class, (cb, cq) -> {
            Root<Company> r2 = cq.from(Company.class);
            cq.select(cb.avg(r2.get("numberOfLocations")));
        }).doubleValue();

        System.out.println("Average number of locations: " + avg);
    }
}
