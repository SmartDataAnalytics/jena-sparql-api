package org.aksw.jena_sparql_api.mapper.proxy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

// TODO Rename; e.g. to simply "Prefixes" or "PrefixRegistry"
public class RDFa {
	private static final Logger logger = LoggerFactory.getLogger(RDFa.class);

	public static final PrefixMapping prefixes = new PrefixMappingImpl();

	static {
		prefixes
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/jena-extended.jsonld"))
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/arq.jsonld"))
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/rdfa11.jsonld"))
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/geosparql.jsonld"))
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/jsa.jsonld"));
	}
	
	// Apparently scanning folders in class path resources is still a pain...
	// PatternMatchingResourceResolver used to work, but I'd like to avoid a spring
	// dependency here...
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


//	public static void main(String[] args) throws Exception {
//		Path path = Paths.get(new URI("file:/home/raven/Projects/Eclipse/jena-sparql-api-parent/jena-sparql-api-mapper-proxy/target/jena-sparql-api-mapper-proxy-3.11.0-1-SNAPSHOT.jar!/rdf-prefixes"));
//		
//		System.out.println(Files.list(path));
//		System.out.println(prefixes);
//	}
}
