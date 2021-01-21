package org.aksw.jena_sparql_api.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import com.google.common.collect.Multiset;

public class TestPrefixAggregation {
	
	
	@Test
	public void testPrefixAggregation() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");
//		Model model = RDFDataMgr.loadModel("/home/raven/Downloads/SQCFrameWork-benchmarks/sqcfreame-swdf-benchmarks/Random-swdf-Sup15-benchmark.ttl");
		
		List<Binding> list = new ArrayList<>();
		Set<Var> resultVars;
		try (QueryExecution qe = QueryExecutionFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }", model)) {
			ResultSet rs = qe.execSelect();
			resultVars = VarUtils.toSet(rs.getResultVars());
			new IteratorResultSetBinding(rs).forEachRemaining(list::add);
		}
		
		Map<Var, Set<String>> usedIriPrefixes = list.stream()
			.collect(ResultSetAnalytics.usedPrefixes(7).asCollector());
		System.out.println(usedIriPrefixes);


		Map<Var, Entry<Multiset<String>, Long>> usedDatatypesAndNulls = list.stream()
				.collect(ResultSetAnalytics.usedDatatypesAndNullCounts(resultVars).asCollector());
				
		System.out.println(usedDatatypesAndNulls);

		SchemaMapperImpl.newInstance()
			.setSourceVars(resultVars)
			.setSourceVarToDatatypes(v -> usedDatatypesAndNulls.get(v).getKey().elementSet())
			.setSourceVarToNulls(v -> usedDatatypesAndNulls.get(v).getValue())
			.setTypePromotionStrategy(TypePromoterImpl.create())
			.createSchemaMapping();
		
		
	}
}
