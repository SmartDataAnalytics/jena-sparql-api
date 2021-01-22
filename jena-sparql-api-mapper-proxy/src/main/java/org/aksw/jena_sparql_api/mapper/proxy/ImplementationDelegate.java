package org.aksw.jena_sparql_api.mapper.proxy;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

/**
 * Abstract base implementation of {@link Implementation}
 * that delegates calls.
 * 
 * @author raven
 *
 */
public abstract class ImplementationDelegate
    extends Implementation
{
    protected abstract Implementation getDelegate();

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        Implementation delegate = getDelegate();
        EnhNode result = delegate.wrap(node, eg);
        return result;
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        Implementation delegate = getDelegate();
        boolean result = delegate.canWrap(node, eg);
        return result;
    }
}
