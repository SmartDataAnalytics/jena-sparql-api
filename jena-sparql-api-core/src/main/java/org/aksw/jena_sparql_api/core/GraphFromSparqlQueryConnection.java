package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.QueryGenerationUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphFromSparqlQueryConnection
    extends GraphBase
{
    private SparqlQueryConnection conn;

    // Whether to delegate a call to close to the underlying qef
    // True by default
    boolean delegateClose;

    public GraphFromSparqlQueryConnection(SparqlQueryConnection conn) {
        this(conn, true);
    }

    public GraphFromSparqlQueryConnection(SparqlQueryConnection conn, boolean delegateClose) {
        this.conn = conn;
        this.delegateClose = delegateClose;
    }

    @Override
    public void close() {
        if(delegateClose) {
            conn.close();
        }
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        System.err.println("Lookup with " + m);
        Query query = QueryGenerationUtils.createQueryTriple(m);
        QueryExecution qe = conn.query(query);
        ExtendedIterator<Triple> result = QueryExecutionUtils.execConstruct(qe, query);
        return result;
    }

}
