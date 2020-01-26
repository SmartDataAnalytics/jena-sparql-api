package org.aksw.jena_sparql_api.json;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

/**
 * TODO If we were to make use of GraphVar we would have to move this class out of
 * jena-jgrapht-bindings
 * 
 * Not a working class, just a bunch of notes and code fragements
 * what may become a sparql based json ld framer
 * 
 * (?o) = Create a list with all values of ?o; implied distinct
 * (?o ?i) = Create a list, with ?i used as the index
 * 
 * 
 * CONSTRUCT {
 *   ?s ?p (?v "MIN(?_)" "!isIRI(?_)") # The minimum value for ?v
 * }
 * 
 * ?s ?p ?v -> sample ?v
 * 
 * 
 * CONSTRUCT {
 *   ?s ?p ("list" (?o) "<") # A list of values; with comparator "<"
 *   ?s ?p ((?o) ?i "<") # A list of values; based on index ?i, ?i values compared by "<"
 *   
 *   ?s ?p ?x
 *   
 *   ?x <foo> <bar>
 * }

 * 
 * @author raven
 *
 */
public class SparqlBasedJsonLdFramer {
	// FIXME We should not hijack the XSD namespace for that
	// JsonRoot is not even a Datatype unlike most if not all elements of the xsd namespace
	public static final Node JSON_ROOT = NodeFactory.createURI(XSD.NS + "JsonRoot");
	
	public Set<Node> findRoots(Collection<Triple> triples) {
		Set<Node> subjects = triples.stream().map(Triple::getSubject).collect(Collectors.toSet());
		Set<Node> objects = triples.stream().map(Triple::getObject).collect(Collectors.toSet());

		Set<Node> result = Sets.difference(subjects, objects);
		return result;
	}
	
	public Set<Node> findDesignatedRoots(Collection<Triple> triples) {
		Set<Node> result = new LinkedHashSet<>();
		for(Triple triple : triples) {
			if(RDF.type.asNode().equals(triple.getPredicate()) && JSON_ROOT.equals(triple.getObject())) {
				result.add(triple.getSubject());
			}
		}
		
		return result;
	}

	public Set<Node> findRoots(Stream<Triple> triples) {
		Set<Node> subjects = triples.map(Triple::getSubject).collect(Collectors.toSet());
		Set<Node> objects = triples.map(Triple::getObject).collect(Collectors.toSet());

		Set<Node> result = Sets.difference(subjects, objects);
		return result;
	}

//	public Agg<JsonElement> createAggregator(Template template) {
//		Graph g = new GraphVarImpl();
//		for(Triple t : template.getTriples()) {
//			g.add(t);
//		}
//		Model m = ModelFactory.createModelForGraph(g);
//
//		
//		// Find designated Json roots -
//		//   if there is none, use the subjects that do not ocurr as objects
//		//     if there are none, use the first subject
//		
//
//		Set<Node> roots = null;
//		
//		for(Node root : roots) {
//			
//		}
//		return null;
//	}
//	
//	
//	public Agg<Object> createAgg(RDFNode root, Set<Node> seen) {
//		Agg<Object> result;
//		
//		Node rootNode = root.asNode();
//		if(!seen.contains(rootNode)) {
//			if(root.isResource()) {
//				Resource r = root.asResource();
//				Iterator<Statement> itStmt = r.listProperties();
//				while(itStmt.hasNext()) {
//					Statement stmt = itStmt.next();
//					Property p = stmt.getPredicate();
//					if(p.asNode().isVariable()) {
//						// result = AggMap<K, V>.create(mapper, subAgg)
//					}
//					
//					
//					RDFNode o = stmt.getObject();
//					// boolean isRdfList = RDFList;
//					
//					
//					
//					
//					//AggMap2<B, K, V, Aggregator<B,V>>
//					//AggObject.create(keyToSubAgg)
//					
//				}
//				
//				
//			}
//			
//			
//		}
//		return null;
//	}
}
