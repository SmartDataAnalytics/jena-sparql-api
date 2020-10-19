package org.aksw.jena_sparql_api.concepts;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementSubQuery;

/**
 * Interface with several default methods for working which an object that holds
 * an {@link Element}. In particular, this interface simplifies extraction
 * of the Query or Table when the held Element is of type
 * {@link ElementSubQuery} or {@link ElementData}, respectively.
 *
 * @author raven
 *
 */
public interface HasElement {

    /**
     * Return the element held by the implementation of this interface
     *
     * @return
     */
    Element getElement();


    default boolean holdsQuery() {
        Element elt = getElement();
        boolean result = elt instanceof ElementSubQuery;
        return result;
    }

    default Query extractQuery() {
        Element elt = getElement();
        ElementSubQuery tmp = (ElementSubQuery)elt;
        Query result = tmp.getQuery();
        return result;
    }

    default boolean holdsTable() {
        Element elt = getElement();
        boolean result = elt instanceof ElementData;
        return result;
    }

    default Table extractTable() {
        Element elt = getElement();
        ElementData tmp = (ElementData)elt;
        Table result = tmp.getTable();
        return result;
    }

    default Op toOp() {
        Element elt = getElement();
        Op result = Algebra.compile(elt);
        return result;
    }
}
