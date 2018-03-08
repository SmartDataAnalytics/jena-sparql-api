package org.aksw.jena_sparql_api.concepts;

/**
 * A triple-based virtual predicate.
 * 
 * Given a triple pattern t
 * a union based on t will be created that yields in addition to t
 * triples of the given binary relation if the condition is met:
 * 
 * Given e.g. a triple pattern ?x ?y ?z
 * and a virtual predicate to yield rdfs:label for the set of resources
 * for which a dc:title exists:
 * 
 * condition: ?foo dc:title ?bar
 * predicate: ?baz rdfs:label ?baa
 * 
 * 
 * 
 * 
 *   {?x ?y ?z }
 * UNION {
 *   ?x ?y ?_z .
 *   ?x dc:title ?bar .
 *   ?x rdfs:label ?z 
 * }
 * 
 * 
 * condition ?s a Person
 * predicate age: ?s ?a { ?s birthDate ?d . BIND(NOW() - ?d AS ?a }
 * 
 *   { { ?x ?y z  }
 *   UNION
 *     {
 *       ?x a Person . # condition / join on the subject position
 *
 *       ?x birthDate ?d
 *       BIND(NOW() - ?d AS ?y)
 *     }
 *  }
 * 
 * 
 * So if there is a ?s ?p ?o pattern, we take ?s ?p ?o - and add any other binary relations for which
 * the condition is satisfied to a union
 * 
 * 
 * 
 *  
 * The condition can be read as:
 * For all triples belonging to the set of resources matching the condition,
 * apply the binary relation
 * 
 * 
 * 
 * 
 * @author raven Mar 7, 2018
 *
 */
public class VirtualPredicate {
	protected TernaryRelation condition;
	protected BinaryRelation predicate;
}
