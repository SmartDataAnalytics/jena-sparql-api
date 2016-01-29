package org.aksw.jena_sparql_api.pagination.extra;

import org.apache.jena.query.Query;

/**
 * Paginates a query with variable page size
 *
 * @author raven
 *
 */
public class QueryPaginator {
    private long nextOffset;
    private Long nextRemaining;

    private Query query;

    /**
     * Note: The query object's limit and offest will be modified.
     * Use Query.cloneQuery in order to create a copy.
     *
     * @param query
     * @param pageSize
     */
    public QueryPaginator(Query query) {
        this(query, null);
    }

    public QueryPaginator(Query query, Long nextOffset)
    {
        this.query = query;

        this.nextOffset = query.getOffset() == Query.NOLIMIT ? 0 : query.getOffset();
        this.nextRemaining = query.getLimit() == Query.NOLIMIT ? null : query.getLimit();
    }


    /**
     * Returns the next query or null
     *
     * @return
     * @throws Exception
     */
    public Query nextPage(long pageSize)
    {
        if(nextOffset == 0) {
            query.setOffset(Query.NOLIMIT);
        } else {
            query.setOffset(nextOffset);
        }

        if(nextRemaining == null) {
            query.setLimit(pageSize);
            nextOffset += pageSize;
        } else {
            long limit = Math.min(pageSize, nextRemaining);
            nextOffset += limit;
            nextRemaining -= limit;

            if(limit == 0) {
                return null;
            }

            query.setLimit(limit);
        }

        return query;
    }
}
