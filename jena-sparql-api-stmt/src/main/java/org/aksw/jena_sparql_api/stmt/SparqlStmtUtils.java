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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ExprTransformNodeElement;
import org.aksw.jena_sparql_api.http.HttpExceptionUtils;
import org.aksw.jena_sparql_api.syntax.UpdateRequestUtils;
import org.aksw.jena_sparql_api.syntax.UpdateUtils;
import org.aksw.jena_sparql_api.utils.ElementTransformSubst2;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.base.Charsets;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.ext.com.google.common.io.CharStreams;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
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
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.modify.request.UpdateData;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class SparqlStmtUtils {

    // TODO Duplicate symbol definition; exists in E_Benchmark
    public static final Symbol symConnection = Symbol.create("http://jsa.aksw.org/connection");

    public static Map<String, Boolean> mentionedEnvVars(SparqlStmt stmt) {
        NodeTransformCollectNodes xform = new NodeTransformCollectNodes();
        applyNodeTransform(stmt, xform);
        Set<Node> nodes = xform.getNodes();
        Map<String, Boolean> result = nodes.stream()
            .map(NodeUtils::getEnvKey)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

    /**
     * Removes all unused prefixes from a stmt
     *
     * @param stmt
     * @return
     */
    public static SparqlStmt optimizePrefixes(SparqlStmt stmt) {
        optimizePrefixes(stmt, null);
        return stmt;
    }

    /**
     * In-place optimize an update request's prefixes to only used prefixes
     * The global prefix map may be null.
     *
     * @param query
     * @param pm
     * @return
     */
    public static SparqlStmt optimizePrefixes(SparqlStmt stmt, PrefixMapping globalPm) {
        if(stmt.isQuery()) {
            QueryUtils.optimizePrefixes(stmt.getQuery(), globalPm);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequestUtils.optimizePrefixes(stmt.getUpdateRequest(), globalPm);
        }
        return stmt;
    }


    public static SparqlStmt applyOpTransform(SparqlStmt stmt, Function<? super Op, ? extends Op> transform) {
        SparqlStmt result;
        if(stmt.isQuery()) {
            Query tmp = stmt.getAsQueryStmt().getQuery();
            Query query = QueryUtils.applyOpTransform(tmp, transform);
            result = new SparqlStmtQuery(query);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequest tmp = stmt.getAsUpdateStmt().getUpdateRequest();
            UpdateRequest updateRequest = UpdateRequestUtils.applyOpTransform(tmp, transform);

            result = new SparqlStmtUpdate(updateRequest);
        } else {
            result = stmt;
        }

        return result;
    }

    public static SparqlStmt applyNodeTransform(SparqlStmt stmt, NodeTransform xform) {
        SparqlStmt result;

        ElementTransform elform = new ElementTransformSubst2(xform);
        ExprTransform exform = new ExprTransformNodeElement(xform, elform);

        if(stmt.isQuery()) {
            Query before = stmt.getAsQueryStmt().getQuery();
//			Op beforeOp = Algebra.compile(before);
//			Op afterOp = NodeTransformLib.transform(xform, beforeOp);

//			NodeTransformLib.transform
//			Transformer.transform(transform, exprTransform, op)
//			Query after = OpAsQuery.asQuery(afterOp);
//			QueryUtils.restoreQueryForm(after, before);

//			Transformer.transform(new TransformCopy(), op)
//			= OpAsQuery.asQu)

            //Query after = QueryTransformOps.transform(before, elform, exform);


            //QueryTransformOps.
//			QueryUtils.applyNodeTransform(query, nodeTransform)
            Query after = QueryUtils.applyNodeTransform(before, xform);
            result = new SparqlStmtQuery(after);
        } else if(stmt.isUpdateRequest()) {

            UpdateRequest before = stmt.getAsUpdateStmt().getUpdateRequest();
            UpdateRequest after = UpdateRequestUtils.copyTransform(before, update -> {
                // Transform UpdataData ourselves as
                // up to Jena 3.11.0 (inclusive) transforms do not affect UpdateData objects
                Update r = update instanceof UpdateData
                    ? UpdateUtils.copyWithQuadTransform((UpdateData)update, q -> QuadUtils.applyNodeTransform(q, xform))
                    : UpdateTransformOps.transform(update, elform, exform);
                return r;
            });

//			ElementTransform elform = new ElementTransformSubst2(xform);
//			UpdateRequest after = UpdateTransformOps.transform(before, elform, new ExprTransformNodeElement(xform, elform));
            result = new SparqlStmtUpdate(after);
        } else {
            result = stmt;
        }

        return result;
    }


    public static SparqlStmtIterator processFile(PrefixMapping pm, String filenameOrURI)
            throws FileNotFoundException, IOException, ParseException {

        return processFile(pm, filenameOrURI, null);
    }


    public static URI extractBaseIri(String filenameOrURI) {
        Context context = null;
        StreamManager streamManager = StreamManager.get(context);

        // Code taken from jena's RDFParser
        String urlStr = streamManager.mapURI(filenameOrURI);

        URI uri;
        try {
            uri = new URI(urlStr);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
//		String result = parent.toString();
//		return result;
        return parent;
    }

    // TODO Move to utils or io - also, internally uses rdf content type for requests, which
    // is not what we want
    public static String loadString(String filenameOrURI) throws IOException {
        String result;
        try(InputStream in = openInputStream(filenameOrURI)) {
            result = in != null ? CharStreams.toString(new InputStreamReader(in, Charsets.UTF_8)) : null;
        }

        return result;
    }


    // FIXME Can we remove this in favor of RDFDataMgr.open()?
    public static TypedInputStream openInputStream(String filenameOrURI) {
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

        return in;
    }

    /**
     *
     * @param pm A <b>modifiable<b> prefix mapping
     * @param filenameOrURI
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ParseException
     */
    public static SparqlStmtIterator processFile(PrefixMapping pm, String filenameOrURI, String baseIri)
            throws FileNotFoundException, IOException, ParseException {

        InputStream in = openInputStream(filenameOrURI);
        if(in == null) {
            throw new IOException("Could not open input stream from " + filenameOrURI);
        }

        if(baseIri == null) {
            URI tmp = extractBaseIri(filenameOrURI);
            baseIri = tmp.toString();
//	        URI uri;
//			try {
//				uri = new URI(urlStr);
//			} catch (URISyntaxException e) {
//				throw new RuntimeException(e);
//			}
//	        URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
//			baseIri = parent.toString();
        }

        return processInputStream(pm, baseIri, in);
        //stmts.forEach(stmt -> process(conn, stmt, sink));
    }

    public static SparqlStmtIterator processInputStream(PrefixMapping pm, String baseIri, InputStream in)
            throws IOException, ParseException {

//		File file = new File(filename).getAbsoluteFile();
//		if(!file.exists()) {
//			throw new FileNotFoundException(file.getAbsolutePath() + " does not exist");
//		}
//
//		String dirName = file.getParentFile().getAbsoluteFile().toURI().toString();

        Prologue prologue = new Prologue();
        //prologue.getPrefixMapping().setNsPrefixes(pm);
        prologue.setPrefixMapping(pm);

        prologue.setBaseURI(baseIri);

        Function<String, SparqlStmt> rawSparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ,
                prologue, true);// .getQueryParser();


        // Wrap the parser with tracking the prefixes
        //SparqlStmtParser sparqlStmtParser = SparqlStmtParser.wrapWithNamespaceTracking(prologue.getPrefixMapping(), rawSparqlStmtParser);
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
        SparqlStmtIterator stmts = SparqlStmtUtils.parse(in, sparqlStmtParser);

        return stmts;
    }


    public static SparqlStmtIterator parse(InputStream in, Function<String, SparqlStmt> parser)
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

        String str;
        try {
            str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } finally {
            in.close();
        }
        // ARQParser parser = new ARQParser(new FileInputStream(file));
        // parser.setQuery(new Query());
        // parser.

        // SparqlStmtParser parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ,
        // PrefixMapping.Extended, true);

        //Stream<SparqlStmt> result = Streams.stream(new SparqlStmtIterator(parser, str));
        SparqlStmtIterator result = new SparqlStmtIterator(parser, str);
        return result;
    }


    public static SPARQLResultEx execAny(RDFConnection conn, SparqlStmt stmt) {
        SPARQLResultEx result = null;

        if (stmt.isQuery()) {
            SparqlStmtQuery qs = stmt.getAsQueryStmt();
            Query q = qs.getQuery();

            if(q == null) {
                String queryStr = qs.getOriginalString();
                q = QueryFactory.create(queryStr, Syntax.syntaxARQ);
            }

            //conn.begin(ReadWrite.READ);
            // SELECT -> STDERR, CONSTRUCT -> STDOUT
            QueryExecution qe = conn.query(q);
            Context cxt = qe.getContext();
            if(cxt != null) {
                cxt.set(symConnection, conn);
            }

            if (q.isConstructQuad()) {
                Iterator<Quad> it = qe.execConstructQuads();
                result = SPARQLResultEx.createQuads(it, qe::close);

            } else if (q.isConstructType()) {
                // System.out.println(Algebra.compile(q));

                Iterator<Triple> it = qe.execConstructTriples();
                result = SPARQLResultEx.createTriples(it, qe::close);
            } else if (q.isSelectType()) {
                ResultSet rs = qe.execSelect();
                result = new SPARQLResultEx(rs, qe::close);
            } else if(q.isJsonType()) {
                Iterator<JsonObject> it = qe.execJsonItems();
                result = new SPARQLResultEx(it, qe::close);
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
     * @param dataset The dataset implementation to use for non-streaming data.
     *                Allows for use of insert-order preserving dataset implementations.
     * @return
     */
    public static Sink<Quad> createSink(RDFFormat format, OutputStream out, PrefixMapping pm, Dataset dataset) {
        boolean useStreaming = format == null ||
                Arrays.asList(Lang.NTRIPLES, Lang.NQUADS).contains(format.getLang());

        Sink<Quad> result;
        if(useStreaming) {
            result = new SinkQuadOutput(out, null, null);
        } else {
            // Dataset ds = DatasetFactory.create();
            // Dataset ds = DatasetFactory.wrap(new Datasetgraphquadsim
            SinkQuadsToDataset core = new SinkQuadsToDataset(false, dataset.asDatasetGraph());

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

                    // TODO Prefixed graph names may break
                    // (where to define their namespace anyway? - e.g. in the default or the named graph?)
                    PrefixMapping usedPrefixes = new PrefixMappingImpl();

                    Stream.concat(
                            Stream.of(dataset.getDefaultModel()),
                            Streams.stream(dataset.listNames()).map(dataset::getNamedModel))
                    .forEach(m -> {
                        // PrefixMapping usedPrefixes = new PrefixMappingImpl();
                        try(Stream<Node> nodeStream = GraphUtils.streamNodes(m.getGraph())) {
                            PrefixUtils.usedPrefixes(pm, nodeStream, usedPrefixes);
                        }
                        m.clearNsPrefixMap();
                        // m.setNsPrefixes(usedPrefixes);
                    });

                    dataset.getDefaultModel().setNsPrefixes(usedPrefixes);
                    RDFDataMgr.write(out, dataset, format);
                }
            };
        }

        return result;
    }


    public static void output(
            SPARQLResultEx rr,
            SPARQLResultVisitor sink) {
        try(SPARQLResultEx r = rr) {
            SPARQLResultVisitor.forward(r, sink);
        } catch (Exception e) {
            throw HttpExceptionUtils.makeHumanFriendly(e);
        }
    }

    public static void output(
        SPARQLResultEx rr,
        Consumer<Quad> sink
    ) {
        SPARQLResultVisitor tmp = new SPARQLResultSinkQuads(sink);
        output(rr, tmp);
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

    public static void process(RDFConnection conn, SparqlStmt stmt, SPARQLResultVisitor sink) {
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

    public static Op toAlgebra(SparqlStmt stmt) {
        Op result = null;

        if(stmt.isQuery()) {
            Query q = stmt.getAsQueryStmt().getQuery();
            result = Algebra.compile(q);
        } else if(stmt.isUpdateRequest()) {
            UpdateRequest ur = stmt.getAsUpdateStmt().getUpdateRequest();
            for(Update u : ur) {
                if(u instanceof UpdateModify) {
                    Element e = ((UpdateModify)u).getWherePattern();
                    result = Algebra.compile(e);
                }
            }
        }

        return result;
    }
}
