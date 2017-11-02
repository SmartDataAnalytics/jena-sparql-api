package org.aksw.jena_sparql_api.batch.cli.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDFS;

public class MainXarql {
	public static void main(String[] args) throws IOException {
		//JenaExtensionJson.register();
		
		Path path = Paths.get(args[0]); //"/home/raven/Projects/Eclipse/trento-bike-racks/datasets/bikesharing/trento-bike-sharing.json");
		String str = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
		System.out.println(str);
		
		Model model = ModelFactory.createDefaultModel();
		//model.setNsPrefixes(PrefixMapping.Extended);
		model.getResource(path.toAbsolutePath().toUri().toString()).addLiteral(RDFS.label, str);
		
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Extended);
		JenaExtensionUtil.addPrefixes(pm);
		
		//PropertyFunction
		pm.setNsPrefix("json", JenaExtensionJson.jsonFn);
		pm.setNsPrefix("xml", JenaExtensionXml.xmlFn);
		pm.setNsPrefix("csv", JenaExtensionCsv.ns);
		
		String q = args[1];
		
		QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model)
				.config()
					.withParser(SparqlStmtParserImpl.create(Syntax.syntaxSPARQL_11, pm, false).getQueryParser())
					//.withPrefixes(PrefixMapping.Extended, false)
					.end().create();
		try(QueryExecution qe = qef.createQueryExecution(q)) {
			Model result = qe.execConstruct();
			RDFDataMgr.write(System.out, result, RDFFormat.TURTLE_PRETTY);
			//ResultSet rs = qe.execSelect();
			//System.out.println(ResultSetFormatter.asText(rs));
		} 
	}
	
}
