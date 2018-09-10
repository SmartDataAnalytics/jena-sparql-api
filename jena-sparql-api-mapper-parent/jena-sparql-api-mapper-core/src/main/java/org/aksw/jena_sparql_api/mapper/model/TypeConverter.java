package org.aksw.jena_sparql_api.mapper.model;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public interface TypeConverter {

    String getDatatypeURI();
    Class<?> getJavaClass();

    /**
     * Convert an expression in such a way that the resulting expression has a simple mapping to a Java type.
     * E.g. xsd:gYear -> Integer: (e) -> new E_DateTimeYear(e)
     *
     * @param expr
     * @return
     */
    Expr toJava(Expr expr);


    /**
     * Convert a Java object, assumed to be an instance of (a subclass of) javaClass, to a node having a datatype
     * assignable to datatypeURI
     *
     *
     * @param o
     * @return
     */
    Node toRdf(Object o);
}
