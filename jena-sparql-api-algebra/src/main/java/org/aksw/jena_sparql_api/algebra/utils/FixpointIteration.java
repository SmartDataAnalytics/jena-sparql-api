package org.aksw.jena_sparql_api.algebra.utils;

import java.util.function.Function;

public class FixpointIteration {

	public static <T> T apply(T op, Function<? super T, ? extends T> transform) {
		T current;
	    do {
	        current = op;
	        op = transform.apply(current);
	    } while(!current.equals(op));
	    
	    return current;
	}
}
