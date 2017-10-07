package org.aksw.jena_sparql_api.jgrapht;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.criteria.Root;

import org.aksw.commons.collections.tagmap.ValidationUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.SubgraphIsomorphismIndexJena;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.jpa.core.RdfEntityManager;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpEquality;
import org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.QueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.jgrapht.DirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MainLsqQueryContainmentEvaluation {

	private static final Logger logger = LoggerFactory.getLogger(MainLsqQueryContainmentEvaluation.class);

	
	public static void main(String[] args) throws Exception {
//        BiMap<String, String> mapA = HashBiMap.create();
//        mapA.put("a", "b");
//        mapA.put("b", "c");
//        mapA.put("c", "a");
//		
//		System.out.println(mapA);
//		if(true) {
//			return;
//		}
		
		NodeMapperOpEquality nodeMapper = new NodeMapperOpEquality();
                
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexA = QueryContainmentIndexImpl.create(nodeMapper);
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexB = QueryContainmentIndexImpl.createFlat(nodeMapper);

        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiA = SubgraphIsomorphismIndexJena.create();
        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiB = SubgraphIsomorphismIndexJena.createFlat();

        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> sii = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiA, siiB);
        
        
        QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = QueryContainmentIndexImpl.create(sii, nodeMapper);
               
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = ValidationUtils.createValidatingProxy(QueryContainmentIndex.class, indexA, indexB);
        
        SparqlEntityManagerFactory emf = new SparqlEntityManagerFactory();
        //Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
        //SparqlService ss = FluentSparqlService.from(model).create();
        SparqlService ss = FluentSparqlService.http("http://localhost:8950/sparql").create();
        //SparqlService ss = FluentSparqlService.http("http://lsq.aksw.org/sparql").create();

        emf.setSparqlService(ss);
        emf.addScanPackageName(MainSparqlQueryToGraph.class.getPackage().getName());
        RdfEntityManager em = emf.getObject();

        List<LsqQuery> lsqQueries;

        lsqQueries = JpaUtils.createTypedQuery(em, LsqQuery.class, (cb, cq) -> {
            Root<LsqQuery> root = cq.from(LsqQuery.class);
            cq.select(root);
        }).setMaxResults(300).getResultList();

        
        // hack; should be done by the framework
        lsqQueries = Lists.newArrayList(lsqQueries);
        for(LsqQuery q : lsqQueries) {
            String id = em.getIri(q);
            q.setIri(id);
        }

        lsqQueries = lsqQueries.stream()
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList(""http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		.collect(Collectors.toList());
        
        Map<Node, Op> ops = lsqQueries.stream()
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00ebbf80", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		.map(lsqQuery -> {
                    // TODO HACK We need to fetch the iri from the em, as the mapper currently does not support placing an entity's iri into a field
                    //System.out.println("Got lsq query: " + lsqQuery);

                	Node node = NodeFactory.createURI(lsqQuery.getIri());
                    String queryStr = lsqQuery.getText();
                    Query query;
                    try {
                        query = SparqlQueryParserImpl.create().apply(queryStr);
                    } catch(Exception e) {
                        logger.warn("Failed to parse: " + queryStr);
                        //continue;
                        return null;
                    }
                    System.out.println("Got lsq query " + lsqQuery.getIri() + ": " + query);
                
                    Op op = Algebra.toQuadForm(Algebra.compile(query));

                    return new SimpleEntry<>(node, op);
        		})
        		.filter(Objects::nonNull)
        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> { throw new RuntimeException("duplicate ckey"); }, LinkedHashMap::new));
        
        //ops.put(NodeFactory.createURI("http://lsq.aksw.org/res/foobar"), ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d1b176")));
        System.out.println("Ops Size: " + ops.size());
        
        for(Entry<Node, Op> e : ops.entrySet()) {
	        Node node = e.getKey();
	        Op op = e.getValue();
        	index.put(node, op);
	    }
        
        siiA.printTree();
        
//        System.out.println("XXX: " + siiA.get(new SimpleEntry<>(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d5ab86"), 2l)));
//        System.out.println("XXX: " + siiA.get(new SimpleEntry<>(NodeFactory.createURI("http://lsq.aksw.org/res/q-00f148fa"), 0l)));
        System.out.println("XXX: " + ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d5ab86")));
        System.out.println("XXX: " + ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00f148fa")));
                	
        	
        for(Entry<Node, Op> e : ops.entrySet()) {
        	
        	logger.info("Querying view candidates of: " + e.getKey());
        	if(Arrays.asList("http://lsq.aksw.org/res/q-00d1b176").contains(e.getKey().getURI())) {
        		System.out.println("Got a specific URI: " + e.getKey());
        	}
        	
	        Op op = e.getValue();
        	index.match(op);
	    }
        
        
        
        System.out.println();
//        System.out.println("Result Index tree:");
        
        //index.getIndex().printTree();
	}

}
