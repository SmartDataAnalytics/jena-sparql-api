package org.aksw.jena_sparql_api.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformCopyBase;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.PathBlock;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Virtual properties map to SPARQL binary relations
 *
 * ?s ?o | arbitrarySparqlElementMentioningSAndO
 *
 * Triple pattern: ?s ?p ?o are replaced by a union of all virtual properties
 *
 *  ->
 *    { ?s ?p ?o }
 *  Union
 *    { Select (<virtualPropertyUri> As ?p) { arbitrarySparqlElementMentioningSAndO }
 *
 * If ?p is a constant, we can directly replace it with the virtual property
 *
 *
 *
 * @author raven
 *
 */
public class ElementTransformVirtualPredicates
    extends ElementTransformCopyBase
{
    private static final Logger logger = LoggerFactory.getLogger(ElementTransformVirtualPredicates.class);

    protected Map<Node, BinaryRelation> virtualPredicates;
    protected Generator<Var> varGen;

    public ElementTransformVirtualPredicates() {
        this(new HashMap<Node, BinaryRelation>());
    }

    public ElementTransformVirtualPredicates(Map<Node, BinaryRelation> virtualPredicates) {
        this(virtualPredicates, VarGeneratorImpl2.create("v"));
    }

    public ElementTransformVirtualPredicates(Map<Node, BinaryRelation> virtualPredicates, Generator<Var> varGen) {
        super();
        this.virtualPredicates = virtualPredicates;
        this.varGen = varGen;
    }


    @Override
    public Element transform(ElementTriplesBlock el) {
        Element result = applyTransform(el, virtualPredicates, varGen);
        return result;
    }

//    {
//      ?s <skos:foo> [ <skos:val> ?l ]
//      Bind(As ?p)
//    }

    @Override
    public Element transform(ElementPathBlock el) {
        Element result = applyTransform(el, virtualPredicates, varGen);
        return result;
    }

    // Constraint: mayEqual(

    public static Element applyTransform(ElementTriplesBlock el, Map<Node, BinaryRelation> virtualPredicates, Generator<Var> rootVarGen) {
        BasicPattern bgp = el.getPattern();

        BasicPattern newPattern = new BasicPattern();
        List<Element> elements = new ArrayList<Element>(bgp.size());
        for(Triple triple : bgp) {
            Generator<Var> varGen = rootVarGen.clone();
            Element e = applyTransform(triple, virtualPredicates, varGen);
            if(e == null) {
                newPattern.add(triple);
            } else {
                elements.add(e);
            }
        }

        Iterable<Element> items = newPattern.isEmpty()
                ? elements
                : Iterables.concat(Collections.singleton(new ElementTriplesBlock(newPattern)), elements)
                ;

        Element result = ElementUtils.createElementGroup(items);
        return result;
    }


    public static Element applyTransform(ElementPathBlock el, Map<Node, BinaryRelation> virtualPredicates, Generator<Var> rootVarGen) {
        PathBlock bgp = el.getPattern();

        ElementPathBlock newPattern = new ElementPathBlock();
        List<Element> elements = new ArrayList<Element>(bgp.size());
        for(TriplePath tp : bgp) {
            if(tp.isTriple()) {
                Triple triple = tp.asTriple();

                Generator<Var> varGen = rootVarGen.clone();
                Element e = applyTransform(triple, virtualPredicates, varGen);
                if(e == null) {
                    newPattern.addTriple(new TriplePath(triple));
                } else {
                    elements.add(e);
                }
            } else {
                logger.warn("Triple path expressions not supported");
                newPattern.addTriple(tp);
            }
        }

        Iterable<Element> items = newPattern.isEmpty()
                ? elements
                : Iterables.concat(Collections.singleton(newPattern), elements)
                ;

        Element result = ElementUtils.createElementGroup(items);
        return result;
    }


    /**
     *
     * Returns null if no transformation needed to be applied
     *
     * @param triple
     * @param virtualPredicates
     * @param varGen
     * @return
     */
    public static Element applyTransform(Triple triple, Map<Node, BinaryRelation> virtualPredicates, Generator<Var> varGen) {
        Node p = triple.getPredicate();

        Node s = triple.getSubject();
        Node o = triple.getObject();

        Var pVar = p.isVariable() ? (Var)p : null; //varGen.next();

        Element result = null;
        if(p.isConcrete()) {
            BinaryRelation relation = virtualPredicates.get(p);
            if(relation != null) {
                result = createElementForConcretePredicate(pVar, p, s, o, relation, varGen);
            }
        }
        else {
            //assert(p.isVariable(), "Expected p to be a variable, but instead was: " + p));
            result = createElementForVariablePredicate(pVar, s, o, virtualPredicates, varGen);
        }

        return result;
    }


//    public static Query createQuery(Triple triple, Map<Node, Relation> virtualProperties, Generator<Var> varGen) {
//        Element result;
//        Node p = triple.getPredicate();
//      if(relation != null) {
//      // TODO If the relation is just a single triple pattern, then just replace directly
//      Element
//
//      Query query = new Query();
//      query.setQuerySelectType();
//
//      Node s = triple.getSubject();
//      Node o = triple.getObject();
//
//      if(s.isVariable()) {
//          query.getProject().add((Var)s);
//      }
//
//      query.getProject().add((Var)p);
//
//      if(o.isVariable()) {
//          query.getProject().add((Var)o);
//      }
//
//
//
//
//      result = ElementUtils.createRenamedElement(relation.getElement(), nodeMap);
//}
//
//    }

    public static Element createElementForVariablePredicate(Var pVar, Node s, Node o, Map<Node, BinaryRelation> virtualPredicates, Generator<Var> varGen)
    {
        Triple orig = new Triple(s, pVar, o);
        ElementUtils.createElement(orig);

        List<Element> unionMembers = new ArrayList<Element>();

        unionMembers.add(ElementUtils.createElement(new Triple(s, pVar, o)));

        for(Entry<Node, BinaryRelation> entry : virtualPredicates.entrySet()) {
            Node pRef = entry.getKey();
            BinaryRelation relation = entry.getValue();

            Element e = createElementForConcretePredicate(pVar, pRef, s, o, relation, varGen);
            unionMembers.add(e);
        }

        Element result = ElementUtils.union(unionMembers);
        return result;
    }
    /**
     *
     * @param p a concrete predicate
     * @param s
     * @param o
     * @param virtualPredicates
     */
    public static Element createElementForConcretePredicate(Var pVar, Node pRef, Node s, Node o, BinaryRelation relation, Generator<Var> varGen) {
        //Relation relation = virtualProperties.get(pRef);

        Var sourceVar = relation.getSourceVar();
        Var targetVar = relation.getTargetVar();

        ElementBind bind = pVar == null ? null : new ElementBind(pVar, NodeValue.makeNode(pRef));

        Set<Var> vars = relation.getVarsMentioned();
        Map<Node, Node> nodeMap = new HashMap<Node, Node>();

        List<Var> skip = Arrays.asList(sourceVar, targetVar);
        for(Var var : vars) {
            if(!skip.contains(var)) {
                Var freshVar = varGen.next();
                nodeMap.put(var, freshVar);
            }
        }

        nodeMap.put(sourceVar, s);
        nodeMap.put(targetVar, o);
        
//        System.out.println("Relation: " + relation);
//        System.out.println("NodeMap: " + nodeMap);
        Element fragment = ElementUtils.createRenamedElement(relation.getElement(), nodeMap);
        Element result = ElementUtils.groupIfNeeded(fragment, bind);

        return result;
    }

    public static Query transform(Query query, Map<Node, BinaryRelation> virtualPredicates, boolean cloneOnChange) {
        Element oldQueryPattern = query.getQueryPattern();
        Element newQueryPattern = transform(oldQueryPattern, virtualPredicates);

        Query result;
        if(oldQueryPattern == newQueryPattern) {
            result = query;
        } else {
            result = cloneOnChange ? query.cloneQuery() : query;
            result.setQueryPattern(newQueryPattern);
        }

        return result;
    }

    public static Element transform(Element element, Map<Node, BinaryRelation> virtualPredicates) {
        ElementTransformVirtualPredicates elementTransform = new ElementTransformVirtualPredicates(virtualPredicates);
        Element result = ElementTransformer.transform(element, elementTransform, new ExprTransformCopy(false));
        return result;
    }

}
