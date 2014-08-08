package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

public class ElementUtils {
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