package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.pagination.extra.PaginationQueryIterator;

import com.hp.hpl.jena.query.Query;

public class PagingQuery {
    private Query proto;
    private Integer pageSize;

    public PagingQuery(Integer pageSize, Query proto) {
        this.proto = proto;
        this.pageSize = pageSize;
    }
    
    public PaginationQueryIterator createQueryIterator(long itemIndex) {
        
        long baseOffset = proto.getOffset() == Query.NOLIMIT ? 0 : proto.getOffset();

        long itemOffset = baseOffset + itemIndex;
        
        long limit = proto.getLimit() == Query.NOLIMIT ? Query.NOLIMIT : proto.getLimit() - itemIndex;
        
        Query clone = proto.cloneQuery();
        clone.setOffset(itemOffset);
        clone.setLimit(limit);
        
        PaginationQueryIterator result = new PaginationQueryIterator(clone, pageSize);
        return result;
    }
}