package org.aksw.sparqlqc.analysis.dataset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.tyrexmo.testqc.simple.SimpleContainmentSolver;

public class MainSparqlQcDatasetAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(MainSparqlQcDatasetAnalysis.class);

    public static void main(String[] args) throws Exception {

        FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();

        Map<String, String> config = new HashMap<String, String>();
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        config.put(Constants.FRAMEWORK_BOOTDELEGATION, "*");

        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(",",
            // API
            "fr.inrialpes.tyrexmo.testqc.simple;version=\"1.0.0\"",
            "fr.inrialpes.tyrexmo.testqc;version=\"1.0.0\"",

            // Dirty API packages (probably should go elsewhere)
            "fr.inrialpes.tyrexmo.queryanalysis;version=\"1.0.0\"",

            // Jena 3
            "org.apache.jena.sparql.algebra;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.optimize;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.op;version=\"1.0.0\"",
            "org.apache.jena.sparql.algebra.expr;version=\"1.0.0\"",
            "org.apache.jena.sparql.core;version=\"1.0.0\"",
            "org.apache.jena.sparql.syntax;version=\"1.0.0\"",
            "org.apache.jena.sparql.expr;version=\"1.0.0\"",
            "org.apache.jena.sparql.graph;version=\"1.0.0\"",
            "org.apache.jena.query;version=\"1.0.0\"",
            "org.apache.jena.graph;version=\"1.0.0\"",
            "org.apache.jena.ext.com.google.common.collect;version=\"1.0.0\"",
            "org.apache.jena.sparql.engine.binding;version=\"1.0.0\"",
            "org.apache.jena.atlas.io;version=\"1.0.0\"",

            // Jena 2 (legacy)
            "com.hp.hpl.jena.sparql;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.optimize;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.op;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.algebra.expr;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.engine;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.core;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.syntax;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.expr;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.expr.nodevalue;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.util.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.query;version=\"1.0.0\"",
            "com.hp.hpl.jena.graph;version=\"1.0.0\"",
            "com.hp.hpl.jena.ext.com.google.common.collect;version=\"1.0.0\"",
            "com.hp.hpl.jena.sparql.engine.binding;version=\"1.0.0\"",

            "org.apache.xerces.util;version=\"1.0.0\"",
            "org.apache.xerces.impl.dv;version=\"1.0.0\"",
            "org.apache.xerces.xs;version=\"1.0.0\"",
            "org.apache.xerces.impl.dv.xs;version=\"1.0.0\"",
            "org.apache.xerces.impl.validation;version=\"1.0.0\"",

            "com.ibm.icu.text;version=\"1.0.0\"",

            // Logging
            "org.slf4j;version=\"1.7.0\""
//                "org.slf4j.impl;version=\"1.0.0\"",
//                "org.apache.log4j;version=\"1.0.0\""

            // ??? What packages are that?
            //"java_cup.runtime;version=\"1.0.0\""
        ));
        List<String> jarFileNames = Arrays.asList("jsa", "sparqlalgebra", "afmu", "treesolver");

        List<File> jarFiles = jarFileNames.stream().map(implStr -> {
            String jarPathStr = String.format("../sparqlqc-impl-%1$s/target/sparqlqc-impl-%1$s-1.0.0-SNAPSHOT.jar", implStr);
            File jarFile = new File(jarPathStr);
            return jarFile;
        }).collect(Collectors.toList());



        Framework framework = frameworkFactory.newFramework(config);
        framework.init();
        framework.start();


        BundleContext context = framework.getBundleContext();

        for (File jarFile : jarFiles) {
            String jarFileStr = jarFile.getAbsolutePath();
            logger.info("Loading: " + jarFileStr);

            Bundle bundle = context.installBundle("reference:file:" + jarFileStr);
            bundle.start();
        }

        ServiceReference<?>[] srs = context.getAllServiceReferences(SimpleContainmentSolver.class.getName(), null);

        Map<String, SimpleContainmentSolver> solvers = Arrays.asList(srs).stream()
                .collect(Collectors.toMap(
                        sr -> "" + sr.getProperty("SHORT_LABEL"),
                        sr -> (SimpleContainmentSolver)context.getService(sr)
                ));


        //QueryExecutionFactory qef = FluentI

        QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://localhost:8910/sparql", "http://lsq.aksw.org/swdf/").create();
        QueryExecution qe = qef.createQueryExecution("PREFIX lsq:<http://lsq.aksw.org/vocab#> CONSTRUCT { ?s lsq:text ?o } { ?s lsq:text ?o ; lsq:hasSpin [ a <http://spinrdf.org/sp#Select> ] } LIMIT 1000");

        Model in = ModelFactory.createDefaultModel();
        qe.execSelect().forEachRemaining(qs -> {
            in.add(qs.get("s").asResource(), LSQ.text, qs.get("o"));
        });

        //String xxx = "PREFIX lsq:<http://lsq.aksw.org/vocab#> CONSTRUCT { ?s ?p ?o } WHERE { { SELECT SAMPLE(?x) AS ?s (lsq:text As ?p) ?o { ?x lsq:text ?o . } GROUP BY ?o } } LIMIT 10";


//        Model in = ModelFactory.createDefaultModel();
//        in.createResource("http://ex.org/foo")
//            .addLiteral(LSQ.text, "SELECT * { ?s ?p ?o }");
//
//        in.createResource("http://ex.org/bar")
//            .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z }");
//
//        in.createResource("http://ex.org/baz")
//            .addLiteral(LSQ.text, "SELECT * { ?x ?y ?z . FILTER(?x = <test>)}");

        Supplier<Stream<Resource>> queryIterable = () -> in.listSubjects().toSet().parallelStream();

//        System.out.println(solvers);
//        if(true) return;

        Entry<String, SimpleContainmentSolver> e = solvers.entrySet().iterator().next();
//        String solverShortLabel = e.getKey();
//        SimpleContainmentSolver solver = e.getValue();

        String solverShortLabel = "JSAC";
        SimpleContainmentSolver solver = solvers.get(solverShortLabel);

        String ns = "http://lsq.aksw.org/vocab#";
        Property _isEntailed = ResourceFactory.createProperty(ns + "isEntailed-" + solverShortLabel);
        Property _isNotEntailed = ResourceFactory.createProperty(ns + "isNotEntailed-" + solverShortLabel);
        Property _isEntailmentError = ResourceFactory.createProperty(ns + "entailmentError-" + solverShortLabel);

        Stream<Statement> x = queryIterable.get()
                .peek((foo) -> System.out.println("foo: " + foo))
                .flatMap(a ->
            queryIterable.get().map(b -> {
                String aStr = a.getProperty(LSQ.text).getString();
                String bStr = b.getProperty(LSQ.text).getString();

                Model m = ModelFactory.createDefaultModel();
                try {
                    boolean isEntailed = solver.entailed(aStr, bStr);
                    if(isEntailed) {
                        m.add(a, _isEntailed, b);
                    } else {
                        m.add(a, _isNotEntailed, b);
                    }
                } catch(Exception ex) {
                    m.add(a, _isEntailmentError, b);
                    logger.warn("Entailment error", ex);
                }
                return m.listStatements().next();
        })

        );

        //x.forEach(System.out::println);

        //System.out.println(x.count());

        OutputStream out = new FileOutputStream(new File("/tmp/swdf-containment.nt"));
        x.forEach(stmt -> {
            Model m = ModelFactory.createDefaultModel();
            m.add(stmt);
            RDFDataMgr.write(out, m, RDFFormat.NTRIPLES_UTF8);
        });

        out.close();

        framework.stop();
        if(false) {
            framework.waitForStop(0);
        } else {
            System.exit(0);
        }

    }


    //public boo testContain(Resource a, Resource b)
}
