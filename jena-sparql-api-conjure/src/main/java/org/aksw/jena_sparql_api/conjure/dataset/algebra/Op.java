package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public interface Op
	extends Resource //, TreeLike<Op>
{
	//@Override
	List<Op> getChildren();
	Op clone(Model cloneModel, List<Op> op);
	
	<T> T accept(OpVisitor<T> visitor);
}
