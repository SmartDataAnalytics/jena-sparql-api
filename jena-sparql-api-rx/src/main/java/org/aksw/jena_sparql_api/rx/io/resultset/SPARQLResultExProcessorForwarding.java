package org.aksw.jena_sparql_api.rx.io.resultset;

public class SPARQLResultExProcessorForwarding<D extends SPARQLResultExProcessor>
    extends SPARQLResultExProcessorForwardingBase<D>
{
    protected D delegate;

    public SPARQLResultExProcessorForwarding(D delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    protected D getDelegate() {
        return delegate;
    }
}
