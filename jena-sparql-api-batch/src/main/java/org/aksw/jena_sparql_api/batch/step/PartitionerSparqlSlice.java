package org.aksw.jena_sparql_api.batch.step;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import org.apache.jena.query.Query;

public class PartitionerSparqlSlice
    implements Partitioner
{
    protected QueryExecutionFactory qef;
    protected Query query;

    // If we know that the slave steps use a certain page size, we
    // can use this information to adjust the grid size.
    // It does not make sense for the grid size to be larger than the amount of pages there are to process.
    // gridSize = Math.min(requestedGridSize, numItemsToProcess / numItemsPerPage)
    protected Long pageSize;


    public PartitionerSparqlSlice() {
        super();
    }

    public PartitionerSparqlSlice(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    public QueryExecutionFactory getSource() {
        return qef;
    }

    public void setSource(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public Map<String, ExecutionContext> partition(int requestedGridSize) {

        long min = 0;
        long max = QueryExecutionUtils.countQuery(query, qef);

        // last part is equivalent to (long)Math.ceil(max / pageSize)
        long gridSize = pageSize == null ? requestedGridSize : Math.min(requestedGridSize, (max + pageSize - 1) / pageSize);

        long targetSize = (max - min) / gridSize + 1;

        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();

        while (start <= max) {

            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);
            if (end >= max) {
                end = max;
            }
            value.putLong("minValue", start);
            value.putLong("maxValue", end);
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }


}
