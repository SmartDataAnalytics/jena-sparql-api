package org.aksw.jena_sparql_api.concepts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.PatternVars;

import com.google.common.collect.Sets;

/**
 * Base interface for SPARQL relations
 * @author raven Mar 7, 2018
 *
 */
public interface Relation {

	/**
	 * Return the distinguished variables of the relation
	 * 
	 * @return
	 */
	List<Var> getVars();
	
	
	Element getElement();
	
	/**
	 * Return the set of mentioned variables without the distinguished ones
	 * 
	 * @return
	 */
	default Set<Var> getNonDistinguishedVars() {
		Set<Var> mentionedVars = getVarsMentioned();
		Set<Var> markedVars = new HashSet<>(getVars());
		Set<Var> result = Sets.difference(mentionedVars, markedVars);

		return result;
	}

	
	default UnaryRelation toUnaryRelation() {
		List<Var> vars = getVars();
		UnaryRelation result;
		if(vars.size() == 1) {
			Element e = getElement();
			result = new Concept(e, vars.get(0));
		} else {
			throw new UnsupportedOperationException();
		}
		
		return result;
	}

	default BinaryRelation toBinaryRelation() {
		List<Var> vars = getVars();
		BinaryRelation result;
		if(vars.size() == 2) {
			Element e = getElement();
			result = new BinaryRelationImpl(e, vars.get(0), vars.get(1));
		} else {
			throw new UnsupportedOperationException();
		}
		
		return result;
	}

	
	default TernaryRelation toTernaryRelation() {
		List<Var> vars = getVars();
		TernaryRelation result;
		if(vars.size() == 3) {
			Element e = getElement();
			result = new TernaryRelationImpl(e, vars.get(0), vars.get(1), vars.get(2));
		} else {
			throw new UnsupportedOperationException();
		}
		
		return result;
	}

//	default TernaryRelation asTernaryRelation() {
//		
//	}
	
	
	default Set<Var> getVarsMentioned() {
		Element e = getElement();
		Set<Var> result = new HashSet<>(PatternVars.vars(e));
		
		// Note: Usually the relation can be considered inconsistent if the vars are not
		// mentioned in element; however, it is useful for empty relations
		result.addAll(getVars());
		return result;
	}
	
	default Relation applyNodeTransform(NodeTransform nodeTransform) {
		return applyDefaultNodeTransform(this, nodeTransform);
	}

	// TODO Move to relation utils
    static Relation applyDefaultNodeTransform(Relation r, NodeTransform nodeTransform) {
    	List<Var> transformedVars = r.getVars().stream()
    			.map(v -> VarUtils.applyNodeTransform(v, nodeTransform))
    			.collect(Collectors.toList());
    	
        Element transformedElement = ElementUtils.applyNodeTransform(r.getElement(), nodeTransform);

        Relation result = new RelationImpl(transformedElement, transformedVars);
        return result;
    }

    
    // Keeps all variables of this relation intact, and appends the element of another relation
    default RelationJoiner joinOn(Var ... vars) {
    	return RelationJoiner.from(this, vars);
    }

    // Similar to joinOn - but *prepends* the elements of the other relation
    default RelationJoiner injectOn(Var ... vars) {
    	return RelationJoiner.from(this, vars).filterRelationFirst(true);
    }

    default Relation project(List<Var> vars) {
    	return new RelationImpl(getElement(), vars);
    }

    default Relation project(Var ... vars) {
    	return project(Arrays.asList(vars));
    }
    
    default Query toQuery() {
    	Query result = RelationUtils.createQuery(this);
    	return result;
    	
//        Query result = new Query();
//        result.setQuerySelectType();
//
//        Element e = getElement();
//        List<Var> vars = getVars();
//        result.setQueryPattern(e);
//
//        VarExprList project = result.getProject();
//        vars.forEach(project::add);
//
//        return result;
    }
    
    default List<Element> getElements() {
        return ElementUtils.toElementList(getElement());
    }
    
//	public static TernaryRelation from(Triple t) {
//		new TernaryRelationImpl(ElementUtils.createElement(t), t.getSubject(), t.getPredicate(), t.getObject())
//	}
}
