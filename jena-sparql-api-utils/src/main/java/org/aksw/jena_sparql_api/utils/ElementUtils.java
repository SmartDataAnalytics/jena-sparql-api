package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

public class ElementUtils {
    public static Element substituteNodes(Element element, Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = new NodeTransformRenameMap(nodeMap);
        Element result = applyNodeTransform(element, nodeTransform);
        return result;
    }
    
    public static Element applyNodeTransform(Element element, NodeTransform nodeTransform) {
        Op op = Algebra.compile(element);
        Op tmp = NodeTransformLib.transform(nodeTransform, op);
        Query query = OpAsQuery.asQuery(tmp);
        Element result = query.getQueryPattern();
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