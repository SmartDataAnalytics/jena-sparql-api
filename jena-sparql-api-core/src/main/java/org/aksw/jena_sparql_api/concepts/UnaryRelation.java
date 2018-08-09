package org.aksw.jena_sparql_api.concepts;

import java.util.Collections;
import java.util.List;

import org.apache.jena.sparql.core.Var;

public interface UnaryRelation
	extends Relation
{
	Var getVar();
	
	@Override
	default List<Var> getVars() {
		Var v = getVar();
		return Collections.singletonList(v);		
	}
	
	
	default boolean isSubjectConcept() {
		return ConceptUtils.isSubjectConcept(this);
	}
}
