package org.aksw.jena_sparql_api.mapper;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.mapper.examples.Company;
import org.aksw.jena_sparql_api.mapper.examples.ExampleMapperDBpedia;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainSparqlToJson {

	public static void exampleUsingMapping() throws Exception {
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
		
        List<Company> matches = JpaUtils.getResultList(em, Company.class, (cb, cq) -> {
            Root<Company> r = cq.from(Company.class);
            cq.select(r)
                    .where(cb.greaterThanOrEqualTo(r.get("foundingYear"), 1955))
                    .where(cb.greaterThanOrEqualTo(r.get("numberOfLocations"), 36000))
                    ;
        });

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        for(Company c : matches) {
        	System.out.println(gson.toJson(c));
        }
	}
	
	public static void exampleUsingAggregation() {
		
		//AggObject
		
	}
	
	public static void main(String[] args) throws Exception {
		exampleUsingMapping();
		exampleUsingAggregation();
	}
	
}
