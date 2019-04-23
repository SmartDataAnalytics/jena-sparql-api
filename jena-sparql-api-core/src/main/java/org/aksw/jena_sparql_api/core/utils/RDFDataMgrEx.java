package org.aksw.jena_sparql_api.core.utils;

import java.io.IOException;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.sparql.ext.fs.JenaExtensionFs;
import org.aksw.jena_sparql_api.sparql.ext.http.JenaExtensionHttp;
import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jena_sparql_api.stmt.SPARQLResultSinkQuads;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.SinkModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.util.ModelUtils;

/**
 * Extensions to load models from .sparql files
 * 
 * @author Claus Stadler, Dec 18, 2018
 *
 */
public class RDFDataMgrEx {

	/**
	 * Execute a sequence of SPARQL update statements from a file against a model.
	 * For example, can be used to materialize triples.
	 * 
	 * @param model
	 * @param filenameOrURI
	 */
	public static void execSparql(Model model, String filenameOrURI) {
		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model))) {
			execSparql(conn, filenameOrURI);
		}
	}
	
	public static void execSparql(RDFConnection conn, String filenameOrURI) {
		readConnection(conn, filenameOrURI, null);
	}
	
	public static void readConnection(RDFConnection conn, String filenameOrURI, Consumer<Quad> quadConsumer) {
		//Sink<Quad> sink = SparqlStmtUtils.createSink(outFormat, System.out);
		
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Extended);
		JenaExtensionUtil.addPrefixes(pm);

		JenaExtensionHttp.addPrefixes(pm);

		// Extended SERVICE <> keyword implementation
		JenaExtensionFs.registerFileServiceHandler();
		
		try {
			SparqlStmtUtils.processFile(pm, filenameOrURI)
				.forEach(stmt -> SparqlStmtUtils.process(conn, stmt, new SPARQLResultSinkQuads(quadConsumer)));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a model by concatenation of a series of construct queries from a given .sparql file
	 * @param conn
	 * @param filenameOrURI
	 * @return
	 */
	public static Model execConstruct(RDFConnection conn, String filenameOrURI) {
		Model result = ModelFactory.createDefaultModel();
		readConnection(conn, filenameOrURI,
				q -> result.add(ModelUtils.tripleToStatement(result, q.asTriple())));
		return result;
	}

//	public static Model execConstruct(RDFConnection conn, String queryStr) {
//		Model result = ModelFactory.createDefaultModel();
//		readConnection(conn, filenameOrURI,
//				q -> result.add(ModelUtils.tripleToStatement(result, q.asTriple())));
//		return result;
//	}

	public static void readDataset(Dataset dataset, String filenameOrURI, Consumer<Quad> quadConsumer) {
		RDFConnection conn = RDFConnectionFactory.connect(dataset);
		readConnection(conn, filenameOrURI, quadConsumer);
	}

	public static void readModel(Model model, String filenameOrURI, Consumer<Quad> quadConsumer) {
		Dataset dataset = DatasetFactory.wrap(model);
		readDataset(dataset, filenameOrURI, quadConsumer);
	}
	
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgrEx.readModel(model, "sparql-test.sparql", null);
		
		System.out.println("Model size: " + model.size());
		
	}
}
