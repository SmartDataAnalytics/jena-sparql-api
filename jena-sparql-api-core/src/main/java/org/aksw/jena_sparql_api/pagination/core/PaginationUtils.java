package org.aksw.jena_sparql_api.pagination.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.CannedQueryUtils;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;



/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/1/11
 *         Time: 12:36 AM
 */
public class PaginationUtils {
    public static long adjustPageSize(QueryExecutionFactory factory, long requestedPageSize) {
        Query query = CannedQueryUtils.spoTemplate();
        query.setLimit(requestedPageSize);

        //System.out.println(query);
        QueryExecution qe = factory.createQueryExecution(query);
        ResultSet rs = qe.execSelect();

        long size = 0;
        while(rs.hasNext()) {
            ++size;
            rs.next();
        }
        size = Math.max(size, 1);
        qe.close();

        return size >= requestedPageSize
            ? requestedPageSize
            : size;
    }
}
