package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Wraps a result set as an iterator of bindings
 *
 * @author raven
 *
 */
public class IteratorResultSetBinding
    implements Iterator<Binding>
{
    private ResultSet rs;

    public IteratorResultSetBinding(ResultSet rs) {
        this.rs = rs;
    }

    @Override
    public boolean hasNext() {
        boolean result = rs.hasNext();
        return result;
    }

    @Override
    public Binding next() {
        Binding result = rs.nextBinding();
        return result;
    }

    @Override
    public void remove() {
        throw new RuntimeException("Operation not supported");
    }
}