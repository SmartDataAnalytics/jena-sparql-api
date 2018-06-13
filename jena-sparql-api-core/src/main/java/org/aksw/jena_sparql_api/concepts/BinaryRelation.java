package org.aksw.jena_sparql_api.concepts;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

import com.google.common.collect.Sets;

public interface BinaryRelation 
	extends Relation
{
	Var getSourceVar();
	Var getTargetVar();
	
	default Set<Var> getMarkedVars() {
		Set<Var> result = new LinkedHashSet<>(Arrays.asList(getSourceVar(), getTargetVar()));
		return result;
	}
	
	default Set<Var> getIntermediaryVars() {
		Set<Var> mentionedVars = getVarsMentioned();
		Set<Var> markedVars = getMarkedVars();
		Set<Var> result = Sets.difference(mentionedVars, markedVars);

		return result;
	}
	
	default BinaryRelation reverse() {
		BinaryRelation result = new BinaryRelationImpl(getElement(), getTargetVar(), getSourceVar());
		return result;
	}

    /**
     * An empty relation does is equivalent to a zero-length path, i.e.
     * it navigates from a set of resources to the same set of resources.
     *
     * It is expressed as an empty graph pattern (ElementGroup), and
     * equal variables in source and target;
     *
     * @return
     */
    default boolean isEmpty() {
        boolean result;

        Element e = getElement();
        if(e instanceof ElementGroup) {
            ElementGroup g = (ElementGroup)e;
            result = g.getElements().isEmpty();

            //relation.getSourceVar().equals(relation.getTargetVar())
        } else {
            result = false;
        }

        return result;
    }


    default Concept getSourceConcept() {
        Concept result = new Concept(getElement(), getSourceVar());
        return result;
    }

    default Concept getTargetConcept() {
        Concept result = new Concept(getElement(), getTargetVar());
        return result;
    }
    
	default BinaryRelation applyNodeTransform(NodeTransform nodeTransform) {
		BinaryRelation result = Relation.applyDefaultNodeTransform(this, nodeTransform).toBinaryRelation();
		return result;
	}

}
