package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Resource;

import com.google.common.collect.Streams;
import com.google.common.graph.SuccessorsFunction;
import com.google.common.graph.Traverser;

public class ResourceTreeUtils {
	
	public static <T> Iterable<? extends T> peekingSubOps(T op, SuccessorsFunction<T> successorsFunction) {
		Iterable<? extends T> result = successorsFunction.successors(op);
		System.out.println("SubOps for " + op.getClass() + " "+ op + " are " + result);
		return result;
	}
	
	public static <T> int getNumOps(T op, SuccessorsFunction<T> successorsFunction) {
		
		// TODO We may want to exclude counting leaf nodes
		// as they do not require any operation
		// then again, a node with multiple children may require more time
		// than one with fewer
		/**
		 * Get the number of operations in the expression.
		 * Can be used as a poor-mans cost estimate
		 */
//		int result = (int)Streams.stream(Traverser.forTree(OpUtils::peekingSubOps)
		int result = (int)Streams.stream(Traverser.forTree(successorsFunction)
			.depthFirstPreOrder(op))
			.count();
		
		return result;
	}
	
	public static <T extends Resource> void clearSubTree(T rootOp, SuccessorsFunction<T> successorsFunction) {
		List<T> ops = Streams.stream(Traverser.forTree(successorsFunction)
				.depthFirstPostOrder(rootOp))
				.collect(Collectors.toList());
		
		for(T op : ops) {
			// TODO Handle lists
			op.removeProperties();
		}		
	}

}