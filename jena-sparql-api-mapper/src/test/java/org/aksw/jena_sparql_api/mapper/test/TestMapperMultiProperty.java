package org.aksw.jena_sparql_api.mapper.test;

import java.text.ParseException;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerJena;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetDescription;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

public class TestMapperMultiProperty {

	/**
	 * Create a Country entity and populate it from a set of triples which contain multiple values for the
	 * country's population.
	 *
	 * @throws ParseException
	 */
	@Test
	public void testCherryPicking() throws ParseException {
		RDFDatatype intType = TypeMapper.getInstance().getTypeByClass(Integer.class);

		//Graph graph = GraphFactory.createDefaultGraph();
		DatasetGraph dg = DatasetGraphFactory.createMem();
		//Graph graph = dg.getDefaultGraph();
		Node aut = NodeFactory.createURI("http://ex.org/Austria");
		Node label = NodeFactory.createURI("http://ex.org/label");
		Node population = NodeFactory.createURI("http://ex.org/population");

		Node g = aut;
		dg.add(new Quad(g, aut, label, NodeFactory.createLiteral("Austria")));
		dg.add(new Quad(g, aut, population, NodeFactory.createLiteral("7", intType)));
		dg.add(new Quad(g, aut, population, NodeFactory.createLiteral("8", intType)));

//		DatasetDescription dd = DatasetDescriptionUtils.createDefaultGraph(g);

		DatasetDescription dd = new DatasetDescription();//DatasetDescriptionUtils.createDefaultGraph(g);
		dd.addNamedGraphURI(g.getURI());
		dd.addDefaultGraphURI(g.getURI());
		SparqlService sparqlService = FluentSparqlService.from(dg)
				.config()
					.configQuery()
						.withParser(SparqlQueryParserImpl.create())
					.end()
					.withDatasetDescription(dd, g.getURI())
				.end()
				.create();

		sparqlService.getQueryExecutionFactory()
			.createQueryExecution("CONSTRUCT { ?s ?p ?o } { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
			//.createQueryExecution("CONSTRUCT { ?g a ?s . ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }").execConstruct().write(System.out, "TTL");
		System.out.println("---");

		//UpdateExecutionUtils.executeInsert(sparqlService.getUpdateExecutionFactory(), graph);
		//sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
		//System.out.println(ResultSetFormatter.asText(sparqlService.getQueryExecutionFactory().createQueryExecution("SELECT ?x ?g ?s ?p ?o WHERE { SELECT  (?s AS ?x) ?g ?s ?p ?o WHERE { GRAPH ?g { ?s  ?p  ?o .} FILTER ( ?p IN (<http://ex.org/label>, <http://ex.org/population>) ) } }").execSelect()));
		//System.out.println(ResultSetFormatter.asText(sparqlService.getQueryExecutionFactory().createQueryExecution("SELECT ?s ?p ?o WHERE { ?s  ?p  ?o . FILTER ( ?p IN (<http://ex.org/label>, <http://ex.org/population>) ) }").execSelect()));
		//System.out.println(ResultSetFormatter.asText(sparqlService.getQueryExecutionFactory().createQueryExecution("SELECT ?g ?s ?p ?o WHERE { Graph ?g { ?s  ?p  ?o . FILTER ( ?p IN (<http://ex.org/label>, <http://ex.org/population>) ) } }").execSelect()));
		//System.out.println(ResultSetFormatter.asText(sparqlService.getQueryExecutionFactory().createQueryExecution("SELECT ?g ?s ?p ?o WHERE { Graph ?g { ?s  ?p  ?o } }").execSelect()));
		//System.out.println("---");

		EntityManagerJena em = new EntityManagerJena(new RdfMapperEngineImpl(sparqlService));
//		RdfType countryType = em.getRdfTypeFactory().forJavaType(Country.class);
//
//		TypedNode typedNode = new TypedNode(countryType, aut);
//		RdfPersistenceContext persistenceContext = em.getPersistenceContext();
//		Object tmp = persistenceContext.entityFor(typedNode);
//		Country entity = (Country)tmp;
//		em.getPersistenceContext().getEntityGraphMap().putAll(graph, entity);

		Country country = em.find(Country.class, aut);
		country.setPopulation(9);
		em.merge(country);

		sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
	}


	/**
	 * Test setting inial data on the persistence context rather than the sparql endpoint
	 * @throws ParseException
	 */
	//@Test
	public void testLowLevelCherryPicking() throws ParseException {
		RDFDatatype intType = TypeMapper.getInstance().getTypeByClass(Integer.class);

		Graph graph = GraphFactory.createDefaultGraph();
		Node aut = NodeFactory.createURI("http://ex.org/Austria");
		Node label = NodeFactory.createURI("http://ex.org/label");
		Node population = NodeFactory.createURI("http://ex.org/population");

		graph.add(new Triple(aut, label, NodeFactory.createLiteral("Austria")));
		graph.add(new Triple(aut, population, NodeFactory.createUncachedLiteral(7, intType)));
		graph.add(new Triple(aut, population, NodeFactory.createUncachedLiteral(8, intType)));


		SparqlService sparqlService = FluentSparqlService.forDataset().create();
//sparqlService.getDatasetDescription().
		EntityManagerJena em = new EntityManagerJena(new RdfMapperEngineImpl(sparqlService));
		RdfType countryType = em.getRdfTypeFactory().forJavaType(Country.class);

		TypedNode typedNode = new TypedNode(countryType, aut);
		RdfPersistenceContext persistenceContext = em.getPersistenceContext();
		Object tmp = persistenceContext.entityFor(typedNode);
		Country entity = (Country)tmp;
		em.getPersistenceContext().getEntityGraphMap().putAll(graph, entity);

		em.find(Country.class, aut);
		sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");

		entity.setPopulation(9);
		em.merge(entity);

		sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
	}

}
