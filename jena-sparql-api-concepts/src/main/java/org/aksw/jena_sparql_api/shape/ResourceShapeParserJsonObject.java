package org.aksw.jena_sparql_api.shape;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;


/*
 * http://www.grappa.univ-lille3.fr/~staworko/papers/staworko-arxiv-shex.pdf
 * 
 * E := e | a | E* | E1 or E2 | E1 and E2 
 * 
 * e = empty expression
 * and = unordered concatenation
 * or = disjunction
 * * = Kleene star
 * 
 * E? = (e or E)
 * E+ = (E and E*)
 * 
 */

// NOTE Maybe we should use the term silhouette to distinguish our approach from the shapes
// a silhouette expression could be shortened to 'silex'.
// What is the semantics of a shex??????
// [[E]]_D
// The evaluation of a silex E over a dataset D yields a graph
// This means, that a silex must be converted to a sparql query first
interface Shex {

}

// Note: could also be modeled as a constant of type ShEx
class ShexEmpty
    implements Shex
{
    
}

class ShexNav
    implements Shex
{
    private Shex base;
    private String predicate;
    private boolean isInverse;
}

abstract class ShexBase2 {
    private Shex left;
    private Shex right;
}

/**
 * Union of two shex expressions
 * 
 * @author raven
 *
 */
class ShexUnion
    extends ShexBase2
{
}

/**
 * 
 * @author raven
 *
 */
class ShexAnd
    extends ShexBase2
{
    
}


/**
 * shape: true
 * 
 * shape: 'rdfs:label'
 * 
 * 
 * shape: ['rdf:type', 'rdfs:label']
 * 
 * shape: {
 *   'rdf:type': true # Fetch rdf:type triples together with all outgoing triples of rdf:type
 * }
 * 
 * shape: {
 *   'rdf:type': false # Fetch rdf:type triples, but no further reachable triples
 * }
 *
 * shape: {
 *   '-rdf:type': ... # Prefix with '-' to navigate in inverse direction (should replace '&gt' which we used so far)
 * }
 *
 * shape: {
 *   '~?p = rdf:type && langMatches(lang(?o), "en")' // Prefix with ~ to use a sparql expression
 * }
 * 
 * Special attributes start with '$':
 * $filter: Set a concept for filtering the set of reached resources
 * 
 * note:
 * ['rdf:type'] is equivalent to { 'rdf:type': false }
 * 
 * shape: {
 *   'rdf:type': {
 *     $filter: '?s | ?s a owl:Class' // Only fetch types that are owl:Classes (i.e. exclude e.g. SKOS concepts),
 *     $predicates: ['rdfs:label']     
 *   }
 * }
 * 
 * Macro symbols:
 * shape: '{@literal @}spatial'
 * 
 * At {@literal @}spatial will extended with its definition.
 * 
 * 
 * @author raven
 *
 */
public class ResourceShapeParserJsonObject {
    public static final String geo = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    public static final String geom = "http://geovocab.org/geometry#";
    public static final String ogc = "http://www.opengis.net/ont/geosparql#asWKT";

    
    // TODO: All these things should be resource shape objects that must be combinable
    public static final String wgs84 = "['geo:lat', 'geo:long']";
    public static final String wgs84geometry = "['geo:geometry']";
    public static final String geoSparqlLgd = "[geom:geometry: 'ogc:AsWkt']";
    
    private PrefixMapping prefixMapping;
    
    public ResourceShapeParserJsonObject(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }
    
    
    /**
     * String must be of format
     * [-] [~] str
     * 
     * -: If present, assume inverse direction
     * ~: If present, str is assumed to be a SPARQL expression. Otherwise, a property URI is assumed
     * 
     * 
     * @param str
     * @return
     */
    public static StepRelation parseStep(String str, PrefixMapping prefixMapping) {
        str = str.trim();
        
        // Check the first character
        char c = str.charAt(0);
        boolean isInverse = c == '-';
        
        if(isInverse) {
            str = str.substring(1);
        }
        
        c = str.charAt(0);
        boolean isExpr = c == '~';

        if(isExpr) {
            str = str.substring(1);
        }        
        
        Expr expr;
        if(isExpr) {
            expr = ExprUtils.parse(str, prefixMapping);
        } else {
            String p = prefixMapping.expandPrefix(str);
            Node np = NodeFactory.createURI(p);
            expr = new E_Equals(new ExprVar(Vars.p), NodeValue.makeNode(np));  
        }
        
        BinaryRelation relation = new BinaryRelationImpl(new ElementFilter(expr), Vars.p, Vars.o);
        
        StepRelation result = new StepRelation(relation, isInverse);
        return result;
    }
    
    public ResourceShape parse(Object obj) {
        ResourceShapeBuilder builder = new ResourceShapeBuilder(prefixMapping);
        parse(obj, builder);
        
        ResourceShape result = builder.getResourceShape();
        return result;
    }
    
    public ResourceShape parse(Object obj, ResourceShapeBuilder builder) {
        
        if(obj == null) {
            // nothing to do
        }
        if(obj instanceof Boolean) { // true -> fetch all properties
            Boolean tf = (Boolean)obj;
            if(tf == true) {
                builder.nav(NodeValue.TRUE, true);
            }
        }
        else if (obj instanceof String) { // fetch a single property
            String str = (String)obj;
            StepRelation step = parseStep(str, prefixMapping);
            
            builder.nav(step);
        }
        else if (obj instanceof List) { // fetch an array of properties (possibly nested)c
            List<?> list = (List<?>)obj;
            for(Object item : list) {
                parse(item, builder);
            }
        }
        else if (obj instanceof Map) { //
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)obj;
            for(Entry<String, Object> entry : map.entrySet()) {
                String str = entry.getKey();
                Object o = entry.getValue();
                StepRelation step = parseStep(str, prefixMapping);
                ResourceShapeBuilder subBilder = builder.nav(step);
                parse(o, subBilder);
                
            }
        } else {
            throw new RuntimeException("Unsupported argument: " + obj);
        }
        
        return null;
    }
}
