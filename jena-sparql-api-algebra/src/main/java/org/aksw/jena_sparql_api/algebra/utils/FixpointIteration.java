package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Objects;
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
	
//	public static <T> T fixpointIteration(int max, T init, Function<? super T, ? extends T> fn) {
//		T result = init;
//		
//		int i = 0;
//		for(; i < max; ++i) {
//			T tmp = fn.apply(result);
//			if(Objects.equals(tmp, result)) {
//				break;
//			}
//			result = tmp;
//		}
//		
////		if(i >= max) {
////			logger.warn("Fixpoint iteration reached iteration threshold");
////		}
//
//		return result;
//	}
}
