package org.aksw.jena_sparql_api.conjure.dataset.algebra;

import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * A sequence of SPARQL statements - i.e. queries and updates.
 * The output of this operation the the dataset obtained from all construct queries.
 * 
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
public interface OpStmtList
	extends Op1
{
	@Iri("rpif:queryString")
	List<String> getStmts();
	OpStmtList setStmts(List<String> stmts);
	
	@Override
	OpStmtList setSubOp(Op subOp);
	
	@Override
	default <T> T accept(OpVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
	
	@Override
	default OpStmtList clone(Model cloneModel, List<Op> subOps) {
		return this.inModel(cloneModel).as(OpStmtList.class)
				.setSubOp(subOps.iterator().next())
				.setStmts(getStmts());
	}

	
	public static OpStmtList create(Model model, Op subOp, String queryString) {
		OpStmtList result = create(model, subOp, Collections.singletonList(queryString));
		
		return result;
	}
	
	public static OpStmtList create(Model model, Op subOp, List<String> stmts) {
		OpStmtList result = model.createResource().as(OpStmtList.class)
			.setSubOp(subOp)
			.setStmts(stmts);
		
		return result;
	}
}
