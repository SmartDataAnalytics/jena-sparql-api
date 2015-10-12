package org.aksw.jena_sparql_api.http;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.atlas.web.HttpException;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

public class QueryExecutionHttpWrapper
    extends QueryExecutionDecorator
{

    public QueryExecutionHttpWrapper(QueryExecution decoratee) {
        super(decoratee);
    }

    @Override
    public ResultSet execSelect() {
        ResultSet result = wrapException(new Supplier<ResultSet>() {
            @Override
            public ResultSet get() {
                ResultSet r = decoratee.execSelect();
                return r;
            }
        });

        return result;
    }


    public static <T> T wrapException(Supplier<T> supplier) {
        T result;
        try {
            result = supplier.get();
        } catch(Exception e) {
            RuntimeException f;
            if(e instanceof HttpException) {
                HttpException x = (HttpException)e;
                f = new HttpException(x.getResponse(), e);
            } else {
                f = new RuntimeException(e);
            }

            throw f;
        }

        return result;
    }

//    public static QueryExecutionHttpWrapper wrap(QueryExecutionF qe) {
//        QueryExecutionHttpWrapper result = new QueryExecutionHttpWrapper(qe);
//        return result;
//    }
}
