package org.aksw.jena_sparql_api.analytics;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder;
import org.aksw.jena_sparql_api.mapper.parallel.AggSet;
import org.aksw.jena_sparql_api.mapper.parallel.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;

import com.google.common.collect.Sets;

public class TestPrefixAggregation {
	
    public static ParallelAggregator<Binding, Map<Var, Set<String>>, ?> createForDatatypes() {

    	ParallelAggregator<Binding, Map<Var, Set<String>>, ?> result = 
    	AggBuilder.from(new AggSet<>(() -> (Set<String>)new LinkedHashSet<String>()))
			.withInputFilter(Objects::nonNull)
    		.withInputTransform(NodeUtils::getDatatypeIri)
    		.withInputSplit((Binding b) -> Sets.newHashSet(b.vars()), Binding::get)
    		.getAsParallelAggregator();
    	
    	return result;
    }
	
	@Test
	public void testPrefixAggregation() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");
//		Model model = RDFDataMgr.loadModel("/home/raven/Downloads/SQCFrameWork-benchmarks/sqcfreame-swdf-benchmarks/Random-swdf-Sup15-benchmark.ttl");
		
		List<Binding> list = new ArrayList<>();
		try (QueryExecution qe = QueryExecutionFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }", model)) {
			ResultSet rs = qe.execSelect();
			new IteratorResultSetBinding(rs).forEachRemaining(list::add);
		}
		
		Map<Var, Set<String>> usedIriPrefixes = list.parallelStream()
			.collect(PrefixAccumulator.createForBindings(6).asCollector());
		System.out.println(usedIriPrefixes);

		Map<Var, Set<String>> usedDatatypeIris = list.parallelStream()
				.collect(createForDatatypes().asCollector());
			System.out.println(usedDatatypeIris);
	}
}
