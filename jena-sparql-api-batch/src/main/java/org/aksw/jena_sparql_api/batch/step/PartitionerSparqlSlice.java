package org.aksw.jena_sparql_api.batch.step;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import com.hp.hpl.jena.query.Query;

public class PartitionerSparqlSlice
    implements Partitioner
{
    protected QueryExecutionFactory qef;
    protected Query query;

    public PartitionerSparqlSlice(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = 0;
        long max = QueryExecutionUtils.countQuery(query, qef);

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
