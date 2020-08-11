package org.aksw.jena_sparql_api.common;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.io.Resources;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default prefixes used throughout jena-sparql-api
 *
 * NOTE on package / module: This class could go to a common package, on the other hand,
 * the 'stmt' module is the first to make use of it
 *
 * @author raven
 *
 */
public class DefaultPrefixes {
    private static final Logger logger = LoggerFactory.getLogger(DefaultPrefixes.class);

    public static final PrefixMapping prefixes = new PrefixMappingImpl();

    static {
        prefixes
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/jena-extended.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/arq.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/rdfa11.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/geosparql.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/dataid.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/jsa.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/mvn.jsonld"))
            .setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/rpif.jsonld"));
    }

    // TODO The issue with dynamic loading of namespaces based on class path scanning is,
    // that if we load prefixes as part of JenaSystem.init(),
    // we cause a chicken-egg-based NPE exception due to static initializers
    public static void toImproveInTheFuture() {
        String folder = "rdf-prefixes";
        String rootStr = Resources.getResource(folder).getPath();
        final Path rootPath = Paths.get(rootStr);

        List<String> files;
        try {
            files = Files.walk(rootPath)
                    .filter(Files::isRegularFile)
                    .map(path -> folder + "/" + rootPath.relativize(path).toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(String file : files) {
            Lang lang = RDFDataMgr.determineLang(file, null, null);
            if(lang == null) {
                logger.warn("Skipped prefix resource: Could not determine lang of " + file);
                continue;
            }
            Model model = RDFDataMgr.loadModel(file, lang);
            prefixes.setNsPrefixes(model);
        }
    }

//	public static void main(String[] args) {
//		Model m = RDFDataMgr.loadModel("rdf-prefixes/prefix.cc.2019-12-17.ttl");
//		RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
//	}


//	public static void main(String[] args) throws Exception {
//		Path path = Paths.get(new URI("file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-mapper-proxy/target/jena-sparql-api-mapper-proxy-3.11.0-1-SNAPSHOT.jar!/rdf-prefixes"));
//
//		System.out.println(Files.list(path));
//		System.out.println(prefixes);
//	}
}
