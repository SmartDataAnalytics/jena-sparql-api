package org.aksw.jena_sparql_api.stmt;

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;

public class SPARQLResultSinkQuads
    implements SPARQLResultSink {

    //protected Consumer<Quad> consumer;
    protected Sink<Quad> sink;
    protected SPARQLResultSink fallback;

    public SPARQLResultSinkQuads(Consumer<Quad> sink) {
        this(sink, null);
    }

    public SPARQLResultSinkQuads(Consumer<Quad> sink, SPARQLResultSink fallback) {
        this(new Sink<Quad>() {
            @Override public void send(Quad item) { sink.accept(item);}
            @Override public void close() { }
            @Override public void flush() { }
        }, fallback);
    }

//	public SPARQLResultSinkQuads(Sink<Quad> sink, SPARQLResultSink fallback) {
//		this(sink, fallback, null);
//	}

    public SPARQLResultSinkQuads(Sink<Quad> sink) {
        this(sink, null);
    }

    public SPARQLResultSinkQuads(Sink<Quad> sink, SPARQLResultSink fallback) {
        super();
        this.sink = sink;
        this.fallback = fallback != null ? fallback : new SPARQLResultSinkPrint(System.err);
    }

    @Override
    public void onQuad(Quad value) {
        sink.send(value);
    }

    @Override
    public void onResultSet(ResultSet rs) {
        fallback.onResultSet(rs);
//		String str = ResultSetFormatter.asText(rs);
//		System.err.println(str);
    }

    @Override
    public void onJson(com.google.gson.JsonElement value) {
        fallback.onJson(value);
//		String json = gson.toJson(value);
//		System.err.println(json);
    }

    @Override
    public void close() throws Exception {
        sink.close();
    }

    @Override
    public void flush() {
        sink.flush();
    }
}