package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformSubst;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;



public class ElementUtils {

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
        //Op op = Algebra.compile(element);
        //Op tmp = NodeTransformLib.transform(nodeTransform, op);
        //Query query = OpAsQuery.asQuery(tmp);
        ElementTransformSubst transform = new ElementTransformSubst(nodeTransform);
        Element result = ElementTransformer.transform(element, transform);
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