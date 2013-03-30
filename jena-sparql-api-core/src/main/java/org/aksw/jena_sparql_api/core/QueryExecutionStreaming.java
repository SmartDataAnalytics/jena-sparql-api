package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;

import java.util.Iterator;

/**
 * A class that is essentially the same as Jena's Query Execution
 * except that it adds two methods for streaming triples.
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/5/11
 *         Time: 2:47 PM
 */
public interface QueryExecutionStreaming
    extends QueryExecution
{
    Iterator<Triple> execConstructStreaming();
    Iterator<Triple> execDescribeStreaming();
}
