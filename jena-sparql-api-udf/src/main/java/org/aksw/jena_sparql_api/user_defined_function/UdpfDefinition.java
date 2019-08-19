package org.aksw.jena_sparql_api.user_defined_function;

/**
 * NOT USED; it turned out we only needed user defined functions so far
 * 
 * Mapping of a user defined function to a user defined property function.
 * This allows for rewriting e.g.
 * FILTER(:f(?x) = ?z) is actually mapped to { ?x :f ?y FILTER(?y = ?z) }
 * 
 * Arbitrary bindings of function parameters to the property function subject / object param lists
 * are not yet supported.
 * 
 * In principle, each target position can be defined as an expression over the input parameters
 *   // Name the positions of the param list
 * :myUdfThatIsMappedToAUdpf
 *   :inputParamAliases ("x" "y" "z") // we may introduce "result" as a special name for the function result
 *   // Bind the target expr lists to expressions over the input params, such as
 *   :subjectMapping ( "?x + 1" "result")
 *   :objectMapping ( "?z - 1" "?y * ?y")
 *  
 * 
 * @author raven
 *
 */
public interface UdpfDefinition {
//
//	/**
//	 * Allow the use of the property function in expressions
//	 * 
//	 * 
//	 * @return
//	 */
//	@Iri("http://ns.aksw.org/jena/udf/allowUseAsFunction")
//	boolean allowUseAsFunction();

}
