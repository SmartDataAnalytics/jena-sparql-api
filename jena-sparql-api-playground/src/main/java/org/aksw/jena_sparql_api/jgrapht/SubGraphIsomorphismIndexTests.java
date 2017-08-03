package org.aksw.jena_sparql_api.jgrapht;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndex;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndexJGraphT;
import org.aksw.jena_sparql_api.iso.index.SubGraphIsomorphismIndexWrapper;
import org.aksw.jena_sparql_api.jgrapht.wrapper.PseudoGraphJenaGraph;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.BeforeClass;
import org.junit.Test;


public class SubGraphIsomorphismIndexTests {

    protected static EntityManager em;

    @BeforeClass
    public static void setup() throws Exception {
        Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
        SparqlService ss = FluentSparqlService.from(model).create();

        em = SparqlEntityManagerFactory.newInstance()
                .setSparqlService(ss)
                .addScanPackageName(MainSparqlQueryToGraph.class.getPackage().getName())
                .getObject();
    }

    public void run(List<String> queryIds) {

        SubGraphIsomorphismIndex<String, Graph, Node> base =
                SubGraphIsomorphismIndexWrapper.wrap(
                        SubGraphIsomorphismIndexJGraphT.create(),
                        PseudoGraphJenaGraph::new);

        SubGraphIsomorphismIndex<String, String, Node> index =
                SubGraphIsomorphismIndexWrapper.wrap(base, MainSparqlQueryToGraph::queryToGraph);


        for(String queryId : queryIds) {
            LsqQuery lsqQuery = em.find(LsqQuery.class, queryId);

            String queryStr = lsqQuery.getText();
            index.put(queryId, queryStr);
        }

        base.printTree();
    }

    /**
     *  Test case: Keys with equivalent under isomorphism entities
     */
    @Test
    public void keysWithEquivalentUnderIsomorphismGraphs() {
        run(Arrays.asList(
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
        run(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q2y",
                "http://lsq.aksw.org/res/r1a",
                "http://lsq.aksw.org/res/spo"
        ));
    }

    @Test
    public void lateRootNode() {
        run(Arrays.asList(
                "http://lsq.aksw.org/res/q1x",
                "http://lsq.aksw.org/res/q2y",
                "http://lsq.aksw.org/res/spo"
        ));
    }
}
