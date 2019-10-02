package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

public class OpUtils {
	
	public static Collection<Op> peekingSubOps(Op op) {
		Collection<Op> result = op.getSubOps();
		System.out.println("SubOps for " + op.getClass() + " "+ op + " are " + result);
		return result;
	}
	
	public static int getNumOps(Op op) {
		
		// TODO We may want to exclude counting leaf nodes
		// as they do not require any operation
		// then again, a node with multiple children may require more time
		// than one with fewer
		/**
		 * Get the number of operations in the expression.
		 * Can be used as a poor-mans cost estimate
		 */
//		int result = (int)Streams.stream(Traverser.forTree(OpUtils::peekingSubOps)
		int result = (int)Streams.stream(Traverser.forTree(Op::getSubOps)
			.depthFirstPreOrder(op))
			.count();
		
		return result;
	}
	
	public static void clearSubTree(Op rootOp) {
		List<Op> ops = Streams.stream(Traverser.forTree(Op::getSubOps)
				.depthFirstPostOrder(rootOp))
				.collect(Collectors.toList());
		
		for(Op op : ops) {
			// TODO Handle lists
			op.removeProperties();
		}		
	}
	
	
	public static Op optimize(Op op, OpVisitor<String> hasher, ResourceStore hashSpace) {
		String hash = op.accept(hasher);
		
		RdfHttpResourceFile res = hashSpace.getResource(hash);
		RdfHttpEntityFile entity = res.allocate(ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class)
				.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString()));
		
		Path path = entity.getAbsolutePath();
		// TODO Check if it exists and
		boolean exists = Files.exists(path);
		Op result;
		if(exists) {
			// In-place change the description of the op into a static reference
			// TODO This does not clear children of the op - so it leaves clutter
			// behind in the model which is not very aesthetic - then again, it is harmless
			//op.removeProperties();
			OpUtils.clearSubTree(op);
			OpPath opValue = op.as(OpPath.class);
			opValue.setName(path.toString());
			
			result = opValue;
		} else {
			List<Op> children = op.getSubOps();
			for(Op child : children) {
				optimize(child, hasher, hashSpace);
			}
			result = op;
		}
		
		return result;
	}
	
	
}