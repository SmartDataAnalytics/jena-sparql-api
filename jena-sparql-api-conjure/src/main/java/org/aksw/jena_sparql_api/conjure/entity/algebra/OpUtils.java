package org.aksw.jena_sparql_api.conjure.entity.algebra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpResourceFile;
import org.aksw.jena_sparql_api.http.repository.api.ResourceStore;
import org.apache.http.entity.ContentType;
import org.apache.jena.rdf.model.ModelFactory;

public class OpUtils {	
	
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
			ResourceTreeUtils.clearSubTree(op, Op::getChildren);
			OpPath opValue = op.as(OpPath.class);
			opValue.setName(path.toString());
			
			result = opValue;
		} else {
			Collection<Op> children = op.getChildren();
			for(Op child : children) {
				optimize(child, hasher, hashSpace);
			}
			result = op;
		}
		
		return result;
	}
	
	
}