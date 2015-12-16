package org.aksw.jena_sparql_api.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformCopyBase;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;

import com.google.common.collect.Iterables;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

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
    protected Map<Node, Relation> virtualPredicates;
    protected Generator<Var> varGen;

    public ElementTransformVirtualPredicates() {
        this(new HashMap<Node, Relation>());
    }

    public ElementTransformVirtualPredicates(Map<Node, Relation> virtualPredicates) {
        super();
        this.virtualPredicates = virtualPredicates;
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
        Element result = applyTransform(el);
        return result;
    }

    // Constraint: mayEqual(

    public static Element applyTransform(ElementTriplesBlock el, Map<Node, Relation> virtualPredicates, Generator<Var> varGen) {
        BasicPattern bgp = el.getPattern();

        BasicPattern newPattern = new BasicPattern();
        List<Element> elements = new ArrayList<Element>(bgp.size());
        for(Triple triple : bgp) {
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
    public static Element applyTransform(ElementPathBlock el) {
        throw new RuntimeException("Not implemented yet");
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
    public static Element applyTransform(Triple triple, Map<Node, Relation> virtualPredicates, Generator<Var> varGen) {
        Node p = triple.getPredicate();

        Node s = triple.getSubject();
        Node o = triple.getObject();

        Var pVar = p.isVariable() ? (Var)p : varGen.next();

        Element result = null;
        if(p.isConcrete()) {
            Relation relation = virtualPredicates.get(p);
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

    public static Element createElementForVariablePredicate(Var pVar, Node s, Node o, Map<Node, Relation> virtualPredicates, Generator<Var> varGen)
    {
        Triple orig = new Triple(s, pVar, o);
        ElementUtils.createElement(orig);

        List<Element> unionMembers = new ArrayList<Element>();

        unionMembers.add(ElementUtils.createElement(new Triple(s, pVar, o)));

        for(Entry<Node, Relation> entry : virtualPredicates.entrySet()) {
            Node pRef = entry.getKey();
            Relation relation = entry.getValue();

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
    public static Element createElementForConcretePredicate(Var pVar, Node pRef, Node s, Node o, Relation relation, Generator<Var> varGen) {
        //Relation relation = virtualProperties.get(pRef);

        ElementBind bind = new ElementBind(pVar, NodeValue.makeNode(pRef));

        Set<Var> vars = relation.getVarsMentioned();
        Map<Node, Node> nodeMap = new HashMap<Node, Node>();
        for(Var var : vars) {
            Var freshVar = varGen.next();
            nodeMap.put(var, freshVar);
        }

        nodeMap.put(relation.getSourceVar(), s);
        nodeMap.put(relation.getTargetVar(), o);
        Element fragment = ElementUtils.createRenamedElement(relation.getElement(), nodeMap);

        ElementGroup group = new ElementGroup();
        group.addElement(fragment);
        group.addElement(bind);


        Element result = group;

        return result;
    }

    public static Query transform(Query query, Map<Node, Relation> virtualPredicates, boolean cloneOnChange) {
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

    public static Element transform(Element element, Map<Node, Relation> virtualPredicates) {
        ElementTransformVirtualPredicates elementTransform = new ElementTransformVirtualPredicates(virtualPredicates);
        Element result = ElementTransformer.transform(element, elementTransform);
        return result;
    }

}
