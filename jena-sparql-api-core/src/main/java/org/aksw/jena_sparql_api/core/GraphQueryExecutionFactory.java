package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.Query;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphQueryExecutionFactory
    extends GraphBase
{
    private QueryExecutionFactory qef;

    // Whether to delegate a call to close to the underlying qef
    // True by default
    boolean delegateClose;

    public GraphQueryExecutionFactory(QueryExecutionFactory qef) {
        this(qef, true);
    }

    public GraphQueryExecutionFactory(QueryExecutionFactory qef, boolean delegateClose) {
        this.qef = qef;
        this.delegateClose = delegateClose;
    }


//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
//
//    }

    @Override
    public void close() {
        if(delegateClose) {
            try {
                this.qef.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        Query query = QueryGenerationUtils.createQueryTriple(m);
        ExtendedIterator<Triple> result = QueryExecutionUtils.execConstruct(qef, query);

        return result;
    }

}
