package org.aksw.jena_sparql_api.pagination.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 8:00 PM
 */

import org.aksw.commons.collections.SinglePrefetchIterator;

import org.apache.jena.query.Query;

/**
 *
 *
 * @author raven
 *
 */
public class PaginationQueryIterator
    extends SinglePrefetchIterator<Query>
{
    private long pageSize;
    private QueryPaginator queryPaginator;

    /**
     * Note: The query object's limit and offest will be modified.
     * Use Query.cloneQuery in order to create a copy.
     *
     * @param query
     * @param pageSize
     */
    public PaginationQueryIterator(Query query, long pageSize)
    {
        this.queryPaginator = new QueryPaginator(query);
        this.pageSize = pageSize;
    }


    /**
     * Returns the next query or null
     *
     * @return
     * @throws Exception
     */
    public Query prefetch()
    {
        Query result = queryPaginator.nextPage(pageSize);
        if(result == null) {
            return finish();
        }

        return result;
    }

}
