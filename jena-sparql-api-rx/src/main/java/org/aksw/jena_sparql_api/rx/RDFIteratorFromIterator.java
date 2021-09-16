package org.aksw.jena_sparql_api.rx;

import org.apache.jena.atlas.iterator.IteratorOnClose;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

public class RDFIteratorFromIterator<T>
    implements RDFIterator<T>
{
    protected IteratorOnClose<T> delegate;
    protected String baseIri;
    protected PrefixMap prefixMap;

    public RDFIteratorFromIterator(IteratorOnClose<T> delegate, String baseIri) {
        this(delegate, baseIri, PrefixMapFactory.create());
    }

    public RDFIteratorFromIterator(IteratorOnClose<T> delegate, String baseIri, PrefixMap prefixMap) {
        super();
        this.delegate = delegate;
        this.baseIri = baseIri;
        this.prefixMap = prefixMap;
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
        return prefixMap;
    }

    @Override
    public boolean prefixesChanged() {
        return false;
    }

    @Override
    public String getBaseIri() {
        return baseIri;
    }
}
