package org.aksw.jena_sparql_api.rx.io.resultset;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

import com.github.jsonldjava.shaded.com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * This class bundles streaming sinks for the different result types of the SPARQL query forms. 
 * 
 * 
 * @author raven
 *
 */
public class SPARQLResultExProcessorImpl
    extends SinkStreamingBase<SPARQLResultEx>
    implements SPARQLResultExProcessor
{
    protected Gson gson = new Gson();

    /**
     * The sink for quad may output immediately or write to a dataset first
     * Sinks may be preconfigured with prefixes an wrap StreamRDF
     */
    protected SinkStreaming<Quad> quadSink;
    protected SinkStreaming<JsonElement> jsonSink;
    protected SinkStreaming<Binding> bindingSink;
    protected Closeable closeAction;

    public SPARQLResultExProcessorImpl(
            SinkStreaming<Quad> quadSink,
            SinkStreaming<JsonElement> jsonSink,
            SinkStreaming<Binding> bindingSink,
            Closeable closeAction) {
        super();
        this.quadSink = quadSink;
        this.jsonSink = jsonSink;
        this.bindingSink = bindingSink;
        this.closeAction = closeAction;
    }


    public Sink<Quad> getQuadSink() {
        return quadSink;
    }

    public SinkStreaming<Binding> getBindingSink() {
        return bindingSink;
    }

    public Sink<JsonElement> getJsonSink() {
        return jsonSink;
    }


    @Override
    public Void onBooleanResult(Boolean value) {
        throw new UnsupportedOperationException("Boolean results not supported");
    }


    @Override
    public Void onResultSet(ResultSet rs) {
        while (rs.hasNext()) {
            Binding binding = rs.nextBinding();
            bindingSink.send(binding);
        }

        return null;
    }


    @Override
    public Void onJsonItems(Iterator<JsonObject> it) {
        while (it.hasNext()) {
            JsonObject json = it.next();
            String jsonStr = json.toString();
            JsonElement el = gson.fromJson(jsonStr, JsonElement.class);

            jsonSink.send(el);
        }

        return null;
    }


    @Override
    public Void onQuads(Iterator<Quad> it) {
        while (it.hasNext()) {
            Quad quad = it.next();
            quadSink.send(quad);
        }

        return null;
    }


    @Override
    public Void onTriples(Iterator<Triple> it) {
        return onQuads(Iterators.transform(it, t -> new Quad(Quad.defaultGraphIRI, t)));
    }


    @Override
    protected void startActual() {
        quadSink.start();
        bindingSink.start();
        jsonSink.start();
    }

    @Override
    protected void finishActual() {
        jsonSink.finish();
        bindingSink.finish();
        quadSink.finish();
    }

    @Override
    protected void sendActual(SPARQLResultEx item) {
        forward(item);
    }

    @Override
    public void flush() {
        quadSink.flush();
        bindingSink.flush();
        jsonSink.flush();
    }


    @Override
    public void close() {
        quadSink.close();
        bindingSink.close();
        jsonSink.close();
        if (closeAction != null) {
            try {
                closeAction.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}