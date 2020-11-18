package org.aksw.jena_sparql_api.sparql_path.core;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL2;

public class PathConstraint3
	extends PathConstraintBase
{
	@Override
    public Collection<Triple> createOutgoingPattern(Node type, Node p) {
		// TODO: Rename Vars.p if it is equal to s or p
		
		return Arrays.asList(
				new Triple(type, Vars.x, Vars.o),
				new Triple(Vars.x, OWL2.annotatedProperty.asNode(), p));
	}

	@Override
    public Collection<Triple> createIngoingPattern(Node type, Node p) {
		return Arrays.asList(
				new Triple(Vars.s, Vars.x,type),
				new Triple(Vars.x, OWL2.annotatedProperty.asNode(), p));

//		Triple u = new Triple(p, VocabPath.isIngoingPredicateOf.asNode(), type);
//    	return Collections.singleton(u);
    }

}
