package org.aksw.jena_sparql_api.conjure.test;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;

public class TestResourceHash {
//	
	public static void main(String[] args) {		
		Op opA = OpUnion.create(
				OpConstruct.create(OpData.create(), "CONSTRUCT WHERE { ?s ?p ?o }"));

		Op opB = OpUnion.create(opA, opA);
		System.out.println(ResourceTreeUtils.createGenericHash(opB));
	}
	

}
