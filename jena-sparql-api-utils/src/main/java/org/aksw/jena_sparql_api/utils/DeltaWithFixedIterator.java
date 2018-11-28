package org.aksw.jena_sparql_api.utils;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.graph.impl.SimpleEventManager;
import org.apache.jena.mem.TrackingTripleIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * Delta implementation that 'fixes' removals on iterators returned by .find().
 * Issue affects at least Jena 3.8.0 and likely earlier versions
 * 
 * Calling .remove() on the returned iterator does not register a deletion record in Delta;
 * instead it removes triples from the base model - which arguably defeats Delta's purpose
 * by breaking its encapsulation.
 * 
 * 
 * 
 */
public class DeltaWithFixedIterator
	extends Delta
{
	public DeltaWithFixedIterator(Graph base) {
		super(base);
	}
	
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple t)
    {
    	ExtendedIterator<Triple> base = super.graphBaseFind(t);
    	
    	ExtendedIterator<Triple> fixed = WrappedIterator.create(new IteratorClosable<Triple>(new TrackingTripleIterator(base) {
    		public void remove() {
    			performDelete(current);
    		};
    	}, () -> base.close()));

        return SimpleEventManager.notifyingRemove(this, fixed);
    }
}