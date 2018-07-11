package org.aksw.jena_sparql_api.views;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCleanGroupsOfOne;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformApplyElementTransform;

public class ElementTransformPropertyBasedRewrite
	extends ElementTransformTripleBasedRewrite
{

    public Element applyTripleTransform(Triple t) {
        Element result = ElementTransformPropertyBasedRewrite.applyTransform(t, filter, valueSet, varGen);
        return result;
    }
    
    protected Element filter;
    protected ValueSet<Node> valueSet;
    protected Generator<Var> varGen;


    public ElementTransformPropertyBasedRewrite(ValueSet<Node> valueSet) {
        this(valueSet, VarGeneratorImpl2.create("v"));
    }

    public ElementTransformPropertyBasedRewrite(ValueSet<Node> valueSet, Generator<Var> varGen) {
        super();

    	this.valueSet = valueSet;
    	this.filter = new ElementFilter(valueSetToExpr(valueSet));
        this.varGen = varGen;

    }


//    {
//      ?s <skos:foo> [ <skos:val> ?l ]
//      Bind(As ?p)
//    }

    // Constraint: mayEqual(

//    public static Element applyTransform(ElementTriplesBlock el, Element filter, Generator<Var> rootVarGen) {
//        BasicPattern bgp = el.getPattern();
//
//        BasicPattern newPattern = new BasicPattern();
//        List<Element> elements = new ArrayList<Element>(bgp.size());
//        for(Triple triple : bgp) {
//            Generator<Var> varGen = rootVarGen.clone();
//            Element e = applyTransform(triple, filter, varGen);
//            if(e == null) {
//                newPattern.add(triple);
//            } else {
//                elements.add(e);
//            }
//        }
//
//        Iterable<Element> items = newPattern.isEmpty()
//                ? elements
//                : Iterables.concat(Collections.singleton(new ElementTriplesBlock(newPattern)), elements)
//                ;
//
//        Element result = ElementUtils.createElementGroup(items);
//        return result;
//    }


//    public static Element applyTransform(ElementPathBlock el, Element filter, Generator<Var> rootVarGen) {
//        PathBlock bgp = el.getPattern();
//
//        ElementPathBlock newPattern = new ElementPathBlock();
//        List<Element> elements = new ArrayList<Element>(bgp.size());
//        for(TriplePath tp : bgp) {
//            if(tp.isTriple()) {
//                Triple triple = tp.asTriple();
//
//                Generator<Var> varGen = rootVarGen.clone();
//                Element e = applyTransform(triple, filter, varGen);
//                if(e == null) {
//                    newPattern.addTriple(new TriplePath(triple));
//                } else {
//                    elements.add(e);
//                }
//            } else {
//                logger.warn("Triple path expressions not supported");
//                newPattern.addTriple(tp);
//            }
//        }
//
//        Iterable<Element> items = newPattern.isEmpty()
//                ? elements
//                : Iterables.concat(Collections.singleton(newPattern), elements)
//                ;
//
//        Element result = ElementUtils.createElementGroup(items);
//        return result;
//    }


    /**
     *
     * Returns null if no transformation needed to be applied
     *
     * @param triple
     * @param filter
     * @param varGen
     * @return
     */
    public static Element applyTransform(Triple triple, Element filter, ValueSet<Node> valueSet, Generator<Var> varGen) {
        Node p = triple.getPredicate();
//        Node s = triple.getSubject();
//        Node o = triple.getObject();
//
        Var pVar = p.isVariable() ? (Var)p : null; //varGen.next();

        Element result = null;
        if(p.isConcrete()) {
            //BinaryRelation relation = filter.get(p);
            if(filter != null) {
                result = createElementForConcretePredicate(triple, valueSet, varGen);
            }
        }
        else {
            //assert(p.isVariable(), "Expected p to be a variable, but instead was: " + p));
            result = createElementForVariablePredicate(triple, filter, varGen);
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

    public static Element createElementForVariablePredicate(Triple t, Element filter, Generator<Var> varGen) {
        //Triple orig = new Triple(s, pVar, o);
    	Var pVar = (Var)t.getPredicate();

        Relation origRel = new Concept(ElementUtils.createElement(t), pVar);
        Relation joined = origRel.joinOn(pVar).with(new Concept(filter, Vars.p));
        
        Element result = joined.getElement();
        return result;
    }

    /**
     *
     * @param p a concrete predicate
     * @param s
     * @param o
     * @param virtualPredicates
     */
    public static Element createElementForConcretePredicate(Triple t, ValueSet<Node> filter, Generator<Var> varGen) {
    	Node p = t.getPredicate();
    	boolean isAllowed = filter.contains(p);
    	Element result = isAllowed ? ElementUtils.createElement(t) : new ElementFilter(NodeValue.FALSE);
    	return result; 
    }

    public static Query transform(Query query, ValueSet<Node> valueSet, boolean cloneOnChange) {
        Element oldQueryPattern = query.getQueryPattern();
        Element newQueryPattern = transform(oldQueryPattern, valueSet);

        Query result;
        if(oldQueryPattern == newQueryPattern) {
            result = query;
        } else {
            result = cloneOnChange ? query.cloneQuery() : query;
            result.setQueryPattern(newQueryPattern);
        }

        return result;
    }

    public static Element transform(Element element, ValueSet<Node> valueSet) {    	
    	ElementTransformPropertyBasedRewrite elementTransform = new ElementTransformPropertyBasedRewrite(valueSet);
        Element result = ElementTransformer.transform(element, elementTransform, new ExprTransformApplyElementTransform(elementTransform));
        
        ElementTransform t2 = new ElementTransformCleanGroupsOfOne();
        result = ElementTransformer.transform(result, t2, new ExprTransformApplyElementTransform(t2));
        return result;
    }
}
