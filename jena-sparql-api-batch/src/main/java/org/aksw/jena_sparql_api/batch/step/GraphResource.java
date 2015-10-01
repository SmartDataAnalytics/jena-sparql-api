package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.batch.cli.main.SupplierExtendedIteratorTriples;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatchFilter;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

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
        Filter<Triple> filter = new TripleMatchFilter(triplePattern);

        ExtendedIterator<Triple> it = supplier.get();
        ExtendedIterator<Triple> result = it.filterKeep(filter);
        return result;
    }
}
