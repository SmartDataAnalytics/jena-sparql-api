package org.aksw.jena_sparql_api.core;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.Query;
import org.apache.jena.util.iterator.ExtendedIterator;

@Deprecated // Use GraphFromRDFConnection
public class GraphSparqlService
    extends GraphBase
{
    private SparqlService sparqlService;

    // Whether to delegate a call to close to the underlying qef
    // True by default
    boolean delegateClose;

    public GraphSparqlService(SparqlService sparqlService) {
        this(sparqlService, true);
    }

    public GraphSparqlService(SparqlService sparqlService, boolean delegateClose) {
        this.sparqlService = sparqlService;
        this.delegateClose = delegateClose;
    }


//    @Override
//    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
//
//    }

    @Override
    public void close() {
        if(delegateClose) {
            //this.sparqlService.close();
        }
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        Query query = QueryGenerationUtils.createQueryTriple(m);
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        ExtendedIterator<Triple> result = QueryExecutionUtils.execConstruct(qef, query);
        return result;
    }

}
