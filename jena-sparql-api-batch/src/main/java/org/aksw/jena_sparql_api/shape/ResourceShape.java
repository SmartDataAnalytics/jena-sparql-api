package org.aksw.jena_sparql_api.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;



//class Target {
//    private Concept concept;
//}

/**
 * A graph expression is a SPARQL expression which only makes use of the variables ?s ?p and ?o
 * The evaluation of a graph expression te over an RDF dataset D yields a graph (a set of triples)
 * 
 * [[te]]_D
 * 
 * 
 * Maybe we should take a set of triples, derive a set of related resources,
 * and then for this related set of resources specify which triples we are interested in again.
 * 
 * In this case, the model is Map<Expr, TripleTree>
 * i.e. the expression evaluates to a resource for each triple, and these resources are used for further lookups. 
 * However, how would we navigate in inverse direction?
 * 
 * Each resource r is associated with a set of ingoing and outgoing triples:
 * 
 * outgoing: { t | t in G and t.s = r}
 * ingoing: { t | t in G and t.o = r}
 *
 * 
 * 
 * 
 * Navigating to the set of related triples:
 * Let G be the set of all triples and G1 and G2 subsetof G be two graphs.
 * { t2 | t1 in G1 and expr(t1, t2)}
 * 
 * 
 * We could use expressions over the additional variables ?x ?y ?z to perform joins:
 * (?s ?p ?o ?x ?y ?z)
 * 
 * ?s = ?x
 * 
 * 
 * G1 x G2 
 * 
 * 
 * @author raven
 *
 */
public class ResourceShape {
    // Constrains the set of resources; such as if one wants all
    // triples about the buyers of a book, however, only for male buyers.
    // (?p = buyerOfBook) ->outgoing
    // May be null.
    private Concept concept;
    
//    Var startVar; // Usually ?s; but could be ?o - TODO would ?p be valid?
    
    // Join condition to sub triples, only valid variables are ?s, ?p and ?o
    // Example: Outgoing edges could be: (?s a ?o) -> (?s ?p ?o)
    private Multimap<Concept, ResourceShape> outgoing = HashMultimap.create();
    private Multimap<Concept, ResourceShape> ingoing = HashMultimap.create();
    
    public Multimap<Concept, ResourceShape> getOutgoing() {
        return outgoing;
    }

    public Multimap<Concept, ResourceShape> getIngoing() {
        return ingoing;
    }
    
    /**
     * Create a construct query for fetching data in this shape
     * @param startConcept
     * @return
     */
    public Query createQuery(Concept startConcept, Generator<Var> generator) {
        
        
        outgoing.asMap()
        
        // propertyConcept: ?p | ?p = rdf:type --- the set of all properties which to traverse
        // objectConcept: ?o 
        // outgoing(propertyConcept, objectConcept)@
        
        
        
        Triple triple = new Triple(Var)
        
        // Create a union of 
        
        
    }

    public List<Element> createElements(Multimap<Concept, ResourceShape> edges, boolean isInverse) {
        List<Element> result = createElements(edges.asMap(), isInverse);
        return result;
    }
    
    public List<Element> createElements(Map<Concept, Collection<ResourceShape>> edges, boolean isInverse) {
        List<Element> result = new ArrayList<Element>();
        for(Entry<Concept, Collection<ResourceShape>> entry : edges.entrySet()) {
            Concept concept = entry.getKey();
            Collection<ResourceShape> children = entry.getValue();
            
            for(ResourceShape child : children) {
                Element element = createElement(concept, child, isInverse);
                result.add(element);
            }
        }
        
        return result;
    }
    
    /**
     * Creates elements for this node and then descends into its children
     * 
     * @param predicateConcept
     * @param target
     * @param isInverse
     * @return
     */
    public static Element createElement(Element baseElement, Generator<Var> vargen, Concept predicateConcept, ResourceShape target, boolean isInverse) {
        // Rename the variables s, p, o with fresh variables from the vargenerator
        Map<Var, Var> rename = new HashMap<Var, Var>();
        rename.put(Vars.s, vargen.next());
        rename.put(Vars.p, vargen.next());
        rename.put(Vars.o, vargen.next());
        
        Element e1 = ElementUtils.substituteNodes(baseElement, rename);
        
        Triple triple = isInverse
                ? new Triple(Vars.o, Vars.p, Vars.s)
                : new Triple(Vars.s, Vars.p, Vars.o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);
        
        ElementTriplesBlock e2 = new ElementTriplesBlock(bp);
        
        Element e = ElementUtils.mergeElements(e1, e2);
       
        Collection<Var> eVars = PatternVars.vars(e);
        Set<Var> pVars = predicateConcept.getVarsMentioned();
        
        // Add the predicateConcept
        Map<Var, Var> pc = ElementUtils.createDistinctVarMap(eVars, pVars, true, vargen);
        // Map the predicate concept's var to ?p
        pc.put(predicateConcept.getVar(), Vars.p);

        
        Element e3 = ElementUtils.substituteNodes(predicateConcept.getElement(), pc);
        
        Set<Node> concretePredicates = new HashSet<Node>(); 
        if(e3 instanceof ElementFilter) {
            ElementFilter filter = (ElementFilter)e3;
            Expr expr = filter.getExpr();
            Pair<Var, NodeValue> c = ExprUtils.extractConstantConstraint(expr);
            if(c.getKey().equals(Vars.p)) {
                Node n = c.getValue().asNode();
                concretePredicates.add(n);
            }
        }
        
        Element newElement = ElementUtils.mergeElements(e, e3);

        
        // TODO Use the target's concept here already?
        
        
        
        // Use the newElement as the next baseElement
        
        Generator<Var> subGenerator = vargen.clone();

        
        target.createElements(vargen);
        //target.getIngoing();
        
        //target.getOutgoing();
        
        
        results.add(e);
        
        
        return result;
    }
    
    
    /**
     * Whether a triple matches any of the ingoing or outgoing filter expression of this node
     * @param triple
     * @param functionEnv
     * @return
     */
    public boolean contains(Triple triple, FunctionEnv functionEnv) {
        Set<Expr> exprs = Sets.union(outgoing.keySet(), ingoing.keySet());
        
        boolean result = contains(exprs, triple, functionEnv);        
        return result;
    }
    
    public static boolean contains(Collection<Expr> exprs, Triple triple, FunctionEnv functionEnv) {
        Binding binding = TripleUtils.tripleToBinding(triple);
        
        boolean result = false;
        for(Expr expr : exprs) {
            NodeValue nodeValue = expr.eval(binding, functionEnv);            
            
            if(nodeValue.equals(NodeValue.TRUE)) {
                result = true;
                break;
            }
        }
        
        return result;        
    }
    
    /**
     * Get all triples about the titles of books, together with the books'
     * male buyers' names and age.
     * 
     * The generated query has the structure
     * 
     * Construct {
     *   ?s ?p ?o
     * } {
     *   { ?x a Book . Bind(?x as ?s)} # Root concept
     *   
     *     { # get the 'buyer' triples
     *       ?s ?p ?o . Filter(?p = boughtBy) // outgoing
     *       { ?o gender male }} // restricting the resources of the relation
     *       
     *              
     *       #Optional {
     *       #  ?x ?y ?z . Filter(?y = rdfs:label)
     *       #}
     *     } 
     *   Union {
     *     { # get the buyers names - requires repetition of above's pattern
     *       ?x ?y ?s . Filter(?y = boughtBy)
     *       { ?s gender male }} // restricting the resources of the relation
     *       
     *       ?s ?p ?o . Filter(?p = rdfs:label)
     *     }   
     *   Union
     *     {
     *       ?s ?p ?o . Filter(?p = dc:title && langMatches(lang(?o), 'en')) # outgoing
     *     }
     * }
     * 
     * 
     * @param root
     * @return
     */
    public static Query createQuery(ResourceShape root) {
        List<Element> unionMembers = new ArrayList<Element>();
    }
    
    
}
