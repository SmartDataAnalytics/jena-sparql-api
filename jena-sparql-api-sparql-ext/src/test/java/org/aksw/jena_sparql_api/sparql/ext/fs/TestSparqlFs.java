package org.aksw.jena_sparql_api.sparql.ext.fs;

import org.aksw.jena_sparql_api.sparql.ext.url.JenaExtensionUrl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.junit.Test;

public class TestSparqlFs {
	String queryStr1 = "SELECT * \n" + 
			"FROM <file:///home/raven/Projects/Eclipse/qrowd-rdf-data-integration/datasets/1014-electric-bikesharing-stations/dcat.ttl> {\n" + 
			"  ?s ?p ?o\n" + 
			"}\n";

	String queryStr2 = "PREFIX nio: <" + JenaExtensionFs.ns + ">\n"
			+ "PREFIX url: <" + JenaExtensionUrl.ns + ">\n"
			+ "SELECT * { <> nio:find ?o . FILTER(REGEX(STR(?o), 'xml$', 'i')) ?o url:text ?c } LIMIT 1";

	String queryStr3 = "SELECT * {\n" + 
			"VALUES (?s) { (<http://qrowd-project.eu/resource/electric-bikesharing-stations>) }\n" +
			"SERVICE <file:///home/raven/Projects/Eclipse/qrowd-rdf-data-integration/datasets/1014-electric-bikesharing-stations/dcat.ttl> {\n" + 
			"  ?s ?p ?o\n" + 
			"} }";

	String queryStr4 = "SELECT * {\n" + 
			"VALUES (?x) { ( <file:///home/raven/Projects/Eclipse/qrowd-rdf-data-integration/datasets/1014-electric-bikesharing-stations/dcat.ttl> ) }\n" +
			"VALUES (?s) { ( <http://qrowd-project.eu/resource/electric-bikesharing-stations>) }\n" +
			"SERVICE ?x {\n" + 
			"  ?s ?p ?o\n" + 
			"} }";

	String queryStr5 = "SELECT * {\n" + 
			"VALUES (?x) { ( <file:///home/raven/Projects/Eclipse/qrowd-rdf-data-integration/datasets/1014-electric-bikesharing-stations/dcat.ttl>) (<http://dbpedia.org/sparql>) }\n" +
			"VALUES (?s) { ( <http://qrowd-project.eu/resource/electric-bikesharing-stations>) (<http://dbpedia.org/resource/Leipzig>) }\n" +
			"SERVICE ?x {\n" + 
			"  ?s ?p ?o\n" + 
			"} }";

	
	String queryStr6 = "PREFIX fs: <" + JenaExtensionFs.ns + ">\n" +
			"SELECT * {\n" + 
			"  <file:///home/raven/Projects/Eclipse/qrowd-rdf-data-integration/datasets/1014-electric-bikesharing-stations/> fs:find ?file\n" + 
			"  BIND(fs:isRdf(?file) AS ?isRdf)\n" + 
			"  BIND(fs:rdfLang(?file) AS ?lang)\n" + 
			"}";

	String queryStr7 = "PREFIX fs: <" + JenaExtensionFs.ns + ">\n" +
			"PREFIX url: <http://jsa.aksw.org/fn/url/>\n" +
			"SELECT * {\n" + 
			"  <file:///home/raven/Projects/limbo/git/claus-playground-dataset/04-dcat-sparql> fs:parents ?file .\n" + 
			" FILTER(url:text(IRI(CONCAT(STR(?file), '.git')))) \n" +
			"}";

	String queryStr = queryStr2;

	
	@Test
	public void testSparqlFs() {

		Dataset dataset = DatasetFactory.wrap(ModelFactory.createDefaultModel());//DatasetFactory.create();
		try(RDFConnection conn = RDFConnectionFactory.connect(dataset)) {
	
			
			//try(QueryExecution qe = QueryExecutionFactory.create(queryStr, (Dataset)null)) {
			try(QueryExecution qe = conn.query(queryStr)) {
				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
		}
	}
	
	//@Test
	public void testSparqlLang() {

		Dataset dataset = DatasetFactory.wrap(ModelFactory.createDefaultModel());//DatasetFactory.create();
		try(RDFConnection conn = RDFConnectionFactory.connect(dataset)) {
			//try(QueryExecution qe = QueryExecutionFactory.create(queryStr, (Dataset)null)) {
			try(QueryExecution qe = conn.query(queryStr7)) {
				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
			}
		}
	}


//	@Test
//	public void testSparqlFs2() {
//
//		try(QueryExecution qe = QueryExecutionFactory.create(queryStr, (Dataset)null)) {
//			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//		}
//	}
}
