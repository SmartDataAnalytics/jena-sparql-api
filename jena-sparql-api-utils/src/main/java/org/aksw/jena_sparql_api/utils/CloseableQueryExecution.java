package org.aksw.jena_sparql_api.utils;

import java.io.Closeable;
import java.io.IOException;

import com.hp.hpl.jena.query.QueryExecution;

public class CloseableQueryExecution
    implements Closeable
{
    private QueryExecution qe;

    public CloseableQueryExecution(QueryExecution qe) {
        this.qe = qe;
    }

        @Override
    public void close() throws IOException {
        qe.close();
    }
}
