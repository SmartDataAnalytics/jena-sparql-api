package org.aksw.jena_sparql_api.pagination.core;

import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 11:41 PM
 */
public class QueryExecutionFactoryIterated
    extends QueryExecutionFactoryBackQuery
{
    private QueryExecutionFactory decoratee;
    private QueryTransformer queryTransformer;
    private boolean breakOnEmptyResult;

    public QueryExecutionFactoryIterated(QueryExecutionFactory decoratee, QueryTransformer queryTransformer, boolean breakOnEmptyResult) {
        this.decoratee = decoratee;
        this.queryTransformer = queryTransformer;
        this.breakOnEmptyResult = breakOnEmptyResult;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Iterator<Query> queryIterator = queryTransformer.transform(query);

        return new QueryExecutionIterated(query, decoratee, queryIterator, breakOnEmptyResult);
    }


    @Override
    public String getId() {
        return decoratee.getId();
    }

    @Override
    public String getState() {
        return decoratee.getState();
    }

    public static void main(String[] args) {
        QueryExecutionFactory factory = new QueryExecutionFactoryHttp("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");
        QueryExecutionFactoryPaginated fp = new QueryExecutionFactoryPaginated(factory, 10000);

        System.out.println(fp.getPageSize());

        /*
        QueryExecution qe = fp.createQueryExecution(CannedQueryUtils.spoTemplate());

        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.next());
        }

        qe.close();
        */
    }
}
