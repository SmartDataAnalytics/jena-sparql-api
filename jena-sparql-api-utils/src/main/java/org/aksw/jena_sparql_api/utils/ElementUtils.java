package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformSubst;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ExprTransformNodeElement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprTransform;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.PatternVars;



public class ElementUtils {

    public static Map<Node, Var> createMapFixVarNames(Element element) {
        Collection<Var> vars = PatternVars.vars(element);
        //Set<Var> vars = NodeUtils.getVarsMentioned(nodes);
        //Set<Node> bnodes = NodeUtils.getBnodesMentioned(vars);
        Generator<Var> gen = VarGeneratorBlacklist.create("v", vars);

        Map<Node, Var> result = new HashMap<Node, Var>();
//        for(Node node : bnodes) {
//            result.put(node, gen.next());
//        }
        for(Var var : vars) {
            if(var.getName().startsWith("?")) {
                result.put(var, gen.next());
            }
            //System.out.println(var);
        }

        return result;
    }

    public static Element fixVarNames(Element element) {
        Map<Node, Var> nodeMap = createMapFixVarNames(element);

        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Element result = ElementUtils.applyNodeTransform(element, nodeTransform);

        return result;
    }

    public static Element toElement(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementGroup e = new ElementGroup();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static Element union(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementUnion e= new ElementUnion();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static ElementGroup createElementGroup(Iterable<Element> members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static ElementGroup createElementGroup(Element ... members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static Element createRenamedElement(Element element, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Element result = applyNodeTransform(element, nodeTransform);
        return result;
    }

    public static Element applyNodeTransform(Element element, NodeTransform nodeTransform) {
        ElementTransformSubst elementTransform = new ElementTransformSubst(nodeTransform);
        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);
        Element result = ElementTransformer.transform(element, elementTransform, exprTransform);
        return result;
    }

    public static void copyElements(ElementGroup target, Element source) {
        if(source instanceof ElementGroup) {
            ElementGroup es = (ElementGroup)source;

            for(Element e : es.getElements()) {
                target.addElement(e);
            }
        } else {
            target.addElement(source);
        }
    }

    /**
     * Creates a new ElementGroup that contains the elements of the given arguments.
     * Argument ElementGroups are flattened. ElementTriplesBlocks however are not combined.
     *
     * @param first
     * @param second
     * @return
     */
    public static Element mergeElements(Element first, Element second) {
        ElementGroup result = new ElementGroup();

        copyElements(result, first);
        copyElements(result, second);

        return result;
    }

    public static List<Element> toElementList(Element element) {
        List<Element> result;

        if(element instanceof ElementGroup) {
            result = ((ElementGroup)element).getElements();
        } else {
            result = Arrays.asList(element);
        }

        // This method always returns a copy of the elements
        result = new ArrayList<Element>(result);

        return result;
    }

}