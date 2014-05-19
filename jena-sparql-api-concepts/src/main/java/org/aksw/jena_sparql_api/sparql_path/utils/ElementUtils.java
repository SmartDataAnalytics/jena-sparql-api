package org.aksw.jena_sparql_api.sparql_path.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

public class ElementUtils {
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
