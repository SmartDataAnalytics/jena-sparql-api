package org.aksw.jena_sparql_api.core.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.util.iterator.ExtendedIterator;


public class GraphResource
    extends GraphBase
{
    protected Supplier<? extends ExtendedIterator<Triple>> supplier;

    public GraphResource(String fileNameOrUrl) {
        this(new SupplierExtendedIteratorTriples(fileNameOrUrl));
    }

    public GraphResource(Supplier<? extends ExtendedIterator<Triple>> supplier) {
        this.supplier = supplier;
    }


    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        Predicate<Triple> filter = new TripleMatchFilter(triplePattern);

        ExtendedIterator<Triple> it = supplier.get();
        ExtendedIterator<Triple> result = it.filterKeep(filter);
        return result;
    }
}
