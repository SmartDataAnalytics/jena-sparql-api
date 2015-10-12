package org.aksw.jena_sparql_api.http;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.apache.jena.atlas.web.HttpException;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;

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
    @Override
    public Iterator<Triple> execConstructTriples() {
        Iterator<Triple> result = wrapException(new Supplier<Iterator<Triple>>() {
            @Override
            public Iterator<Triple> get() {
                Iterator<Triple> r = decoratee.execConstructTriples();
                return r;
            }
        });

        return result;
    }


    @SuppressWarnings("unchecked")
    public static <T> T wrapException(Supplier<T> supplier) {
        T result;
        try {
            result = supplier.get();
        } catch(Exception e) {
            RuntimeException f;
            if(e instanceof ResultSetException) {
                ResultSetException x = (ResultSetException)e;
                String msg = x.getMessage();
                if(msg.contains("One or more of the required keys")) {
                    result = (T)ResultSetFactory.create(QueryIterNullIterator.create(null), Collections.<String>emptyList());
                    return result;
                } else {
                    f = new RuntimeException(e);
                }

            } else if(e instanceof HttpException) {
                HttpException x = (HttpException)e;
                f = new HttpException(x.getResponse(), e);
            } else if(e instanceof  QueryExceptionHTTP) {
                QueryExceptionHTTP x = (QueryExceptionHTTP)e;
                f = new QueryExceptionHTTP(x.getResponseMessage() + " ", e);
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
