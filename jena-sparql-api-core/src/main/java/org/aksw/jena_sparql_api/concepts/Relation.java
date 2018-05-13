package org.aksw.jena_sparql_api.concepts;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * Base interface for SPARQL relations
 * @author raven Mar 7, 2018
 *
 */
public interface Relation {
	List<Var> getVars();
	Element getElement();
}
