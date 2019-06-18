package org.aksw.jena_sparql_api.stmt;

public interface SPARQLResultSink
	extends SPARQLResultVisitor, AutoCloseable
{
    void flush() ;
}
