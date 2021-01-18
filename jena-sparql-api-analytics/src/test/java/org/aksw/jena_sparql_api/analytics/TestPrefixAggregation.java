package org.aksw.jena_sparql_api.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

public class TestPrefixAggregation {
	@Test
	public void testPrefixAggregation() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");

		List<Binding> list = new ArrayList<>();
		try (QueryExecution qe = QueryExecutionFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }", model)) {
			ResultSet rs = qe.execSelect();
			new IteratorResultSetBinding(rs).forEachRemaining(list::add);
		}
		
		Map<Var, Set<String>> x = list.parallelStream()
			.collect(PrefixAccumulator.create(6).asCollector());

		System.out.println(x);
	}
}
