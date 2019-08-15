package org.aksw.jena_sparql_api.stmt;

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;

public class SPARQLResultSinkQuads
	implements SPARQLResultSink {
	
	//protected Consumer<Quad> consumer;
	protected Sink<Quad> sink;
	protected Gson gson;

	public SPARQLResultSinkQuads(Sink<Quad> sink) {
		this(sink, null);
	}

	public SPARQLResultSinkQuads(Consumer<Quad> sink) {
		this(new Sink<Quad>() {
			@Override public void send(Quad item) { sink.accept(item);}
			@Override public void close() { }
			@Override public void flush() { }
		}, null);
	}

	public SPARQLResultSinkQuads(Sink<Quad> sink, Gson gson) {
		super();
		this.sink = sink;
		this.gson = gson != null ? gson : new Gson();
	}

	@Override
	public void onQuad(Quad value) {
		sink.send(value);
	}

	@Override
	public void onResultSet(ResultSet rs) {
		String str = ResultSetFormatter.asText(rs);
		System.err.println(str);
	}
	
	@Override
	public void onJson(com.google.gson.JsonElement value) {
		String json = gson.toJson(value);
		System.err.println(json);
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