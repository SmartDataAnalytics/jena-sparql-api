package org.aksw.jena_sparql_api.rx.io.resultset;

import java.io.Closeable;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jena_sparql_api.json.RdfJsonUtils;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;

import com.google.gson.JsonElement;

public class SPARQLResultExProcessorBuilder {
	protected OutputMode outputMode;

    protected Supplier<? extends OutputStream> out;
    protected Supplier<? extends OutputStream> err;
    protected String outFormat;
    protected PrefixMapping prefixMapping;
    protected RDFFormat tripleFormat;
    protected RDFFormat quadFormat;
    protected long deferCount;
    protected boolean jqMode;
    protected int jqDepth;
    protected boolean jqFlatMode;
    protected Closeable closeAction;

    /* The collection of statements can be used for auto-configuration of the output mode -
     * it is NOT used for processing! */
    protected Collection<? extends SparqlStmt> stmts;
    
    public Supplier<? extends OutputStream> getOut() {
		return out;
	}
	
	public OutputMode getOutputMode() {
		return outputMode;
	}

	public SPARQLResultExProcessorBuilder setOutputMode(OutputMode outputMode) {
		this.outputMode = outputMode;
		return this;
	}

	public SPARQLResultExProcessorBuilder setOut(Supplier<? extends OutputStream> out) {
		this.out = out;
		return this;
	}
	
	public Supplier<? extends OutputStream> getErr() {
		return err;
	}

	public SPARQLResultExProcessorBuilder setErr(Supplier<? extends OutputStream> err) {
		this.err = err;
		return this;
	}

	public String getOutFormat() {
		return outFormat;
	}
	
	public SPARQLResultExProcessorBuilder setOutFormat(String outFormat) {
		this.outFormat = outFormat;
		return this;
	}
	
	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}

	public SPARQLResultExProcessorBuilder setPrefixMapping(PrefixMapping prefixMapping) {
		this.prefixMapping = prefixMapping;
		return this;
	}
	
	public RDFFormat getTripleFormat() {
		return tripleFormat;
	}

	public SPARQLResultExProcessorBuilder setTripleFormat(RDFFormat tripleFormat) {
		this.tripleFormat = tripleFormat;
		return this;
	}
	
	public RDFFormat getQuadFormat() {
		return quadFormat;
	}
	
	public SPARQLResultExProcessorBuilder setQuadFormat(RDFFormat quadFormat) {
		this.quadFormat = quadFormat;
		return this;
	}
	
	public long getDeferCount() {
		return deferCount;
	}

	public SPARQLResultExProcessorBuilder setDeferCount(long deferCount) {
		this.deferCount = deferCount;
		return this;
	}

	public boolean isJqMode() {
		return jqMode;
	}
	
	public SPARQLResultExProcessorBuilder setJqMode(boolean jqMode) {
		this.jqMode = jqMode;
		return this;
	}
	
	public int getJqDepth() {
		return jqDepth;
	}

	public SPARQLResultExProcessorBuilder setJqDepth(int jqDepth) {
		this.jqDepth = jqDepth;
		return this;
	}
	
	public boolean isJqFlatMode() {
		return jqFlatMode;
	}

	public SPARQLResultExProcessorBuilder setJqFlatMode(boolean jqFlatMode) {
		this.jqFlatMode = jqFlatMode;
		return this;
	}
	
	public Closeable getCloseAction() {
		return closeAction;
	}

	public SPARQLResultExProcessorBuilder setCloseAction(Closeable closeAction) {
		this.closeAction = closeAction;
		return this;
	}
	
    public Collection<? extends SparqlStmt> getStmts() {
		return stmts;
	}

	public SPARQLResultExProcessorBuilder setStmts(Collection<? extends SparqlStmt> stmts) {
		this.stmts = stmts;
		return this;
	}

	public SPARQLResultExProcessor build() {
    	return configureProcessor(
    			out.get(), err.get(),
    			outFormat, stmts == null ? Collections.emptyList(): stmts, prefixMapping,
    			tripleFormat, quadFormat, deferCount,
    			jqMode, jqDepth, jqFlatMode, closeAction);
    }
    
    
	
	
//	public static SPARQLResultExProcessor outputQuads(RDFFormat outFormat) {
//	    SPARQLResultExProcessor resultProcessor = SparqlIntegrateCmdImpls.configureProcessor(
//	            MainCliNamedGraphStream.out, System.err,
//	            cmdFlatMap.outFormat,
//	            stmts,
//	            pm,
//	            RDFFormat.TURTLE_BLOCKS,
//	            RDFFormat.TRIG_BLOCKS,
//	            false, 0, false,
//	            () -> {});
//	}

    public static SPARQLResultExProcessorBuilder create() {
    	SPARQLResultExProcessorBuilder result = new SPARQLResultExProcessorBuilder();
    	result
    		.setOut(() -> StdIo.openStdOutWithCloseShield())
    		.setErr(() -> StdIo.openStdErrWithCloseShield())
    		.setDeferCount(20)
    		.setTripleFormat(RDFFormat.TURTLE_BLOCKS)
    		.setQuadFormat(RDFFormat.TRIG_BLOCKS)
    		.setJqMode(false)
    		.setJqDepth(3)
    		.setJqFlatMode(false);

    	return result;
    }

    public static SPARQLResultExProcessorBuilder createForQuadOutput() {
    	return create().setOutputMode(OutputMode.QUAD);
    }

	
    /**
     *
     * @param outFormat
     * @param stmts
     * @param prefixMapping
     * @param quadFormat Allows to preset a streaming format in case quads were requested
     * @param deferCount
     * @param jqMode
     * @param jqDepth
     * @param jqFlatMode
     * @return
     */
    public static SPARQLResultExProcessor configureProcessor(
           OutputStream out,
           OutputStream err,
           String outFormat,
           Collection<? extends SparqlStmt> stmts,
           PrefixMapping prefixMapping,
           RDFFormat tripleFormat,
           RDFFormat quadFormat,
           long deferCount,
           boolean jqMode, int jqDepth, boolean jqFlatMode,
           Closeable closeAction) {

       OutputFormatSpec spec = OutputFormatSpec.create(outFormat, tripleFormat, quadFormat, stmts, jqMode);

       // RDFLanguagesEx.findRdfFormat(cmd.outFormat, probeFormats)
       List<Var> selectVars = SparqlStmtUtils.getUnionProjectVars(stmts);

       SPARQLResultExProcessorImpl coreProcessor = configureForOutputMode(
               spec.getOutputMode(),
               out,
               err,
               prefixMapping,
               spec.getOutRdfFormat(),
               deferCount,
               spec.getOutLang(),
               selectVars,
               closeAction);


       // TODO The design with SPARQLResultExProcessorForwarding seems a bit overly complex
       // Perhaps allow setting up the jq stuff on SPARQLResultExProcessorImpl directly?
       SPARQLResultExProcessor effectiveProcessor;
       if (jqMode) {
           effectiveProcessor = new SPARQLResultExProcessorForwarding<SPARQLResultExProcessorImpl>(coreProcessor) {
               @Override
               public Void onResultSet(ResultSet it) {
                   while (it.hasNext()) {
                       QuerySolution qs = it.next();
                       JsonElement json = RdfJsonUtils.toJson(qs, jqDepth, jqFlatMode);
                       coreProcessor.getJsonSink().send(json);
                   }
                   return null;
               }
           };
       } else {
           effectiveProcessor = coreProcessor;
       }
       return effectiveProcessor;
   }
   
   

   /**
    * Configure a SPARQLResultExProcessor to delegate
    * JSON, triples/quads and bindings to the appropriate target.
    * 
    * TODO Wrap as a builder
    * @param outputMode
    * @param out
    * @param err
    * @param pm
    * @param outRdfFormat
    * @param deferCount Number of items to analyze for used prefixes before writing them out
    * @param outLang
    * @param resultSetVars
    * @param closeAction
    * @return
    */
   public static SPARQLResultExProcessorImpl configureForOutputMode(
           OutputMode outputMode,
           OutputStream out,
           OutputStream err,
           PrefixMapping pm,
           RDFFormat outRdfFormat,
           long deferCount,
           Lang outLang,
           List<Var> resultSetVars,
           Closeable closeAction
           ) {

       SPARQLResultExProcessorImpl result;

       Supplier<Dataset> datasetSupp = () -> DatasetFactoryEx.createInsertOrderPreservingDataset();
       switch (outputMode) {
       case TRIPLE:
       case QUAD:
           Objects.requireNonNull(outRdfFormat);

           result = new SPARQLResultExProcessorImpl(
                   SinkStreamingQuads.createSinkQuads(outRdfFormat, out, pm, deferCount, datasetSupp),
                   new SinkStreamingJsonArray(err, false),
                   new SinkStreamingAdapter<>(),
                   closeAction) { //new SinkStreamingBinding(err, resultSetVars, ResultSetLang.SPARQLResultSetText)) {
               @Override
               public Void onResultSet(ResultSet rs) {
                   ResultSetMgr.write(err, rs, ResultSetLang.SPARQLResultSetText);
                   return null;
               }
           };

           break;
       case JSON:
           result = new SPARQLResultExProcessorImpl(
                   SinkStreamingQuads.createSinkQuads(RDFFormat.TRIG_BLOCKS, err, pm, 0, datasetSupp),
                   new SinkStreamingJsonArray(out),
                   //new SinkStreamingBinding(err, resultSetVars, ResultSetLang.SPARQLResultSetText));
                   new SinkStreamingAdapter<>(),
                   closeAction) {
               @Override
               public Void onResultSet(ResultSet rs) {
                   ResultSetMgr.write(err, rs, ResultSetLang.SPARQLResultSetText);
                   return null;
               }
           };
           break;
       case BINDING:
           Objects.requireNonNull(outLang);

           result = new SPARQLResultExProcessorImpl(
                   SinkStreamingQuads.createSinkQuads(RDFFormat.TRIG_BLOCKS, err, pm, 0, datasetSupp),
                   new SinkStreamingJsonArray(err, false),
                   new SinkStreamingBinding(out, resultSetVars, outLang),
                   closeAction);
           break;
       default:
           throw new IllegalArgumentException("Unknown output mode: " + outputMode);
       };


       return result;
   }
}
