package org.aksw.jena_sparql_api.mapper.test.cases;

import java.text.ParseException;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.test.domain.Country;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatasetDescription;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformLimit;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Iterators;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.graph.GraphFactory;

public class TestMapperMultiProperty {

    /**
     * Create a Country entity and populate it from a set of triples which contain multiple values for the
     * country's population.
     *
     * @throws ParseException
     */
    //@Test // TODO Fix and re-enable this test
    public void testCherryPicking() throws Exception {
        Dataset ds = DatasetFactory.createGeneral();
        RDFDataMgr.read(ds, new ClassPathResource("test-country.nq").getInputStream(), Lang.NQUADS);

        String graphName = ds.listNames().next();
        Node s = ds.getNamedModel(graphName).listStatements().toSet().iterator().next().asTriple().getSubject();

        DatasetDescription dd = DatasetDescriptionUtils.createDefaultGraph(graphName);
        SparqlService sparqlService = FluentSparqlService.from(ds)
                .config()
                    .configQuery()
                        .withParser(SparqlQueryParserImpl.create())
                    .end()
                    .withDatasetDescription(dd, graphName)
                    .configQuery()
                        .withQueryTransform(F_QueryTransformDatasetDescription.fn)
                        .withQueryTransform(F_QueryTransformLimit.create(1000))
                    .end()
                .end()
                .create();


        RDFDatatype intType = TypeMapper.getInstance().getTypeByClass(Integer.class);

        System.out.println("names: " + Iterators.toString(ds.listNames()));



        System.out.println(ResultSetFormatter.asText(sparqlService.getQueryExecutionFactory()
                .createQueryExecution("SELECT * { ?s ?p ?o }").execSelect()));
        System.out.println("---");


        EntityManagerImpl em = new EntityManagerImpl(new RdfMapperEngineImpl(sparqlService));
//		RdfType countryType = em.getRdfTypeFactory().forJavaType(Country.class);
//
//		TypedNode typedNode = new TypedNode(countryType, aut);
//		RdfPersistenceContext persistenceContext = em.getPersistenceContext();
//		Object tmp = persistenceContext.entityFor(typedNode);
//		Country entity = (Country)tmp;
//		em.getPersistenceContext().getEntityGraphMap().putAll(graph, entity);

        Country country = em.find(Country.class, s);
        System.out.println("Found: " + country);
        country.setPopulation(9);
        em.merge(country);

        System.out.println("New state:");
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
        graph.add(new Triple(aut, population, NodeFactory.createLiteralByValue(7, intType)));
        graph.add(new Triple(aut, population, NodeFactory.createLiteralByValue(8, intType)));


        SparqlService sparqlService = FluentSparqlService.forDataset().create();
//sparqlService.getDatasetDescription().
        EntityManagerImpl em = new EntityManagerImpl(new RdfMapperEngineImpl(sparqlService));
        RdfType countryType = em.getRdfTypeFactory().forJavaType(Country.class);

// TODO Fix the code below
//        TypedNode typedNode = new TypedNode(countryType, aut);
//        RdfPersistenceContext persistenceContext = em.getPersistenceContext();
//        Object tmp = persistenceContext.entityFor(typedNode);
//        Country entity = (Country)tmp;
//        em.getPersistenceContext().getEntityGraphMap().putAll(graph, entity);
//
//        em.find(Country.class, aut);
//        sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
//
//        entity.setPopulation(9);
//        em.merge(entity);
//
//        sparqlService.getQueryExecutionFactory().createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct().write(System.out, "TTL");
    }

}
