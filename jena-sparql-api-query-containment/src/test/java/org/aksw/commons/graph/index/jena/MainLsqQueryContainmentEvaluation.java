package org.aksw.commons.graph.index.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.criteria.Root;

import org.aksw.commons.collections.tagmap.TagMapSetTrie;
import org.aksw.commons.collections.tagmap.ValidationUtils;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.mapper.jpa.core.RdfEntityManager;
import org.aksw.jena_sparql_api.mapper.jpa.core.SparqlEntityManagerFactory;
import org.aksw.jena_sparql_api.mapper.util.JpaUtils;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOp;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpContainment;
import org.aksw.jena_sparql_api.query_containment.index.OpContext;
import org.aksw.jena_sparql_api.query_containment.index.ResidualMatching;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.query_containment.index.SparqlTreeMapping;
import org.aksw.jena_sparql_api.query_containment.index.TreeMapping;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.util.NodeUtils;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

public class MainLsqQueryContainmentEvaluation {

	private static final Logger logger = LoggerFactory.getLogger(MainLsqQueryContainmentEvaluation.class);

	
	@SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        SparqlEntityManagerFactory emf = new SparqlEntityManagerFactory();
        //Model model = RDFDataMgr.loadModel("lsq-sparqlqc-synthetic-simple.ttl", Lang.TURTLE);
        //SparqlService ss = FluentSparqlService.from(model).create();
        //SparqlService ss = FluentSparqlService.http("http://localhost:8950/sparql").create();
        SparqlService ss = FluentSparqlService.http("http://lsq.aksw.org/sparql").create();

        emf.setSparqlService(ss);
        emf.addScanPackageName(SubGraphIsomorphismIndexTests.class.getPackage().getName());
        RdfEntityManager em = emf.getObject();

        List<LsqQuery> lsqQueries;

        int offset = 0;
        int limit = 30000;
        
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
	    
	    
	    
		int n = 1;
		for(int i = 0; i < n; ++i) {
			doRun(lsqQueries);
			
			if(i + 1 != n) {
				//Thread.sleep(3000);
			}
		}
	}
	
    public static void doRun(List<LsqQuery> lsqQueries) throws Exception {
	
//        BiMap<String, String> mapA = HashBiMap.create();
//        mapA.put("a", "b");
//        mapA.put("b", "c");
//        mapA.put("c", "a");
//		
//		System.out.println(mapA);
//		if(true) {
//			return;
//		}

        List<Node> nodesA = Arrays.asList(
		        "http://lsq.aksw.org/res/q-00ea1cb7",  // C
		        "http://lsq.aksw.org/res/q-0a1604c3", // B
		        "http://lsq.aksw.org/res/q-00dcd456", // A ?s ?p ?o
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesB = Arrays.asList(
		        "http://lsq.aksw.org/res/q-00dcd456", // A ?s ?p ?o
		        "http://lsq.aksw.org/res/q-0a1604c3", // B
		        "http://lsq.aksw.org/res/q-00ea1cb7",  // C
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesC = Arrays.asList(
		        "http://lsq.aksw.org/res/q-00d1b176",
		        "http://lsq.aksw.org/res/q-00dcd456", // A ?s ?p ?o
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        //"http://lsq.aksw.org/res/q-00d1b176",

// http://lsq.aksw.org/res/q-08319d47: 
//        29 SELECT DISTINCT  ?nombre ?confec ?tema ?even
//        		 30 WHERE
//        		 31   { ?person  rdf:type      foaf:Person ;
//        		 32              foaf:name     ?nombre ;
//        		 33              foaf:page     ?page ;
//        		 34              foaf:made     ?confec .
//        		 35     ?confec  dc:title      ?tema ;
//        		 36              swc:isPartOf  ?even
//        		 37   }
//        		 38 LIMIT   1000
        List<Node> nodesD = Arrays.asList(
		        "http://lsq.aksw.org/res/q-08319d47",   // C { 05bb5a8c + 4 more TPs }
		        "http://lsq.aksw.org/res/q-00dcd456",   // A { ?s ?p ?o }
		        "http://lsq.aksw.org/res/q-05bb5a8c",   // B { ?person foaf:made ?paper . ?paper dc:title ?title }
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesE = Arrays.asList(
        		"http://lsq.aksw.org/res/q-07dd2d6f",
		        "http://lsq.aksw.org/res/q-05bb5a8c",   // B { ?person foaf:made ?paper . ?paper dc:title ?title }
		        "http://lsq.aksw.org/res/q-08319d47",   // C { 05bb5a8c + 4 more TPs }
		        "http://lsq.aksw.org/res/q-00dcd456",   // A { ?s ?p ?o }
		        "http://lsq.aksw.org/res/q-07e1980e",
		        "http://lsq.aksw.org/res/q-02b76ce3",
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesF = Arrays.asList(
		        "http://lsq.aksw.org/res/q-05bb5a8c",   // B { ?person foaf:made ?paper . ?paper dc:title ?title }
		        "http://lsq.aksw.org/res/q-08319d47",   // C { 05bb5a8c + 4 more TPs }
		        "http://lsq.aksw.org/res/q-00dcd456",   // A { ?s ?p ?o }
		        "http://lsq.aksw.org/res/q-07e1980e",
		        "http://lsq.aksw.org/res/q-02b76ce3",
		        "http://lsq.aksw.org/res/q-07dd2d6f", // had wrong result in output
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesG = Arrays.asList(
		        "http://lsq.aksw.org/res/q-0a553057",   // B { ?person foaf:made ?paper . ?paper dc:title ?title }
		        "http://lsq.aksw.org/res/q-0a3ec3ad",
		        "http://foobar"
		        ).stream().map(NodeFactory::createURI).collect(Collectors.toList());
        
        List<Node> nodesH = Arrays.asList(
                "http://lsq.aksw.org/res/q-1f7e2778",
                "http://lsq.aksw.org/res/q-1cc48e1a", 
                "http://foobar"
            ).stream().map(NodeFactory::createURI).collect(Collectors.toList());
        List<Node> nodesY = Arrays.asList(
                "http://lsq.aksw.org/res/q-1f7e2778", 
                "http://lsq.aksw.org/res/q-1cb02a53", 
                "http://lsq.aksw.org/res/q-1a9649ac", 
                "http://lsq.aksw.org/res/q-1f3e6981", 
                "http://lsq.aksw.org/res/q-219e9dfd", 
                "http://lsq.aksw.org/res/q-21928849", 
                "http://lsq.aksw.org/res/q-215099fb", 
                "http://lsq.aksw.org/res/q-1d26f81c", 
                "http://lsq.aksw.org/res/q-21a0a856", 
                "http://lsq.aksw.org/res/q-1cb3555d", 
                "http://lsq.aksw.org/res/q-21582481", 
                "http://lsq.aksw.org/res/q-1ee03b8c", 
                "http://lsq.aksw.org/res/q-2167d571", 
                "http://lsq.aksw.org/res/q-1c2ca6b6", 
                "http://lsq.aksw.org/res/q-21587b79", 
                "http://lsq.aksw.org/res/q-1ed4652e", 
                "http://lsq.aksw.org/res/q-1cf9c6c1", 
                "http://lsq.aksw.org/res/q-1cdca5ea", 
                "http://lsq.aksw.org/res/q-1fb633fc", 
                "http://lsq.aksw.org/res/q-2199136d", 
                "http://lsq.aksw.org/res/q-1f147eba", 
                "http://lsq.aksw.org/res/q-1f1b73fc", 
                "http://lsq.aksw.org/res/q-1f5daf7c", 
                "http://lsq.aksw.org/res/q-1cc48e1a", 
//              "http://lsq.aksw.org/res/q-1ee03b8c", 
//                "http://lsq.aksw.org/res/q-1f8f40ce", 
//                "http://lsq.aksw.org/res/q-1c5e53d2", 
                "http://foobar"
            ).stream().map(NodeFactory::createURI).collect(Collectors.toList());

        List<Node> nodesZ = Arrays.asList(
"http://lsq.aksw.org/res/q-01a34de1", 
"http://lsq.aksw.org/res/q-02f0c3d7", 
"http://lsq.aksw.org/res/q-1542fa51", 
"http://lsq.aksw.org/res/q-12b1169e", 
"http://lsq.aksw.org/res/q-1542fa51", 
"http://lsq.aksw.org/res/q-1cb02a53", 
"http://lsq.aksw.org/res/q-0a71648b", 
"http://lsq.aksw.org/res/q-0d9f0243", 
"http://lsq.aksw.org/res/q-21928849", 
"http://lsq.aksw.org/res/q-0cd88027", 
"http://lsq.aksw.org/res/q-0a696fe5", 
"http://lsq.aksw.org/res/q-00d5ab86", 
"http://lsq.aksw.org/res/q-1754371d", 
"http://lsq.aksw.org/res/q-010162bb", 
"http://lsq.aksw.org/res/q-174465fd", 
"http://lsq.aksw.org/res/q-080bb526", 
"http://lsq.aksw.org/res/q-21a0a856", 
"http://lsq.aksw.org/res/q-0da4b853", 
"http://lsq.aksw.org/res/q-08356904", 
"http://lsq.aksw.org/res/q-08356904", 
"http://lsq.aksw.org/res/q-0f7034af", 
"http://lsq.aksw.org/res/q-12199024", 
"http://lsq.aksw.org/res/q-12199024", 
"http://lsq.aksw.org/res/q-1ee03b8c", 
"http://lsq.aksw.org/res/q-1ee03b8c", 
"http://lsq.aksw.org/res/q-11202793", 
"http://lsq.aksw.org/res/q-0a1c0f8f", 
"http://lsq.aksw.org/res/q-07d50c32", 
"http://lsq.aksw.org/res/q-0569e351", 
"http://lsq.aksw.org/res/q-1cdca5ea", 
"http://lsq.aksw.org/res/q-0a763963", 
"http://lsq.aksw.org/res/q-0790fbb5", 
"http://lsq.aksw.org/res/q-05e5a05c", 
"http://lsq.aksw.org/res/q-11e008fa", 
"http://lsq.aksw.org/res/q-079b2194", 
"http://lsq.aksw.org/res/q-14fdf694", 
"http://lsq.aksw.org/res/q-1f8f40ce", 
"http://lsq.aksw.org/res/q-02d86ac1", 
"http://lsq.aksw.org/res/q-0fb1cf93", 
"http://lsq.aksw.org/res/q-00f8ee20", 
"http://lsq.aksw.org/res/q-07c8aab2", 
"http://lsq.aksw.org/res/q-00f148fa", 
"http://lsq.aksw.org/res/q-02adf4b1", 
"http://lsq.aksw.org/res/q-14d67ede", 
"http://lsq.aksw.org/res/q-020d7175", 
"http://lsq.aksw.org/res/q-1a2aad83", 
"http://lsq.aksw.org/res/q-0ab566e6", 
"http://lsq.aksw.org/res/q-00dcd456", 
"http://lsq.aksw.org/res/q-1a183566", 
"http://lsq.aksw.org/res/q-01d133fa", 
"http://lsq.aksw.org/res/q-1cc48e1a", 
"http://lsq.aksw.org/res/q-1f3e6981", 
"http://lsq.aksw.org/res/q-219e9dfd", 
"http://lsq.aksw.org/res/q-1a4d9ad2", 
"http://lsq.aksw.org/res/q-0af14c34", 
"http://lsq.aksw.org/res/q-12327a61", 
"http://lsq.aksw.org/res/q-123de850", 
"http://lsq.aksw.org/res/q-1a8b0b01", 
"http://lsq.aksw.org/res/q-02315601", 
"http://lsq.aksw.org/res/q-1a72a026", 
"http://lsq.aksw.org/res/q-02f54dfa", 
"http://lsq.aksw.org/res/q-0fa8db3b", 
"http://lsq.aksw.org/res/q-02315601", 
"http://lsq.aksw.org/res/q-1cd2a277", 
"http://lsq.aksw.org/res/q-19ab9b8b", 
"http://lsq.aksw.org/res/q-1776a369", 
"http://lsq.aksw.org/res/q-0a5eecac", 
"http://lsq.aksw.org/res/q-19e81324", 
"http://lsq.aksw.org/res/q-21582481", 
"http://lsq.aksw.org/res/q-01923335", 
"http://lsq.aksw.org/res/q-0d78e814", 
"http://lsq.aksw.org/res/q-15321a3e", 
"http://lsq.aksw.org/res/q-19dd80e1", 
"http://lsq.aksw.org/res/q-11a43795", 
"http://lsq.aksw.org/res/q-21587b79", 
"http://lsq.aksw.org/res/q-1c2ca6b6", 
"http://lsq.aksw.org/res/q-1ed4652e", 
"http://lsq.aksw.org/res/q-0d7b6130", 
"http://lsq.aksw.org/res/q-0a741431", 
"http://lsq.aksw.org/res/q-086f78bd", 
"http://lsq.aksw.org/res/q-147b92be", 
"http://lsq.aksw.org/res/q-147b92be", 
"http://lsq.aksw.org/res/q-148da8c1", 
"http://lsq.aksw.org/res/q-12298a9a", 
"http://lsq.aksw.org/res/q-12d10d80", 
"http://lsq.aksw.org/res/q-0d7b6130", 
"http://lsq.aksw.org/res/q-12298a9a", 
"http://lsq.aksw.org/res/q-1f147eba", 
"http://lsq.aksw.org/res/q-0575fd2b", 
"http://lsq.aksw.org/res/q-054a1247", 
"http://lsq.aksw.org/res/q-1f5daf7c", 
"http://lsq.aksw.org/res/q-1edbcfdd", 
"http://lsq.aksw.org/res/q-030c36b8", 
"http://lsq.aksw.org/res/q-19ba0ec5", 
"http://lsq.aksw.org/res/q-02918d28", 
"http://lsq.aksw.org/res/q-14c796ed", 
"http://lsq.aksw.org/res/q-11fd9147", 
"http://lsq.aksw.org/res/q-054b7b9c", 
"http://lsq.aksw.org/res/q-054b7b9c", 
"http://lsq.aksw.org/res/q-0170fb32", 
"http://lsq.aksw.org/res/q-0d2cd9be", 
"http://lsq.aksw.org/res/q-0a3355ff", 
"http://lsq.aksw.org/res/q-115fc1dd", 
"http://lsq.aksw.org/res/q-0a9f338b", 
"http://lsq.aksw.org/res/q-17a074e9", 
"http://lsq.aksw.org/res/q-17267dc9", 
"http://lsq.aksw.org/res/q-12bc14cd", 
"http://lsq.aksw.org/res/q-11c1ddef", 
"http://lsq.aksw.org/res/q-11a160b2", 
"http://lsq.aksw.org/res/q-17f2a865", 
"http://lsq.aksw.org/res/q-11de2a8a", 
"http://lsq.aksw.org/res/q-081ae29d", 
"http://lsq.aksw.org/res/q-08105bc3", 
"http://lsq.aksw.org/res/q-1d26f81c", 
"http://lsq.aksw.org/res/q-0a6ecbd0", 
"http://lsq.aksw.org/res/q-115c3b05", 
"http://lsq.aksw.org/res/q-0fd1e2ec", 
"http://lsq.aksw.org/res/q-2167d571", 
"http://lsq.aksw.org/res/q-11767874", 
"http://lsq.aksw.org/res/q-123028d6", 
"http://lsq.aksw.org/res/q-02d1bd69", 
"http://lsq.aksw.org/res/q-0a2ae7ee", 
"http://lsq.aksw.org/res/q-07ef75ef", 
"http://lsq.aksw.org/res/q-0a2ae7ee", 
"http://lsq.aksw.org/res/q-0118ea9f", 
"http://lsq.aksw.org/res/q-14c14d6d", 
"http://lsq.aksw.org/res/q-11f05e0a", 
"http://lsq.aksw.org/res/q-1f1b73fc", 
"http://lsq.aksw.org/res/q-12abf2b0", 
"http://lsq.aksw.org/res/q-01aadfa3", 
"http://lsq.aksw.org/res/q-1a488197", 
"http://lsq.aksw.org/res/q-0fc60c0a", 
"http://lsq.aksw.org/res/q-0d0c9ef9", 
"http://lsq.aksw.org/res/q-0fb45505", 
"http://lsq.aksw.org/res/q-1531be3f", 
"http://lsq.aksw.org/res/q-11988af7", 
"http://lsq.aksw.org/res/q-129355cf", 
"http://lsq.aksw.org/res/q-0fe4c85b", 
"http://lsq.aksw.org/res/q-07eba3e9", 
"http://lsq.aksw.org/res/q-013dd9ba", 
"http://lsq.aksw.org/res/q-1f7e2778", 
"http://lsq.aksw.org/res/q-129d4e8b", 
"http://lsq.aksw.org/res/q-08237184", 
"http://lsq.aksw.org/res/q-011578c8", 
"http://lsq.aksw.org/res/q-024c7959", 
"http://lsq.aksw.org/res/q-02c8874a", 
"http://lsq.aksw.org/res/q-02c64fbf", 
"http://lsq.aksw.org/res/q-215099fb", 
"http://lsq.aksw.org/res/q-0ade4cb9", 
"http://lsq.aksw.org/res/q-07dc0d33", 
"http://lsq.aksw.org/res/q-1cb3555d", 
"http://lsq.aksw.org/res/q-0cedd37c", 
"http://lsq.aksw.org/res/q-1045f148", 
"http://lsq.aksw.org/res/q-0cedd37c", 
"http://lsq.aksw.org/res/q-1045f148", 
"http://lsq.aksw.org/res/q-10041501", 
"http://lsq.aksw.org/res/q-0acbee2d", 
"http://lsq.aksw.org/res/q-0fd5e688", 
"http://lsq.aksw.org/res/q-17c60ddc", 
"http://lsq.aksw.org/res/q-01157e1c", 
"http://lsq.aksw.org/res/q-0cb6747e", 
"http://lsq.aksw.org/res/q-011fb6d0", 
"http://lsq.aksw.org/res/q-0f7bd7b6", 
"http://lsq.aksw.org/res/q-1254e631", 
"http://lsq.aksw.org/res/q-11fcead0", 
"http://lsq.aksw.org/res/q-07df7392", 
"http://lsq.aksw.org/res/q-1cf9c6c1", 
"http://lsq.aksw.org/res/q-05216d11", 
"http://lsq.aksw.org/res/q-0ab6b994", 
"http://lsq.aksw.org/res/q-150fb1a0", 
"http://lsq.aksw.org/res/q-1fb633fc", 
"http://lsq.aksw.org/res/q-150fb1a0", 
"http://lsq.aksw.org/res/q-031f73bc", 
"http://lsq.aksw.org/res/q-2199136d", 
"http://lsq.aksw.org/res/q-0260f440", 
"http://lsq.aksw.org/res/q-0260f440", 
"http://lsq.aksw.org/res/q-0d5b6dd8", 
"http://lsq.aksw.org/res/q-1214800d", 
"http://lsq.aksw.org/res/q-12108897", 
"http://lsq.aksw.org/res/q-1480df3b", 
"http://lsq.aksw.org/res/q-121462cd", 
"http://lsq.aksw.org/res/q-0a34baa3", 
"http://lsq.aksw.org/res/q-1c5e53d2", 
"http://lsq.aksw.org/res/q-11551f6e", 
"http://lsq.aksw.org/res/q-19ee74be", 
"http://lsq.aksw.org/res/q-17de8414", 
"http://lsq.aksw.org/res/q-07e9506e", 
"http://lsq.aksw.org/res/q-0a9f78b5", 
"http://lsq.aksw.org/res/q-05e0acfb", 
"foobar"
).stream().map(NodeFactory::createURI).collect(Collectors.toList());
        
        //"http://lsq.aksw.org/res/q-00d5ab86"
        
        
        List<Node> nodesU = Arrays.asList(
                "http://lsq.aksw.org/res/q-00d5ab86",
                "http://lsq.aksw.org/res/q-00f148fa", 
                "http://foobar"
            ).stream().map(NodeFactory::createURI).collect(Collectors.toList());
        
        Set<Node> nodesX = new HashSet<Node>();
        nodesX.addAll(nodesU);
//        nodesX.addAll(nodesE);
//        nodesX.addAll(nodesG);
//        nodesX.addAll(nodesF);
        
        List<Node> filter = null; //new ArrayList<>(nodesX); //nodesF;
    	boolean shuffle = false;
        Node criticalNode = null; //NodeFactory.createURI("http://lsq.aksw.org/res/q-08237184");

        
    	//Collections.reverse(nodesD);
    	
        if(filter != null) {
        	if(shuffle) {
        		Collections.shuffle(filter);
        	}
            System.out.println("ORDER: " + filter);
        }

    	
		//BiFunction<OpContext, OpContext, NodeMapperOp> nodeMapperFactory = (a, b) -> new NodeMapperOpEquality();
          
    	TriFunction<OpContext, OpContext, Table<Op, Op, BiMap<Node, Node>>, NodeMapperOp> nodeMapperFactory = NodeMapperOpContainment::new; //(aContext, bContext) -> new NodeMapperOpContainment(aContext, bContext);

        //TriFunction<OpContext, OpContext, Table<Op, Op, BiMap<Node, Node>>, NodeMapperOp> nodeMapperFactory = NodeMapperOpEquality::new;
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexA = QueryContainmentIndexImpl.create(nodeMapper);
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexB = QueryContainmentIndexImpl.createFlat(nodeMapper);

        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> siiTreeTags = SubgraphIsomorphismIndexJena.create();
        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> siiFlat = SubgraphIsomorphismIndexJena.createFlat();
        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> siiTagBased = SubgraphIsomorphismIndexJena.createTagBased(new TagMapSetTrie<>(NodeUtils::compareRDFTerms));

        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> siiValidating = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiTreeTags, siiTagBased);
        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> sii = siiTreeTags; //siiValidating;
        
        SparqlQueryContainmentIndex<Node, ResidualMatching>index = SparqlQueryContainmentIndexImpl.create(sii, nodeMapperFactory);
               
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> index = ValidationUtils.createValidatingProxy(QueryContainmentIndex.class, indexA, indexB);
        
        lsqQueries = lsqQueries.subList(0, 10000);
        //lsqQueries = lsqQueries.subList(8000, 8100);
        //lsqQueries = lsqQueries.subList(15000, 20000);
        //lsqQueries = lsqQueries.subList(20000, 30000);
        
        System.out.println("# initial queries: "+ lsqQueries.size());

        if(filter != null) {
            lsqQueries = lsqQueries.stream()
            		.filter(lsqQuery -> filter.contains(NodeFactory.createURI(lsqQuery.getIri())))
            		.collect(Collectors.toList());        	
        }

//        lsqQueries = lsqQueries.stream()
//        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
//        		//.filter(lsqQuery -> Arrays.asList(""http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456").contains(lsqQuery.getIri()))
//        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
//        		//.filter(lsqQuery -> Arrays.asList("http://lsq.aksw.org/res/q-00f148fa", "http://lsq.aksw.org/res/q-00d5ab86", "http://lsq.aksw.org/res/q-00dcd456", "http://lsq.aksw.org/res/q-00d1b176").contains(lsqQuery.getIri()))
//        		.filter(lsqQuery -> nodes.contains(NodeFactory.createURI(lsqQuery.getIri())))
//        		.collect(Collectors.toList());
        
        //Collections.reverse(lsqQueries);
        
        Map<Node, Op> ops2 = lsqQueries.stream()
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
                    
                    
                    if(filter != null) {
                    	Query q = query.cloneQuery();
                        q.setPrefixMapping(QueryUtils.usedPrefixes(query));
                    	
                    	System.out.println("Got lsq query " + lsqQuery.getIri() + ":\n" + q);
                    }
                    
                    // We only want select queries for now
                    if(!query.isSelectType()) {
                    	return null;
                    }
                    
                    Op op = Algebra.toQuadForm(Algebra.compile(query));

                    return new SimpleEntry<>(node, op);
        		})
        		.filter(Objects::nonNull)
        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> { throw new RuntimeException("duplicate ckey"); }, LinkedHashMap::new));
        
        //ops.put(NodeFactory.createURI("http://lsq.aksw.org/res/foobar"), ops.get(NodeFactory.createURI("http://lsq.aksw.org/res/q-00d1b176")));
        
        //Node criticalNode = NodeFactory.createURI("http://lsq.aksw.org/res/q-00e5a47a");

        Map<Node, Op> ops;
        if(filter != null) { 

	        ops = filter.stream()
	        		.map(node -> new SimpleEntry<>(node, ops2.get(node)))
	        		.filter(e -> e.getValue() != null)
	        		.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> { throw new RuntimeException("duplicate ckey"); }, LinkedHashMap::new));
	        
        } else {
	        ops = ops2;
        }

        System.out.println("# remaining queries: "+ ops.size());
        Thread.sleep(1000);

        Op criticalOp = ops.get(criticalNode);
        
        List<Entry<Node, Op>> opList = new ArrayList<>(ops.entrySet());
        
        
//        System.out.println("SHUFFLE: " + opList.stream().map(Entry::getKey).collect(Collectors.toList()));
        
        Iterator<Entry<Node, Op>> it = opList.iterator();
        int indexCounter = 0;
        while(it.hasNext()) {
            Entry<Node, Op> e = it.next();
	        Node node = e.getKey();
	        Op op = e.getValue();
//        	System.out.println("Inserted: " + node);
//        	if(node.equals(criticalNode)) {
//        		siiA.printTree();
//        	}
//
	
	        // Lets filter out spo
//	        boolean isSpo = false;
//	        Query q = OpAsQuery.asQuery(op);
//
//	        Element element = q.getQueryPattern();
//	        if(element instanceof ElementTriplesBlock) {
//	            List<Triple> triples = ((ElementTriplesBlock)element).getPattern().getList();
//
//	            if(triples.size() == 1) {
//
//	                Triple triple = triples.get(0);
//
//	                // TODO Refactor into e.g. ElementUtils.isVarsOnly(element)
//	                isSpo =
//	                        triple.getSubject().isVariable() &&
//	                        triple.getPredicate().isVariable() &&
//	                        triple.getObject().isVariable();
//
//	            }
//	        }
//
//	        
//	        if(isSpo) {
//	        	logger.debug("Skipping spo " + node);
//	        	continue;
//	        }
	        
	        try {
	            logger.debug("Indexing [" + ++indexCounter + "]: " + node);
	            
	            if(node.equals(criticalNode)) {
	                System.out.println("Indexing ");
	            }
	            
                //index.put(NodeFactory.createURI(node.getURI() + "alias1"), op);
	            index.put(node, op);
	            //System.out.println("Op: " + OpAsQuery.asQuery(op));
	            //System.out.println("Current Index tree:");
	            //siiTreeTags.printTree();

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
                	
        	
        for(int xx = 0; xx < 1; ++xx) {
        	Stopwatch sw = Stopwatch.createStarted();
        	int seenQueryCount = 0;
	        for(Entry<Node, Op> e : opList) {
	        	Node key = e.getKey();
	        	
	        	if(criticalNode == null || Arrays.asList(criticalNode).contains(e.getKey())) {
//	        		System.out.println("Got a specific URI: " + e.getKey());
//                    siiA.printTree();

                    //Thread.sleep(5000);
	        		Multimap<Node, SparqlTreeMapping<ResidualMatching>> matches = ArrayListMultimap.create();
	        		
                    System.out.println("Querying view candidates of: " + e.getKey());                    
                    Op op = e.getValue();
                    try {
                    	//index.match(op);                    	
                    	 index.match(op).forEach(item -> {
                             matches.put(item.getKey(), item.getValue());
                    	 });
                    } catch(Throwable ex) { // We need to catch Assertion*Error*
                    	logger.error("Failed match", ex);
                    	siiTreeTags.printTree();
                    	//Thread.sleep(60000);
                    	
                    	index.match(op);
                    	//throw new RuntimeException(ex);
                    	//break;
                    }

//	               	 if(matches.isEmpty()) {
//	            		 throw new RuntimeException("failed");
//	            	 }
	               	 	               	 
	               	 // Find identity mapping:
	            	 //TreeMapping<Op, Op, BiMap<Node, Node>, Op> treeMap = matches.get(key).stream().filter(tm -> tm.getOverallMatching().isEmpty()).findFirst().orElse(null);
	            	 
	               	 // Find all isomorphic ones:
	               	Set<Node> isomorphicKeys = matches.entries().stream().filter(jj -> {
	               		SparqlTreeMapping<ResidualMatching> treeMapping = jj.getValue();
	               		//TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching> treeMapping = jj.getValue();
	               		Op rootA = treeMapping.getaTree().getRoot();
	               		Op rootB = treeMapping.getbTree().getRoot();
	               		
	               		Table<Op, Op, ResidualMatching> m = jj.getValue().getNodeMappings();
	               		Map<Op, ResidualMatching> targets = m.row(rootA);
	               		
	               		boolean containsRoot = targets.containsKey(rootB);
	               		return containsRoot;
	               	})
	               	.map(jj -> jj.getKey()).collect(Collectors.toSet());
	               	if(isomorphicKeys.size() > 1) { 
	               		System.out.println(key + " is isomorphic to: " + isomorphicKeys);
	               		//Thread.sleep(10000);
	               	}

                    
                    
	        	}
	        	                
    	        ++seenQueryCount;
    	        double elapsedSeconds = sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0;
    	        double rateInSeconds = elapsedSeconds / (double)seenQueryCount;
    	        System.out.println("Rate [after " + seenQueryCount + " queries]: " + rateInSeconds);
		  	}
	        
	        System.out.println("Time taken: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));	        
        }
        
        
        System.out.println();
        //System.out.println("Result Index tree:");
        //siiTreeTags.printTree();
        
        //index.getIndex().printTree();
	}

    
}
