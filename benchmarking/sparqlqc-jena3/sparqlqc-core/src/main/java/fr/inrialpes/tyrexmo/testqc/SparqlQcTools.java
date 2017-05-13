package fr.inrialpes.tyrexmo.testqc;

import java.io.IOException;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public class SparqlQcTools {	
	private static final Logger logger = LoggerFactory.getLogger(SparqlQcTools.class);
	

    public static Map<String, SimpleContainmentSolver> solvers = null;

    public static SimpleContainmentSolver jsaSolver = null;
    public static SimpleContainmentSolver sparqlAlgebraSolver = null;
    public static SimpleContainmentSolver treeSolver = null;
    public static SimpleContainmentSolver afmuSolver = null;


    public static Framework framework = null;

	
	public static void forbidSystemExitCall() {
	    final SecurityManager securityManager = new SecurityManager() {
	        @Override
	        public void checkExit(int arg0) {
	            throw new SecurityException("Exit trapped");
	        }
	
	        @Override
	        public void checkPermission(Permission permission) {
	            if (permission.getName().startsWith("exitVM")) {
	                throw new SecurityException("Exit trapped");
	            }
	        }
	
	    };
	    System.setSecurityManager(securityManager);
	}

	public static void enableSystemExitCall() {
	    System.setSecurityManager(null);
	}

	public static void destroy() throws BundleException, InterruptedException {
	    enableSystemExitCall();
	    SparqlQcTools.framework.stop();
	    if (false) {
	        SparqlQcTools.framework.waitForStop(0);
	    }
	}

	public static void init() throws BundleException, IOException, InvalidSyntaxException {
	    forbidSystemExitCall();
	    FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
	
	    Map<String, String> config = new HashMap<String, String>();
	    config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
	    config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");
	
	    config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",",
	            // API
	            "fr.inrialpes.tyrexmo.testqc.simple;version=\"1.0.0\"", "fr.inrialpes.tyrexmo.testqc;version=\"1.0.0\"",
	
	            // Dirty API packages (probably should go elsewhere)
	            "fr.inrialpes.tyrexmo.queryanalysis;version=\"1.0.0\"",
	
	            // Jena 3
	            "org.apache.jena.sparql.algebra;version=\"1.0.0\"",
	            "org.apache.jena.sparql.algebra.optimize;version=\"1.0.0\"",
	            "org.apache.jena.sparql.algebra.op;version=\"1.0.0\"",
	            "org.apache.jena.sparql.algebra.expr;version=\"1.0.0\"",
	            "org.apache.jena.sparql.core;version=\"1.0.0\"", "org.apache.jena.sparql.syntax;version=\"1.0.0\"",
	            "org.apache.jena.sparql.expr;version=\"1.0.0\"", "org.apache.jena.sparql.graph;version=\"1.0.0\"",
	            "org.apache.jena.query;version=\"1.0.0\"", "org.apache.jena.graph;version=\"1.0.0\"",
	            "org.apache.jena.ext.com.google.common.collect;version=\"1.0.0\"",
	            "org.apache.jena.sparql.engine.binding;version=\"1.0.0\"", "org.apache.jena.atlas.io;version=\"1.0.0\"",
	
	            // Jena 2 (legacy)
	            "com.hp.hpl.jena.sparql;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.algebra;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.algebra.optimize;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.algebra.op;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.algebra.expr;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.engine;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.core;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.syntax;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.expr;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.expr.nodevalue;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.graph;version=\"1.0.0\"", "com.hp.hpl.jena.sparql.util.graph;version=\"1.0.0\"",
	            "com.hp.hpl.jena.query;version=\"1.0.0\"", "com.hp.hpl.jena.graph;version=\"1.0.0\"",
	            "com.hp.hpl.jena.ext.com.google.common.collect;version=\"1.0.0\"",
	            "com.hp.hpl.jena.sparql.engine.binding;version=\"1.0.0\"",
	
	            "org.apache.xerces.util;version=\"1.0.0\"", "org.apache.xerces.impl.dv;version=\"1.0.0\"",
	            "org.apache.xerces.xs;version=\"1.0.0\"", "org.apache.xerces.impl.dv.xs;version=\"1.0.0\"",
	            "org.apache.xerces.impl.validation;version=\"1.0.0\"",
	
	            "com.ibm.icu.text;version=\"1.0.0\"",
	
	            // Logging
	            "org.slf4j;version=\"1.7.0\""
	    // "org.slf4j.impl;version=\"1.0.0\"",
	    // "org.apache.log4j;version=\"1.0.0\""
	
	    // ??? What packages are that?
	    // "java_cup.runtime;version=\"1.0.0\""
	    ));
	
	    SparqlQcTools.framework = frameworkFactory.newFramework(config);
	    SparqlQcTools.framework.init();
	    SparqlQcTools.framework.start();
	
	    List<String> pluginNames = Arrays.asList("jsa", "sparqlalgebra", "afmu", "treesolver");
	
	    // "reference:file:" + jarFileStr
	    List<String> jarRefs = pluginNames.stream().map(pluginName ->
	        String.format("sparqlqc-impl-%1$s-1.0.0-SNAPSHOT.jar", pluginName)
	    ).collect(Collectors.toList());
	
	
	    BundleContext context = SparqlQcTools.framework.getBundleContext();
	
	    for (String jarRef : jarRefs) {
	        SparqlQcTools.logger.info("Loading: " + jarRef);
	        ClassPathResource r = new ClassPathResource(jarRef);
	
	        Bundle bundle = context.installBundle("inputstream:" + jarRef, r.getInputStream());
	        bundle.start();
	    }
	
	    ServiceReference<?>[] srs = context.getAllServiceReferences(SimpleContainmentSolver.class.getName(), null);
	
	    Map<String, SimpleContainmentSolver> loadedSolvers = Arrays.asList(srs).stream().collect(Collectors.toMap(
	            sr -> "" + sr.getProperty("SHORT_LABEL"), sr -> (SimpleContainmentSolver) context.getService(sr)));
	
	    //System.out.println(solvers);
	
	    // QueryExecutionFactory qef = FluentI
	
	
	    // String xxx = "PREFIX lsq:<http://lsq.aksw.org/vocab#> CONSTRUCT { ?s
	    // ?p ?o } WHERE { { SELECT SAMPLE(?x) AS ?s (lsq:text As ?p) ?o { ?x
	    // lsq:text ?o . } GROUP BY ?o } } LIMIT 10";
	
	    // Model in = ModelFactory.createDefaultModel();
	    // in.createResource("http://ex.org/foo")
	    // .addLiteral(LSQ.text, "SELECT * { ?s ?p ?o }");
	    //
	    // in.createResource("http://ex.org/bar")
	    // .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z }");
	    //
	    // in.createResource("http://ex.org/baz")
	    // .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z . FILTER(?x = <test>)}");
	
	    // Supplier<Stream<Resource>> queryIterable = () ->
	    // in.listSubjects().toSet().parallelStream();
	
	    // System.out.println(solvers);
	    // if(true) return;
	
	    // Entry<String, SimpleContainmentSolver> e =
	    // solvers.entrySet().iterator().next();
	    // String solverShortLabel = e.getKey();
	    // SimpleContainmentSolver solver = e.getValue();
	
	    SparqlQcTools.solvers = loadedSolvers;
	    SparqlQcTools.jsaSolver = loadedSolvers.get("JSAC");
	    SparqlQcTools.sparqlAlgebraSolver = loadedSolvers.get("SA");
	    SparqlQcTools.treeSolver = loadedSolvers.get("TS");
	    SparqlQcTools.afmuSolver = loadedSolvers.get("AFMU");
	}

}
