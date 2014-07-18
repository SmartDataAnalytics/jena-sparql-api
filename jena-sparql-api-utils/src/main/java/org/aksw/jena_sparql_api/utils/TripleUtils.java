package org.aksw.jena_sparql_api.utils;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;

public class TripleUtils {
	public static String toNTripleString(Triple triple) {
		String s = NodeUtils.toNTriplesString(triple.getSubject());
		String p = NodeUtils.toNTriplesString(triple.getPredicate());
		String o = NodeUtils.toNTriplesString(triple.getObject());
		
		String result = s + " " + p + " " + o + " .";
		
		return result;
	}
	
	public static Triple swap(Triple t) {
        Triple result = new Triple(t.getObject(), t.getPredicate(), t.getSubject());
        return result;
	}
	
    public static Set<Triple> swap(Iterable<Triple> triples) {
        Set<Triple> result = new HashSet<Triple>();
        
        for(Triple t : triples) {
            result.add(swap(t));
        }
        
        return result;
    }
}