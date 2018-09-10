package org.aksw.jena_sparql_api.mapper.test.cases;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.test.domain.Company;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;


public class MainExampleMapperDBpedia {

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

        // Classes which to register to the persistence unit
        emFactory.addScanPackageName(Company.class.getPackage().getName());

        Model dataModel = RDFDataMgr.loadModel("dbpedia-companies.ttl");

        emFactory.setSparqlService(FluentSparqlService
                .from(dataModel)
            //.http("http://dbpedia.org/sparql", "http://dbpedia.org")
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

        System.out.println("Example 1 =======================================================");
        System.out.println("Companies founded after 1955 with more than 36000 locations");
        for(Company c : matches) {
            System.out.println("Matched: " + c);
        }


        System.out.println("Example 2 ===========================================================");
        System.out.println("Value for McDonalds");
        Company c = em.find(Company.class, "http://dbpedia.org/resource/McDonald's");
        System.out.println("Found: " + c);

        System.out.println("Example 3 ===========================================================");

        /*
         * Extra: Put the entities into an in-memory backed SPARQL endpoint for modification
         */
        Model model = ModelFactory.createDefaultModel();
        emFactory.setSparqlService(FluentSparqlService.from(model).create());
        EntityManager emLocal = emFactory.getObject();

        //matches.forEach(emLocal::persist);
        for(Company match : matches) {
            try {
                emLocal.persist(match);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        matches.forEach(m -> m.setNumberOfLocations(1000));


        //matches.forEach(emLocal::merge);
        for(Company match : matches) {
            try {
                emLocal.persist(match);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }


        System.out.println("Model after updating properties:");
        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

        /*
         * Query 2: Avg number of locations of all companies
         */

        System.out.println("Example 4 ===========================================================");

        Double avg = JpaUtils.getSingleResult(em, Double.class, (cb, cq) -> {
            Root<Company> r2 = cq.from(Company.class);
            cq.select(cb.avg(r2.get("numberOfLocations")));
        }).doubleValue();

        System.out.println("Average number of locations: " + avg);



        /*
         * Join 1: (to be done)
         * TODO port examples from http://www.objectdb.com/java/jpa/query/jpql/from
         */
//        CriteriaQuery<Country> q = cb.createQuery(Country.class);
//        Root<Country> c = q.from(Country.class);
//        Join<Country> p = c.join("capital", JoinType.LEFT);

    }
}
