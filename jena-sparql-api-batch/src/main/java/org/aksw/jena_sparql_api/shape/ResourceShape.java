package org.aksw.jena_sparql_api.shape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptOps;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Triples;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import com.hp.hpl.jena.sparql.syntax.Template;


/**
 * How to deal with inserts?
 *
 * For example, the shape is: get all buyers (of dvds) togther with their (immediate friends), thus:
 *
 * Nav(Out(hasBuyer), Out(hasFriend))
 *
 *
 * Whenever we add triples that were not part of the original working set,
 * we could just add them to the store (it does not matter whether they already existed there or not).
 *
 * Yet, it would be very useful to validate whether newly inserted triples are part of a working set or not.
 *
 *
 *
 *
 * Whenever we delete triples from the working set, well, its a delete
 *
 *
 * If we added (EvilDead hasBuyer Alice),
 * then we know that this triple is real addition, because
 * - the subject equals the source resource
 * - the triple matches one of the immediate relations of the shape
 * - the triple was not part of the shape's graph
 *
 * However, if we added that triple, we might want to fetch data for Alice according to the shape.
 *
 *
 *
 */

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
    private Map<Relation, ResourceShape> outgoing = new HashMap<Relation, ResourceShape>();
    private Map<Relation, ResourceShape> ingoing = new HashMap<Relation, ResourceShape>();

    public Map<Relation, ResourceShape> getOutgoing() {
        return outgoing;
    }

    public Map<Relation, ResourceShape> getIngoing() {
        return ingoing;
    }

    public void extend(ResourceShape that) {
        // TODO Maybe we should create a deep clone of 'that' first
        this.outgoing.putAll(that.outgoing);
        this.ingoing.putAll(that.ingoing);
    }


    public static List<Concept> collectConcepts(ResourceShape source) {
        List<Concept> result = new ArrayList<Concept>();
        collectConcepts(result, source);
        return result;
    }

    public static void collectConcepts(Collection<Concept> result, ResourceShape source) {
        Generator<Var> vargen = VarGeneratorImpl.create("v");

        collectConcepts(result, source, vargen);
    }

    public static void collectConcepts(Collection<Concept> result, ResourceShape source, Generator<Var> vargen) {
        Concept baseConcept = new Concept((Element)null, Var.alloc("x"));
        collectConcepts(result, baseConcept, source, vargen);
    }

    public static void collectConcepts(Collection<Concept> result, Concept baseConcept, ResourceShape source, Generator<Var> vargen) {

        Map<Relation, ResourceShape> outgoing = source.getOutgoing();
        Map<Relation, ResourceShape> ingoing = source.getIngoing();

        collectConcepts(result, baseConcept, outgoing, false, vargen);
        collectConcepts(result, baseConcept, ingoing, true, vargen);

        //collectConcepts(result, null, source,);
    }

    public static void collectConcepts(Collection<Concept> result, Concept baseConcept, Map<Relation, ResourceShape> map, boolean isInverse, Generator<Var> vargen) {

//        Var baseVar = baseConcept.getVar();

        {
            Set<Relation> raw = map.keySet();
            Collection<Relation> opt = group(raw);

            for(Relation relation : opt) {
                //Concept sc = new Concept(relation.getElement(), baseVar);
                Concept sc = baseConcept;
                Concept item = createConcept(sc, vargen, relation, isInverse);
                result.add(item);
            }
        }


        Multimap<ResourceShape, Relation> groups = HashMultimap.create();

        for(Entry<Relation, ResourceShape> entry : map.entrySet()) {
            groups.put(entry.getValue(), entry.getKey());
        }

        for(Entry<ResourceShape, Collection<Relation>> group : groups.asMap().entrySet()) {
            ResourceShape target = group.getKey();
            Collection<Relation> raw = group.getValue();

            Collection<Relation> opt = group(raw);


            for(Relation relation : opt) {
                //Concept sc = new Concept(relation.getElement(), baseVar);
                Concept sc = baseConcept;

                Concept item = createConcept(sc, vargen, relation, isInverse);

                //result.add(item);

                // Map the

                // Now use the concept as a base for its children
                collectConcepts(result, item, target, vargen);
            }


        }
    }


    public static List<Relation> group(Collection<Relation> relations) {
        List<Relation> result = new ArrayList<Relation>();


        Set<Node> concretePredicates = new HashSet<Node>();
        Set<Expr> simpleExprs = new HashSet<Expr>();

        // Find all relations that are simply ?p = expr
        for(Relation relation : relations) {
            Var s = relation.getSourceVar();
//            Var t = relation.getTargetVar();
            Element e = relation.getElement();

            if(e instanceof ElementFilter) {
                ElementFilter filter = (ElementFilter)e;
                Expr expr = filter.getExpr();
                Pair<Var, NodeValue> c = ExprUtils.extractConstantConstraint(expr);
                if(c != null && c.getKey().equals(s)) {
                    Node n = c.getValue().asNode();
                    concretePredicates.add(n);
                } else {
                    simpleExprs.add(expr);
                }
            } else {
                result.add(relation);
                //throw new RuntimeException("Generic re")
            }

        }

        if(!simpleExprs.isEmpty()) {
            Expr orified = ExprUtils.orifyBalanced(simpleExprs);
            Relation r = asRelation(orified);
            result.add(r);
        }

        if(!concretePredicates.isEmpty()) {
            ExprList exprs = new ExprList();
            for(Node node : concretePredicates) {
                Expr expr = com.hp.hpl.jena.sparql.util.ExprUtils.nodeToExpr(node);
                exprs.add(expr);
            }

            ExprVar ep = new ExprVar(Vars.p);
            Expr ex = exprs.size() > 1
                    ? new E_OneOf(ep, exprs)
                    : new E_Equals(ep, exprs.get(0));

            Relation r = asRelation(ex);
            result.add(r);
        }

        return result;
    }


    public static Relation asRelation(Expr expr) {
        ElementFilter e = new ElementFilter(expr);
        Relation result = new Relation(e, Vars.p, Vars.o);

        return result;
    }


    public static Query createQuery(ResourceShape resourceShape, Concept filter) {
        List<Concept> concepts = ResourceShape.collectConcepts(resourceShape);

        Query result = createQuery(concepts, filter);
        return result;
    }

    public static Query createQuery(List<Concept> concepts, Concept filter) {

        Template template = new Template(BasicPattern.wrap(Collections.singletonList(Triples.spo)));

        List<Concept> tmps = new ArrayList<Concept>();
        for(Concept concept : concepts) {
            Concept tmp = ConceptOps.intersect(concept, filter);
            tmps.add(tmp);
        }

        List<Element> elements = new ArrayList<Element>();
        for(Concept concept : tmps) {
            Element e = concept.getElement();
            elements.add(e);
        }

        Element element = ElementUtils.union(elements);


        Query result = new Query();
        result.setQueryConstructType();
        result.setConstructTemplate(template);
        result.setQueryPattern(element);

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
    public static Concept createConcept(Concept baseConcept, Generator<Var> vargen, Relation predicateRelation, boolean isInverse) {
        Var sourceVar;

        Var baseVar = baseConcept.getVar();
        Element baseElement = baseConcept.getElement();


        Triple triple = isInverse
                ? new Triple(Vars.o, Vars.p, Vars.s)
                : new Triple(Vars.s, Vars.p, Vars.o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);

        ElementTriplesBlock e2 = new ElementTriplesBlock(bp);

        Element e;
        if(baseElement != null) {
            //Var baseVar = baseConcept.getVar();
            //Element baseElement = baseConcept.getElement();


            // Rename the variables s, p, o with fresh variables from the vargenerator
            Map<Var, Var> rename = new HashMap<Var, Var>();
            rename.put(Vars.s, vargen.next());
            rename.put(Vars.p, vargen.next());
            rename.put(Vars.o, Vars.s);

            sourceVar = MapUtils.getOrElse(rename, baseVar, baseVar);
            Element e1 = ElementUtils.substituteNodes(baseElement, rename);
            e = ElementUtils.mergeElements(e1, e2);
        }   else {
            e = e2;
            sourceVar = Vars.s;
        }


        Collection<Var> eVars = PatternVars.vars(e);
        Set<Var> pVars = predicateRelation.getVarsMentioned();

        // Add the predicateConcept
        Map<Var, Var> pc = VarUtils.createDistinctVarMap(eVars, pVars, true, vargen);
        // Map the predicate concept's var to ?p
        pc.put(predicateRelation.getSourceVar(), Vars.p);
        pc.put(predicateRelation.getTargetVar(), Vars.o);


        Element e3 = ElementUtils.substituteNodes(predicateRelation.getElement(), pc);
        Element newElement = ElementUtils.mergeElements(e, e3);


        Concept result = new Concept(newElement, sourceVar);

        return result;
    }


    /**
     * Whether a triple matches any of the ingoing or outgoing filter expression of this node
     * @param triple
     * @param functionEnv
     * @return
     */
//    public boolean contains(Triple triple, FunctionEnv functionEnv) {
//        Set<Expr> exprs = Sets.union(outgoing.keySet(), ingoing.keySet());
//
//        boolean result = contains(exprs, triple, functionEnv);
//        return result;
//    }

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
//    public static Query createQuery(ResourceShape root) {
//        List<Element> unionMembers = new ArrayList<Element>();
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ingoing == null) ? 0 : ingoing.hashCode());
        result = prime * result
                + ((outgoing == null) ? 0 : outgoing.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceShape other = (ResourceShape) obj;
        if (ingoing == null) {
            if (other.ingoing != null)
                return false;
        } else if (!ingoing.equals(other.ingoing))
            return false;
        if (outgoing == null) {
            if (other.outgoing != null)
                return false;
        } else if (!outgoing.equals(other.outgoing))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ResourceShape [outgoing=" + outgoing + ", ingoing=" + ingoing
                + "]";
    }

}
