package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.Iterator;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.ResultSet;

public abstract class SPARQLResultVisitorFowarding<T>
    implements SPARQLResultVisitor<T>
{
    protected abstract SPARQLResultVisitor<T> getDelegate();

    @Override
    public T onBooleanResult(Boolean value) {
        return getDelegate().onBooleanResult(value);
    }

    @Override
    public T onResultSet(ResultSet it) {
        return getDelegate().onResultSet(it);
    }

    @Override
    public T onJsonItems(Iterator<JsonObject> it) {
        return getDelegate().onJsonItems(it);
    }

}