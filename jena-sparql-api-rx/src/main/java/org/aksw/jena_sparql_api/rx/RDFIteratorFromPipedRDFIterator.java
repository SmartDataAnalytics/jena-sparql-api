package org.aksw.jena_sparql_api.rx;

import org.apache.jena.riot.lang.PipedRDFIterator;

public class RDFIteratorFromPipedRDFIterator<T>
    extends PipedRDFIterator<T>
    implements RDFIterator<T>
{
    /** A dirty flag for prefixes */
    protected volatile boolean prefixesChanged = false;

    public RDFIteratorFromPipedRDFIterator(int bufferSize, boolean fair, int pollTimeout, int maxPolls) {
        super(bufferSize, fair, pollTimeout, maxPolls);
    }

    @Override
    protected void prefix(String prefix, String iri) {
        super.prefix(prefix, iri);
        prefixesChanged = true;
    }

    /**
     * Returns the status of the dirty flag for prefixes and resets it to false.
     * Hence, should this method return true at a future invocation there has been a change since
     * the last invocation
     *
     */
    @Override
    public boolean prefixesChanged() {
        boolean result = prefixesChanged;
        prefixesChanged = false;
        return result;
    }
}
