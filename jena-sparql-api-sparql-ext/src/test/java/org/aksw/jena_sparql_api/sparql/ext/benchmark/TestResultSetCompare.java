package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

public class TestResultSetCompare {

	@Test
	public void testResultSetCompare() {
		Model m = ModelFactory.createDefaultModel();
		m.add(RDF.type, RDF.type, OWL.ObjectProperty);
		m.add(RDF.type, RDFS.label, "type");
		
		m.add(RDFS.label, RDF.type, OWL.DatatypeProperty);
		m.add(RDFS.label, RDFS.comment, "comment");
		m.add(RDFS.label, RDFS.seeAlso, RDFS.label);

		String str = "PREFIX sys: <http://jsa.aksw.org/fn/sys/>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
				+ "SELECT ?cmp {\n"
				+ "  'SELECT DISTINCT ?p { ?s a <http://www.w3.org/2002/07/owl#ObjectProperty> ; ?p ?o }' sys:benchmark (?time1 ?size1 ?rs1) .\n"
				+ "  'SELECT DISTINCT ?p { ?s a <http://www.w3.org/2002/07/owl#DatatypeProperty> ; ?p ?o }' sys:benchmark (?time2 ?size2 ?rs2) .\n"
				+ "  BIND(sys:rscmp(?rs1, ?rs2) AS ?cmp)\n"
				+ "}\n";
		
		try(QueryExecution qe = QueryExecutionFactory.create(str, m)) {
			JsonObject json = (JsonObject)qe.execSelect().next().get("cmp").asLiteral().getValue();
			System.out.println("ResultSet comparison: " + json);
			Assert.assertEquals(0.33333333, json.get("precision").getAsDouble(), 0.00001f);
			Assert.assertEquals(0.5, json.get("recall").getAsDouble(), 0.00001f);
			Assert.assertEquals(0.4, json.get("fmeasure").getAsDouble(), 0.00001f);
			//System.out.println("ResultSet comparison: " + ResultSetFormatter.asText(qe.execSelect()));
		}
	}
}
