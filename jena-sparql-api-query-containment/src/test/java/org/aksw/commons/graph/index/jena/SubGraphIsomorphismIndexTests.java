package org.aksw.commons.graph.index.jena;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndexWrapper;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.commons.jena.jgrapht.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.query_containment.core.SparqlQueryContainmentUtils;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.jgrapht.graph.DefaultGraphType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;


//@Ignore
public class SubGraphIsomorphismIndexTests {
	
	private static final Logger logger = LoggerFactory.getLogger(SubGraphIsomorphismIndexTests.class);

	
    protected static EntityManager em;

    public static String clearProjection(String rawQueryStr) {
        Query rawQuery = SparqlQueryContainmentUtils.queryParser.apply(rawQueryStr);
        Op rawOp = Algebra.compile(rawQuery);
        Op op = QueryToGraph.normalizeOpReplaceConstants(rawOp);
        Query query = OpAsQuery.asQuery(op);
        query.setQueryResultStar(true);

        String result = "" + query;
        return result;
    }
    
    @BeforeClass
    public static void setup() throws Exception {
        Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
        SparqlService ss = FluentSparqlService.from(model).create();

        em = SparqlEntityManagerFactory.newInstance()
                .setSparqlService(ss)
                .addScanPackageName(SubGraphIsomorphismIndexTests.class.getPackage().getName())
                .getObject();
    }

    public SubgraphIsomorphismIndex<String, String, Node> buildIndex(Collection<String> queryIds) {

    	logger.warn("Claiming a simple directed graph - although it may be a pseudo graph");
    	
        SubgraphIsomorphismIndex<String, Graph, Node> base =
                SubgraphIsomorphismIndexWrapper.wrap(
                        SubgraphIsomorphismIndexJena.create(),
                        jenaGraph -> new PseudoGraphJenaGraph(jenaGraph, DefaultGraphType.directedSimple()));

        SubgraphIsomorphismIndex<String, String, Node> result =
                SubgraphIsomorphismIndexWrapper.wrap(base, QueryToGraph::queryToGraph);


        for(String queryId : queryIds) {
            LsqQuery lsqQuery = em.find(LsqQuery.class, queryId);

            // Transform the query so that no constants remain in basic graph patterns
            // Example: ?s a ?o -> ?s ?p ?o . FILTER(?p = rdf:type)
            
            
            String rawQueryStr = lsqQuery.getText();
            String queryStr = clearProjection(rawQueryStr);
            result.put(queryId, queryStr);
        }

        base.printTree();

        return result;
    }

    public void lookup(SubgraphIsomorphismIndex<String, String, Node> index, Collection<String> queryIds) {

        System.out.println("Lookup results:");
        for(String queryId : queryIds) {
            LsqQuery lsqQuery = em.find(LsqQuery.class, queryId);

            String rawQueryStr = lsqQuery.getText();
            String queryStr = clearProjection(rawQueryStr);
            System.out.println("Lookup result for " + queryId + ": " + queryStr);

            
            Multimap<String, BiMap<Node, Node>> r = index.lookup(queryStr, false);

            r.asMap().forEach((k, isos) -> {
                isos.forEach(iso -> {
                    System.out.println(k + ": " + iso);
                });
            });
        }
        System.out.println("End of lookup results");
    }

    /**
     *  Test case: Keys with equivalent under isomorphism entities
     */
    @Test
    public void keysWithEquivalentUnderIsomorphismGraphs() {
        buildIndex(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q1a"
        ));

        //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/q2y"));
        //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/r1a"));
        //queries.add(em.find(LsqQuery.class, "http://lsq.aksw.org/res/spo"));
    }


    /**
     * q2y can be reached with different isomorphisms via q1x and r1a
     *
     */
    @Test
    public void multiSubsumption() {
        buildIndex(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q2y",
                "http://lsq.aksw.org/res/r1a",
                "http://lsq.aksw.org/res/spo"
        ));
    }

    /**
     * Insert the entry that becomes the root node last, which requires rebuilding the whole index
     */
    @Test
    public void lateRootNode() {
        buildIndex(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q2y",
                "http://lsq.aksw.org/res/spo"
        ));
    }

    @Test
    public void lookup1() {
        SubgraphIsomorphismIndex<String, String, Node> index = buildIndex(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q2y",
                "http://lsq.aksw.org/res/spo"
        ));


        lookup(index, Arrays.asList(
                "http://lsq.aksw.org/res/q2y"
        ));
    }
}
