package org.aksw.jena_sparql_api.rx;

import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.system.PrefixMap;

public class RDFIteratorFromPipedRDFIterator<T>
    implements RDFIterator<T>
{
    protected PipedRDFIterator<T> delegate;

    public RDFIteratorFromPipedRDFIterator(PipedRDFIterator<T> delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public T next() {
        return delegate.next();
    }

    @Override
    public PrefixMap getPrefixes() {
        return delegate.getPrefixes();
    }

    @Override
    public String getBaseIri() {
        return delegate.getBaseIri();
    }

    @Override
    public boolean prefixesChanged() {
        return false;
    }
}
