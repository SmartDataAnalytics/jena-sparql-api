package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.concurrent.Callable;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.util.ResultSetUtils;


/**
 * This callable is the active component in traversing the iterator.
 *
 *
 *
 * @author raven
 *
 */
public class CallableResultsetConsumer
    implements Callable<Long>
{
    protected long currentRow = 0;
    protected ResultSet resultSet;
    protected Long maxResultRow;
    protected AutoCloseable closeAction;


    @Override
    public Long call() throws Exception {
        for(; resultSet.hasNext() && (maxResultRow == null || currentRow < maxResultRow) && !Thread.interrupted(); ++currentRow) {
            resultSet.next();
            //ResultSet
            //ResultSetFactory.create


        }

        return currentRow;
    }

}
