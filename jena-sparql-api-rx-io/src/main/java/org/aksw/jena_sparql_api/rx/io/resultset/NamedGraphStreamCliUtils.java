package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.rx.op.RxOps;
import org.aksw.commons.rx.util.RxUtils;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.SparqlScriptProcessor;
import org.aksw.jena_sparql_api.rx.SparqlScriptProcessor.Provenance;
import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformLib2;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.TransformUnionQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.engine.http.Service;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Utilities for processing a sequence of command line arguments that denote
 * RDF sources and SPARQL-based transformations into streams of data
 *
 * @author raven
 *
 */
public class NamedGraphStreamCliUtils {

    private static final Logger logger = LoggerFactory.getLogger(NamedGraphStreamCliUtils.class);

    /**
     *  If one of the args is '-' for STDIN there must not be any further arg
     *
     * @param args
     */
    public static void validateStdIn(List<String> args) {
        long stdInCount = args.stream().filter(item -> item.equals("-")).count();
        if (stdInCount != 0 && args.size() > 1) {
            throw new RuntimeException("If STDIN (denoted by '-') is used no further input sources may be used");
        }
    }

    public static Callable<TypedInputStream> validate(String filenameOrIri, Iterable<Lang> probeLangs, boolean displayProbeResult) {
        Callable<TypedInputStream> result;
        if (RDFDataMgrEx.isStdIn(filenameOrIri)) {
            TypedInputStream tin = RDFDataMgrEx.forceBuffered(RDFDataMgrEx.open(filenameOrIri, probeLangs));

            // Beware that each invocation of the supplier returns the same underlying input stream
            // however with a fresh close shield! The purpose is to allow probing on stdin
            result = () -> RDFDataMgrEx.wrapInputStream(new CloseShieldInputStream(tin.getInputStream()), tin);
        } else {
            try(TypedInputStream tin = RDFDataMgrEx.open(filenameOrIri, probeLangs)) {
                String ct = tin.getContentType();
                Lang lang = RDFLanguages.contentTypeToLang(ct);
                if (displayProbeResult) {
                    logger.info("Detected format: " + filenameOrIri + " " + ct);
                }

                result = () -> RDFDataMgrEx.forceBuffered(RDFDataMgrEx.open(filenameOrIri, Arrays.asList(lang)));
            }
        }

        return result;
    }
    /**
     * Injects stdin if there are no arguments and checks that stdin is not mixed with
     * other input sources
     *
     * @param args
     * @return
     */
    public static List<String> preprocessArgs(List<String> args) {
        List<String> result = args.isEmpty() ? Collections.singletonList("-") : args;

        validateStdIn(args);

        return result;
    }

    /**
     * Open wrapper for the convention where STDIN can be referred to by the following means:
     * - no argument given
     * - single argument matching '-'
     *
     * @param args
     * @param probeLangs
     * @return
     */
//    public static TypedInputStream open(String args, Collection<Lang> probeLangs) {
//        String src = args.isEmpty()
//                ? "-"
//                : args.get(0);
//
//        TypedInputStream result = RDFDataMgrEx.open(src, probeLangs);
//        return result;
//    }

   /**
     * Default procedure to obtain a stream of named graphs from a
     * list of non-option arguments
     *
     * If the list is empty or the first argument is '-' data will be read from stdin
     * @param args
     */
    public static Flowable<Dataset> createNamedGraphStreamFromArgs(
            List<String> rawArgs,
            String fmtHint,
            PrefixMapping pm,
            Collection<Lang> quadLangs
            ) {

        List<String> args = preprocessArgs(rawArgs);
        Map<String, Callable<TypedInputStream>> map = validate(args, quadLangs, true);


        Flowable<Dataset> result = Flowable.fromIterable(map.entrySet())
                .concatMap(arg -> {
//                        TypedInputStream tmp = RDFDataMgrEx.open(arg, MainCliNamedGraphStream.quadLangs);

                    String argName = arg.getKey();
                    logger.info("Loading stream for arg " + argName);
                    Callable<TypedInputStream> inSupp = arg.getValue();
                    Flowable<Dataset> r = RDFDataMgrRx.createFlowableDatasets(inSupp)
                    // TODO Decoding of distinguished names should go into the util method
                        .map(ds -> NodeTransformLib2.applyNodeTransform(RDFDataMgrRx::decodeDistinguished, ds));
                    return r;
                });

        return result;
    }

    /**
     * Validate whether all given arguments can be opened.
     * This is similar to probe() except that an exception is raised on error
     *
     * @param args
     * @param probeLangs
     */
    public static Map<String, Callable<TypedInputStream>> validate(List<String> args, Iterable<Lang> probeLangs, boolean displayProbeResults) {

        Map<String, Callable<TypedInputStream>> result = new LinkedHashMap<>();

        NamedGraphStreamCliUtils.validateStdIn(args);

        int violationCount = 0;
        for (String arg : args) {

            try {
                Callable<TypedInputStream> inSupp = NamedGraphStreamCliUtils.validate(arg, probeLangs, displayProbeResults);
                result.put(arg, inSupp);
            } catch(Exception e) {
                String msg = ExceptionUtils.getRootCauseMessage(e);
                logger.info(arg + ": " + msg);

                ++violationCount;
            }
        }

        if (violationCount != 0) {
            throw new IllegalArgumentException("Some arguments failed to validate");
        }

        return result;
    }



    public static void execMap(
            PrefixMapping pm,
            List<String> sourceStrs,
            Collection<Lang> quadLangs,
            List<String> stmtStrs,
            String timeoutSpec,
            String outFormat,
            long deferCount) {

        Consumer<Context> contextMutator = cxt -> {
            if (!Strings.isNullOrEmpty(timeoutSpec)) {
                cxt.set(Service.queryTimeout, timeoutSpec);
            }
        };

        SparqlScriptProcessor scriptProcessor = SparqlScriptProcessor.createWithEnvSubstitution(pm);

        // Register a (best-effort) union default graph transform
        scriptProcessor.addPostTransformer(stmt -> SparqlStmtUtils.applyOpTransform(stmt,
                op -> Transformer.transformSkipService(new TransformUnionQuery(), op)));


        scriptProcessor.process(stmtStrs);
        List<Entry<SparqlStmt, Provenance>> workloads = scriptProcessor.getSparqlStmts();

        List<SparqlStmt> stmts = workloads.stream().map(Entry::getKey).collect(Collectors.toList());

        OutputMode outputMode = OutputModes.detectOutputMode(stmts);

        // This is the final output sink
        SPARQLResultExProcessor resultProcessor = SPARQLResultExProcessorBuilder.configureProcessor(
                StdIo.openStdOutWithCloseShield(), System.err,
                outFormat,
                stmts,
                pm,
                RDFFormat.TURTLE_BLOCKS,
                RDFFormat.TRIG_BLOCKS,
                deferCount,
                false, 0, false,
                () -> {});

        // SPARQLResultExProcessor resultProcessor = resultProcessorBuilder.build();

        Function<RDFConnection, SPARQLResultEx> mapper = SparqlMappers.createMapperToSparqlResultEx(outputMode, stmts, resultProcessor);

        Flowable<SPARQLResultEx> flow =
                // Create a stream of Datasets
                NamedGraphStreamCliUtils.createNamedGraphStreamFromArgs(sourceStrs, null, pm, quadLangs)
                    // Map the datasets in parallel
                    .compose(RxOps.createParallelMapperOrdered(
                        // Map the dataset to a connection
                        SparqlMappers.mapDatasetToConnection(
                                // Set context attributes on the connection, e.g. timeouts
                                SparqlMappers.applyContextHandler(contextMutator)
                                    // Finally invoke the mapper
                                    .andThen(mapper))));

        resultProcessor.start();
        try {
//            for(SPARQLResultEx item : flow.blockingIterable(16)) {
//                System.out.println(item);
//                resultProcessor.forwardEx(item);
//            }
            RxUtils.consume(flow.map(item -> { resultProcessor.forwardEx(item); return item; }));
            resultProcessor.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            resultProcessor.flush();
            resultProcessor.close();
        }
    }

}
