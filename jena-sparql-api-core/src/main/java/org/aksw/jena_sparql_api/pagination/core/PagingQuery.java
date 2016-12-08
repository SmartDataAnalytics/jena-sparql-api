package org.aksw.jena_sparql_api.pagination.core;

import org.aksw.jena_sparql_api.pagination.extra.PaginationQueryIterator;

import org.apache.jena.query.Query;

public class PagingQuery {
    private Query proto;
    private Integer pageSize;

    public PagingQuery(Integer pageSize, Query proto) {
        this.proto = proto;
        this.pageSize = pageSize;
    }

    public PaginationQueryIterator createQueryIterator(Long offset, Long limit) {

        long o = offset == null ? 0 : offset;
        long l = limit == null ? Long.MAX_VALUE : limit;

        long queryOffset = proto.getOffset() == Query.NOLIMIT ? 0 : proto.getOffset();

        long itemOffset = queryOffset + o;

        long queryLimit = proto.getLimit() == Query.NOLIMIT ? Long.MAX_VALUE : proto.getLimit() - o;

        long itemLimit = Math.min(queryLimit, l);
        itemLimit = itemLimit == Long.MAX_VALUE ? Query.NOLIMIT : itemLimit;

        Query clone = proto.cloneQuery();
        clone.setOffset(itemOffset);
        clone.setLimit(itemLimit);

        PaginationQueryIterator result = new PaginationQueryIterator(clone, pageSize);
        return result;
    }
}