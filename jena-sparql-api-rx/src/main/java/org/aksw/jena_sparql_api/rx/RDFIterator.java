package org.aksw.jena_sparql_api.rx;


import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.util.iterator.ClosableIterator;

public interface RDFIterator<T>
    extends ClosableIterator<T>, Closeable, java.io.Closeable, AutoCloseable
{
    /**
     * Return the prefixes encountered so far.
     * If the iterator is backed by a read-ahead parsing process prefix changes
     *
     * @return
     */
    PrefixMap getPrefixes();

    /**
     * Returns true if the most recent call to next()/hasNext() caused a change
     * in the prefixes
     *
     * @return
     */
    boolean prefixesChanged();

    String getBaseIri();
}
