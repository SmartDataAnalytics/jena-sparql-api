package org.aksw.jena_sparql_api.stmt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.io.CharStreams;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.lang.SinkQuadsToDataset;
import org.apache.jena.riot.out.SinkQuadOutput;
import org.apache.jena.riot.out.SinkTripleOutput;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class SparqlStmtUtils {


	public static Stream<SparqlStmt> processFile(RDFConnection conn, PrefixMapping pm, String filenameOrURI)
			throws FileNotFoundException, IOException, ParseException {
		
		Context context = null;
		StreamManager streamManager = StreamManager.get(context);

		// Code taken from jena's RDFParser
		String urlStr = streamManager.mapURI(filenameOrURI);
        TypedInputStream in;
        urlStr = StreamManager.get(context).mapURI(urlStr);
        if ( urlStr.startsWith("http://") || urlStr.startsWith("https://") ) {
            HttpClient httpClient = null;
        	String acceptHeader = 
                ( httpClient == null ) ? WebContent.defaultRDFAcceptHeader : null; 
            in = HttpOp.execHttpGet(urlStr, acceptHeader, httpClient, null);
        } else { 
            in = streamManager.open(urlStr);
        }

        URI uri;
		try {
			uri = new URI(urlStr);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
        URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
		String dirName = parent.toString();

//		File file = new File(filename).getAbsoluteFile();
//		if(!file.exists()) {
//			throw new FileNotFoundException(file.getAbsolutePath() + " does not exist");
//		}
//		
//		String dirName = file.getParentFile().getAbsoluteFile().toURI().toString();

		Prologue prologue = new Prologue();
		prologue.setPrefixMapping(pm);

		prologue.setBaseURI(dirName);

		Function<String, SparqlStmt> rawSparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ,
				prologue, true);// .getQueryParser();

		
		// Wrap the parser with tracking the prefixes
		SparqlStmtParser sparqlStmtParser = SparqlStmtParser.wrapWithNamespaceTracking(pm, rawSparqlStmtParser);
//				Function<String, SparqlStmt> sparqlStmtParser = s -> {
//					SparqlStmt r = rawSparqlStmtParser.apply(s);
//					if(r.isParsed()) {
//						PrefixMapping pm2 = null;
//						if(r.isQuery()) {
//							pm2 = r.getAsQueryStmt().getQuery().getPrefixMapping();
//						} else if(r.isUpdateRequest()) {
//							pm2 = pm.setNsPrefixes(r.getAsUpdateStmt().getUpdateRequest().getPrefixMapping());
//						}
//						
//						if(pm2 != null) {
//							pm.setNsPrefixes(pm2);
//						}
//					}
//					return r;
//				};
		
		//InputStream in = new FileInputStream(filename);
		Stream<SparqlStmt> stmts = SparqlStmtUtils.parse(in, sparqlStmtParser);

		return stmts;
		//stmts.forEach(stmt -> process(conn, stmt, sink));
	}


	public static Stream<SparqlStmt> parse(InputStream in, Function<String, SparqlStmt> parser)
			throws IOException, ParseException {
		// try(QueryExecution qe = qef.createQueryExecution(q)) {
		// Model result = qe.execConstruct();
		// RDFDataMgr.write(System.out, result, RDFFormat.TURTLE_PRETTY);
		// //ResultSet rs = qe.execSelect();
		// //System.out.println(ResultSetFormatter.asText(rs));
		// }
		// File file = new
		// File("/home/raven/Projects/Eclipse/trento-bike-racks/datasets/test/test.sparql");
		// String str = Files.asCharSource(, StandardCharsets.UTF_8).read();

		String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));

		// ARQParser parser = new ARQParser(new FileInputStream(file));
		// parser.setQuery(new Query());
		// parser.

		// SparqlStmtParser parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ,
		// PrefixMapping.Extended, true);

		Stream<SparqlStmt> result = Streams.stream(new SparqlStmtIterator(parser, str));
		return result;
	}

	public static SPARQLResultEx execAny(RDFConnection conn, SparqlStmt stmt) {
		SPARQLResultEx result = null;

		if (stmt.isQuery()) {
			SparqlStmtQuery qs = stmt.getAsQueryStmt();
			Query q = qs.getQuery();
			//conn.begin(ReadWrite.READ);
			// SELECT -> STDERR, CONSTRUCT -> STDOUT
			QueryExecution qe = conn.query(q);
	
			if (q.isConstructQuad()) {
				Iterator<Quad> it = qe.execConstructQuads();
				result = SPARQLResultEx.createQuads(it);
				
			} else if (q.isConstructType()) {
				// System.out.println(Algebra.compile(q));
	
				Iterator<Triple> it = qe.execConstructTriples();
				result = SPARQLResultEx.createTriples(it);
			} else if (q.isSelectType()) {
				ResultSet rs = qe.execSelect();
				result = new SPARQLResultEx(rs);
			} else if(q.isJsonType()) {
				Iterator<JsonObject> it = qe.execJsonItems();
				result = new SPARQLResultEx(it);
			} else {
				throw new RuntimeException("Unsupported query type");
			}
		} else if (stmt.isUpdateRequest()) {
			UpdateRequest u = stmt.getAsUpdateStmt().getUpdateRequest();

			conn.update(u);
			result = SPARQLResultEx.createUpdateType();
		}
		
		return result;
	}

	/**
	 * Create a sink that for line based format
	 * streams directly to the output stream or collects quads in memory and emits them
	 * all at once in the given format when flushing the sink. 
	 * 
	 * @param r
	 * @param format
	 * @param out
	 * @return
	 */
	public static Sink<Quad> createSink(RDFFormat format, OutputStream out) {
		boolean useStreaming = format == null ||
				Arrays.asList(Lang.NTRIPLES, Lang.NQUADS).contains(format.getLang());

		Sink<Quad> result;
		if(useStreaming) {
			result = new SinkQuadOutput(out, null, null);
		} else {
			Dataset ds = DatasetFactory.create();
			SinkQuadsToDataset core = new SinkQuadsToDataset(false, ds.asDatasetGraph());

			return new Sink<Quad>() {
				@Override
				public void close() {
					core.close();
				}

				@Override
				public void send(Quad item) {
					core.send(item);
				}

				@Override
				public void flush() {
					core.flush();					
					RDFDataMgr.write(out, ds, format);
				}			
			};
		}
		
		return result;
	}

	public static void output(
		SPARQLResultEx r,
		Consumer<Quad> sink
	) {
		Consumer<Quad> dataSink = sink == null ? q -> {} : sink;
		
		//logger.info("Processing SPARQL Statement: " + stmt);
		if (r.isQuads()) {
			//SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
			Iterator<Quad> it = r.getQuads();
			while (it.hasNext()) {
				Quad t = it.next();
				dataSink.accept(t);
			}

		} else if (r.isTriples()) {
			// System.out.println(Algebra.compile(q));

			Iterator<Triple> it = r.getTriples();
			while (it.hasNext()) {
				Triple t = it.next();
				Quad quad = new Quad(null, t);
				dataSink.accept(quad);
			}
		} else if (r.isResultSet()) {
			ResultSet rs = r.getResultSet();
			String str = ResultSetFormatter.asText(rs);
			System.err.println(str);
		} else if(r.isJson()) {
			JsonArray tmp = new JsonArray();
			r.getJsonItems().forEachRemaining(tmp::add);
			String json = tmp.toString();
			System.out.println(json);
		} else if(r.isUpdateType()) {
			// nothing to do
		} else {
			throw new RuntimeException("Unsupported query type");
		}
	}

	public static void output(SPARQLResultEx r) {
		SinkQuadOutput dataSink = new SinkQuadOutput(System.out, null, null);
		try {
			output(r, dataSink::send);
		} finally {
			dataSink.flush();
			dataSink.close();
		}
	}
	
//	public static void output(SPARQLResultEx r) {
//		//logger.info("Processing SPARQL Statement: " + stmt);
//		if (r.isQuads()) {
//			SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
//			Iterator<Quad> it = r.getQuads();
//			while (it.hasNext()) {
//				Quad t = it.next();
//				sink.send(t);
//			}
//			sink.flush();
//			sink.close();
//
//		} else if (r.isTriples()) {
//			// System.out.println(Algebra.compile(q));
//
//			SinkTripleOutput sink = new SinkTripleOutput(System.out, null, null);
//			Iterator<Triple> it = r.getTriples();
//			while (it.hasNext()) {
//				Triple t = it.next();
//				sink.send(t);
//			}
//			sink.flush();
//			sink.close();
//		} else if (r.isResultSet()) {
//			ResultSet rs =r.getResultSet();
//			String str = ResultSetFormatter.asText(rs);
//			System.err.println(str);
//		} else if(r.isJson()) {
//			JsonArray tmp = new JsonArray();
//			r.getJsonItems().forEachRemaining(tmp::add);
//			String json = tmp.toString();
//			System.out.println(json);
//		} else {
//			throw new RuntimeException("Unsupported query type");
//		}
//	}

	public static void process(RDFConnection conn, SparqlStmt stmt, Consumer<Quad> sink) {
		SPARQLResultEx sr = execAny(conn, stmt);
		output(sr, sink);
	}
	
	
	public static void processOld(RDFConnection conn, SparqlStmt stmt) {
		//logger.info("Processing SPARQL Statement: " + stmt);

		if (stmt.isQuery()) {
			SparqlStmtQuery qs = stmt.getAsQueryStmt();
			Query q = qs.getQuery();
			q.isConstructType();
			conn.begin(ReadWrite.READ);
			// SELECT -> STDERR, CONSTRUCT -> STDOUT
			QueryExecution qe = conn.query(q);

			if (q.isConstructQuad()) {
				// ResultSetFormatter.ntrqe.execConstructTriples();
				//throw new RuntimeException("not supported yet");
				SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
				Iterator<Quad> it = qe.execConstructQuads();
				while (it.hasNext()) {
					Quad t = it.next();
					sink.send(t);
				}
				sink.flush();
				sink.close();

			} else if (q.isConstructType()) {
				// System.out.println(Algebra.compile(q));

				SinkTripleOutput sink = new SinkTripleOutput(System.out, null, null);
				Iterator<Triple> it = qe.execConstructTriples();
				while (it.hasNext()) {
					Triple t = it.next();
					sink.send(t);
				}
				sink.flush();
				sink.close();
			} else if (q.isSelectType()) {
				ResultSet rs = qe.execSelect();
				String str = ResultSetFormatter.asText(rs);
				System.err.println(str);
			} else if(q.isJsonType()) {
				String json = qe.execJson().toString();
				System.out.println(json);
			} else {
				throw new RuntimeException("Unsupported query type");
			}

			conn.end();
		} else if (stmt.isUpdateRequest()) {
			UpdateRequest u = stmt.getAsUpdateStmt().getUpdateRequest();

			conn.update(u);
		}
	}
}
