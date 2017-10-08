package org.aksw.jena_sparql_api.jgrapht;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
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

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MainLsqQueryContainmentEvaluation {

	private static final Logger logger = LoggerFactory.getLogger(MainLsqQueryContainmentEvaluation.class);

	
	@SuppressWarnings("unchecked")
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

        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiC = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiA, siiB);
        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> sii = siiC;
        
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

        int offset = 0;
        int limit = 10000;
        
        File qFile = new File("/tmp/queries-" + offset + "-" + limit + ".dat");

        if(qFile.exists()) {
            try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(qFile))) {
                lsqQueries = (List<LsqQuery>)in.readObject();
            }
        } else {
            lsqQueries = JpaUtils.createTypedQuery(em, LsqQuery.class, (cb, cq) -> {
                Root<LsqQuery> root = cq.from(LsqQuery.class);
                cq.select(root);
            }).setFirstResult(offset).setMaxResults(limit).getResultList();
    
            
            // hack; should be done by the framework
            lsqQueries = Lists.newArrayList(lsqQueries);
            for(LsqQuery q : lsqQueries) {
                String id = em.getIri(q);
                q.setIri(id);
            }
            
            ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(qFile));
            oout.writeObject(lsqQueries);
            oout.flush();
            oout.close();
        }

        //lsqQueries = lsqQueries.subList(00, 9000);
        
        System.out.println("# queries: "+ lsqQueries.size());

        Thread.sleep(1000);


        lsqQueries = lsqQueries.stream()
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList(""http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
        		.filter(lsqQuery -> Arrays.asList(
        		        "http://lsq.aksw.org/res/q-00dcd456", // ?s ?p ?o
        		        "http://lsq.aksw.org/res/q-00d5ab86", // 0: { ?a  rdf:type  swc:TutorialEvent . ?a ?b ?c } 2: { ?c  rdf:type  ?d }
        		        //"http://lsq.aksw.org/res/q-00dc64d6", // 
        		        "http://lsq.aksw.org/res/q-00e5a47a" // { ?instance  swc:hasProgramme  ?a . ?instance rdf:type ?dClass . }
        		        ).contains(lsqQuery.getIri()))
        		.collect(Collectors.toList());
        
        //Collections.reverse(lsqQueries);
        
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
                    //System.out.println("Got lsq query " + lsqQuery.getIri() + ": " + query);
                
                    Op op = Algebra.toQuadForm(Algebra.compile(query));

                    return new SimpleEntry<>(node, op);
        		})
        		.filter(Objects::nonNull)
        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> { throw new RuntimeException("duplicate ckey"); }, LinkedHashMap::new));
        
        //ops.put(NodeFactory.createURI("http://lsq.aksw.org/res/foobar"), ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d1b176")));
        System.out.println("Ops Size: " + ops.size());
        
        Node criticalNode = NodeFactory.createURI("http://lsq.aksw.org/res/q-00e5a47a");
        
        
        Op criticalOp = ops.get(criticalNode);
        
        Iterator<Entry<Node, Op>> it = ops.entrySet().iterator();
        while(it.hasNext()) {
            Entry<Node, Op> e = it.next();
	        Node node = e.getKey();
	        Op op = e.getValue();
//        	System.out.println("Inserted: " + node);
//        	if(node.equals(criticalNode)) {
//        		siiA.printTree();
//        	}
//
	        
	        try {
	            logger.debug("Indexing: " + node);
	            
	            if(node.equals(criticalNode)) {
	                System.out.println("Indexing ");
	            }
	            
                index.put(NodeFactory.createURI(node.getURI() + "alias1"), op);
	            index.put(node, op);
	        } catch(Exception ex) {
	            logger.warn("Failed to index: " + node + " " + op, ex);
	            it.remove();
	        }
	        //index.match(criticalOp);
        	
//        	if(node.equals(criticalNode)) {
//        		siiA.printTree();
//                //index.match(criticalOp);
//                System.out.println("SUCCESS");
//        	}
//        	if(Arrays.asList("http://lsq.aksw.org/res/q-00ea1cb7").contains(e.getKey().getURI())) {
//        		System.out.println("Got a specific URI: " + e.getKey());
//        		Thread.sleep(5000);
//        	}
//
//        	index.match(op);
	    }
        
//        siiA.printTree();
//        
//        System.out.println("HERE");
        //Thread.sleep(5000);
//        System.out.println("XXX: " + siiA.get(new SimpleEntry<>(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d5ab86"), 2l)));
//        System.out.println("XXX: " + siiA.get(new SimpleEntry<>(NodeFactory.createURI("http://lsq.aksw.org/res/q-00f148fa"), 0l)));
//        System.out.println("XXX: " + ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d5ab86")));
//        System.out.println("XXX: " + ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00f148fa")));
                	
        	
        for(int xx = 0; xx < 2; ++xx) {
        Stopwatch sw = Stopwatch.createStarted();
	        for(Entry<Node, Op> e : ops.entrySet()) {
	        	
	        	if(Arrays.asList(criticalNode).contains(e.getKey())) {
	        		System.out.println("Got a specific URI: " + e.getKey());
                    siiA.printTree();

                    //Thread.sleep(5000);
                    logger.info("Querying view candidates of: " + e.getKey());                    
                    Op op = e.getValue();
                    index.match(op);
	        	}
	        	
		    }
	        System.out.println("Time taken: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
        
        
        System.out.println();
        System.out.println("Result Index tree:");
        siiA.printTree();
        
        //index.getIndex().printTree();
	}

}
