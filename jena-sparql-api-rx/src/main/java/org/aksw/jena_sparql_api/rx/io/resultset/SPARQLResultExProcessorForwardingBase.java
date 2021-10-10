package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Iterator;

import org.aksw.jena_sparql_api.stmt.SPARQLResultEx;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;

public abstract class SPARQLResultExProcessorForwardingBase<D extends SPARQLResultExProcessor>
    implements SPARQLResultExProcessor
{
    protected abstract D getDelegate();

    @Override
    public void start() {
        getDelegate().start();
    }

    @Override
    public void finish() {
        getDelegate().finish();
    }

    @Override
    public void send(SPARQLResultEx item) {
        getDelegate().send(item);
    }

    @Override
    public void flush() {
        getDelegate().flush();
    }

    @Override
    public void close() {
        getDelegate().close();
    }

    @Override
    public Void onQuads(Iterator<Quad> it) {
        return getDelegate().onQuads(it);
    }

    @Override
    public Void onTriples(Iterator<Triple> it) {
        return getDelegate().onTriples(it);
    }

    @Override
    public Void onBooleanResult(Boolean value) {
        return getDelegate().onBooleanResult(value);
    }

    @Override
    public Void onResultSet(ResultSet rs) {
        return getDelegate().onResultSet(rs);
    }

    @Override
    public Void onJsonItems(Iterator<JsonObject> it) {
        return getDelegate().onJsonItems(it);
    }
}
