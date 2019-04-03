package org.aksw.jena_sparql_api.concepts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.ParserSPARQL11;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.PatternVars;

import com.google.common.collect.Sets;

public class RelationUtils {

//    public static Relation createRelationRenamed(Relation prototype, Relation target) {
//        RelationUtils.create
//
//        Set<Var> allowed = new HashSet<Var>(Arrays.asList(sourceVar, targetVar));
//        Set<Var> bl = Sets.difference(blacklist, allowed);
//
//        Generator<Var> gen = VarGeneratorBlacklist.create("v", bl);
//
//        Set<Var> sourceVars = relation.getVarsMentioned();
//        Map<Var, Var> varMap = VarUtils.createJoinVarMap();
//
//    }

	public static Relation fromQuery(String queryStr) {
		return fromQuery(queryStr, PrefixMapping.Extended);
	}

	public static Relation fromQuery(String queryStr, PrefixMapping prefixMapping) {
        Query query = new Query();
        query.setPrefixMapping(prefixMapping);
        // TODO Make parser configurable
        SPARQLParser parser = new ParserSPARQL11();
        parser.parse(query, queryStr);

    	Relation result = fromQuery(query);
    	return result;
	}
	
	public static Relation fromQuery(Query query) {
		Relation result;
		if(query.isSelectType()) {
			List<Var> vars = query.getProjectVars();
			Element element = query.getQueryPattern();
			result = new RelationImpl(element, vars);
		} else {
			throw new RuntimeException("SELECT query form expected, instead got " + query);
		}
		
		return result;
	}

	

    public static Triple extractTriple(BinaryRelation relation) {
        Element e = relation.getElement();
        Triple result = ElementUtils.extractTriple(e);
        return result;
    }

//    public static Relation union(Relation a, Relation b, boolean transformInPlaceIfApplicable) {
//    	Relation result = addUnionMember(a, b, false);
//    	return result;
//    }
    
//    public static Relation concat(Relation a, Relation b, boolean transformInPlaceIfApplicable) {
//    	
//    }
    
    public static BinaryRelation and(BinaryRelation a, BinaryRelation b, boolean transformInPlaceIfApplicable) {
    	Element ae = a.getElement();
    	Element be = b.getElement();
    	
    	Collection<Var> vas = PatternVars.vars(ae);
    	Collection<Var> vbs = PatternVars.vars(be);
    	Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, null);
    	
    	varMap.put(b.getSourceVar(), a.getTargetVar());
    	Element ce = ElementUtils.createRenamedElement(be, varMap);
    	
    	ElementGroup eg;
    	boolean isInPlace = ae instanceof ElementGroup && transformInPlaceIfApplicable; 
    	if(isInPlace) {
    		eg = (ElementGroup)ae;
    	} else {
    		eg = new ElementGroup();
    		eg.addElement(ae);
    	}
    	eg.addElement(ce);;
		
    	BinaryRelation result = new BinaryRelationImpl(eg, a.getSourceVar(), varMap.getOrDefault(b.getTargetVar(), a.getSourceVar()));
    	
    	return result;    	
    }
    
    
    /**
     * 
     * 
     * @param a
     * @param b
     * @param transformInPlaceIfApplicable Add 'b' to to 'a' if a's element already is a union
     * @return
     */
    public static BinaryRelation union(BinaryRelation a, BinaryRelation b, boolean transformInPlaceIfApplicable) {
    	Element ae = a.getElement();

    	ElementUnion u;
    	boolean isInPlace;
    	if(transformInPlaceIfApplicable && a.getElement() instanceof ElementUnion) {
    		u = (ElementUnion)ae;
    		isInPlace = true;
    	} else {
    		u = new ElementUnion();
    		u.addElement(a.getElement());
    		isInPlace = false;
    	}
    	
    	
    	Map<Var, Var> varMap = new HashMap<>();
    	
    	Collection<Var> vas = PatternVars.vars(a.getElement());
    	Collection<Var> vbs = PatternVars.vars(b.getElement());
    	VarUtils.createDistinctVarMap(vbs, vas, true, null);
    	
    	varMap.put(b.getSourceVar(), a.getSourceVar());
    	varMap.put(b.getTargetVar(), a.getTargetVar());
    	Element c = ElementUtils.createRenamedElement(b.getElement(), varMap);
    	u.addElement(c);

    	BinaryRelation result = isInPlace ? a : new BinaryRelationImpl(u, a.getSourceVar(), a.getTargetVar());
    	return result;
    }
    
    public static BinaryRelation createRelation(String propertyUri, boolean isInverse, PrefixMapping prefixMapping) {

        String p = prefixMapping == null ? propertyUri : prefixMapping.expandPrefix(propertyUri);
        Node node = NodeFactory.createURI(p);
        BinaryRelation result = createRelation(node, isInverse);
        return result;
    }


    public static BinaryRelation createRelation(Node property, boolean isInverse) {
    	
        //Expr expr = new E_Equals(new ExprVar(Vars.p), ExprUtils.nodeToExpr(property));
    	
    	Triple t = isInverse
    			? new Triple(Vars.o, property, Vars.s)
    			: new Triple(Vars.s, property, Vars.o);
    	    	
    	Element element = ElementUtils.createElement(t);
    	//Element element = new ElementTriplesBlock(bgp);
        BinaryRelation result = new BinaryRelationImpl(element, Vars.s, Vars.o);//createRelation(expr, isInverse);
        return result;
    }

    public static BinaryRelation createRelation(Property property, boolean isInverse) {
        BinaryRelation result = createRelation(property.asNode(), isInverse);
        return result;
    }


    public static BinaryRelation createRelation(Expr expr, boolean isInverse) {
        BinaryRelation result = new BinaryRelationImpl(new ElementFilter(expr), Vars.p, Vars.o);
        return result;
    }


//    public static Relation createRelation(StepRelation step) {
//        Relation result = nav(step.getRelation(), step.isInverse());
//        return result;
//    }


    public static Query createQuery(Relation relation) {
    	// If the element is already a query, just limit the projection
        Element e = relation.getElement();
        List<Var> vars = relation.getVars();

        Query result;
        if(e instanceof ElementSubQuery) {
        	result = ((ElementSubQuery)e).getQuery().cloneQuery();
        	
        	// Update the projection
        	Set<Var> removals = new HashSet<>(result.getProject().getVars());
        	removals.removeAll(vars);

        	VarExprList project = result.getProject();
        	removals.forEach(project::remove);
        	
        } else {
	        result = new Query();
	        result.setQuerySelectType();
	
	        result.setQueryPattern(e);
	
	        VarExprList project = result.getProject();
	        vars.forEach(project::add);
        }

        return result;
    }
}
