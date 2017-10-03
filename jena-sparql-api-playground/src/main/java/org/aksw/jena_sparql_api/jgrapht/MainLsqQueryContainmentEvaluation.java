package org.aksw.jena_sparql_api.jgrapht;

import java.util.List;

import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.jpa.core.RdfEntityManager;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpEquality;
import org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLsqQueryContainmentEvaluation {

	private static final Logger logger = LoggerFactory.getLogger(MainLsqQueryContainmentEvaluation.class);

	
	public static void main(String[] args) throws Exception {
        NodeMapperOpEquality nodeMapper = new NodeMapperOpEquality();
        QueryContainmentIndexImpl<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = QueryContainmentIndexImpl.create(nodeMapper);

        SparqlEntityManagerFactory emf = new SparqlEntityManagerFactory();
        Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
        SparqlService ss = FluentSparqlService.from(model).create();
        //SparqlService ss = FluentSparqlService.http("http://localhost:8950/sparql").create();

        emf.setSparqlService(ss);
        emf.addScanPackageName(MainSparqlQueryToGraph.class.getPackage().getName());
        RdfEntityManager em = emf.getObject();

        List<LsqQuery> queries;

        queries = JpaUtils.createTypedQuery(em, LsqQuery.class, (cb, cq) -> {
            Root<LsqQuery> root = cq.from(LsqQuery.class);
            cq.select(root);
        }).setMaxResults(1000).getResultList();

        
        // hack; shoud be done by the framework
        queries = Lists.newArrayList(queries);
        for(LsqQuery q : queries) {
            String id = em.getIri(q);
            q.setIri(id);
        }

        for(LsqQuery lsqq : queries) {

            // TODO HACK We need to fetch the iri from the em, as the mapper currently does not support placing an entity's iri into a field
            System.out.println("Got lsq query: " + lsqq);

        	Node node = NodeFactory.createURI(lsqq.getIri());
            String queryStr = lsqq.getText();
            Query query;
            try {
                query = SparqlQueryParserImpl.create().apply(queryStr);
            } catch(Exception e) {
                logger.warn("Failed to parse: " + queryStr);
                continue;
            }
        
            Op op = Algebra.toQuadForm(Algebra.compile(query));

	        index.put(node, op);
	    }
        
        System.out.println();
        System.out.println("Result Index tree:");
        
        index.getIndex().printTree();
	}

}
