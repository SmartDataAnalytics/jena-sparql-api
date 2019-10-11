package org.aksw.jena_sparql_api.conjure.test;

import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TestResourceHash {
//	
	public static void main(String[] args) {
		Model m = ModelFactory.createDefaultModel();
		Op opA = OpUnion.create(m,
				OpConstruct.create(m, OpData.create(m), "CONSTRUCT WHERE { ?s ?p ?o }"));

		Op opB = OpUnion.create(m, opA, opA);
		System.out.println(ResourceTreeUtils.createGenericHash(opB));
	}
	

}
