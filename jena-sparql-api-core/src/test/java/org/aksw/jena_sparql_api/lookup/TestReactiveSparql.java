package org.aksw.jena_sparql_api.lookup;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Table;
import org.junit.Test;

import com.google.common.collect.Range;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class TestReactiveSparql {

	
	
	//@Test
	public void testSelectLookupSimple() {
		RDFConnection conn = RDFConnectionFactory.connect(RDFDataMgr.loadDataset("virtual-predicates-example.ttl"));
		
		LookupService<Node, Table> ls = new LookupServiceSparqlQuery(
				new QueryExecutionFactorySparqlQueryConnection(conn),
				QueryFactory.create("SELECT * { ?s ?p ?o }"),
				Vars.s);

		Flowable<Entry<Node, Table>> flowable = ls.apply(Arrays.asList(
			NodeFactory.createURI("http://www.example.org/Anne"),
			NodeFactory.createURI("http://www.example.org/Bob")
		));
		
		
		flowable.subscribe(item -> System.out.println("Item: " + item));
	}

	@Test(expected=RuntimeException.class)
	public void testSelectListSimple() {
		RDFConnection conn = RDFConnectionFactory.connect(RDFDataMgr.loadDataset("virtual-predicates-example.ttl"));

		DelayerDefault delayer = new DelayerDefault(5000);
		delayer.setLastRequestTime(System.currentTimeMillis());
		
		MapService<Concept, Node, Table> ms = new MapServiceSparqlQuery(
				new QueryExecutionFactoryDelay(new QueryExecutionFactorySparqlQueryConnection(conn), delayer),
				QueryFactory.create("SELECT * { ?s ?p ?o }"),
				Vars.s);

		Flowable<Entry<Node, Table>> flowable = ms.createPaginator(ConceptUtils.createSubjectConcept()).apply(Range.all());
			
		flowable
			.timeout(1, TimeUnit.SECONDS)
			.subscribeOn(Schedulers.io())
			.toList().blockingGet();
		//flowable.take(1).subscribe(item -> System.out.println("Item: " + item));
	}
}
