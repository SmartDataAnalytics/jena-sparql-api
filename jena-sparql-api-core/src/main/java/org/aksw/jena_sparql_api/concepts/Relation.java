package org.aksw.jena_sparql_api.concepts;

import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.lang.ParserSPARQL10;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

/**
 * This is a binary relation used to relate two concepts to each other
 * 
 * @author raven
 * 
 */
public class Relation {
    private Var sourceVar;
    private Var targetVar;
    private Element element;

    public Relation(Element element, Var sourceVar, Var targetVar) {
        this.element = element;
        this.sourceVar = sourceVar;
        this.targetVar = targetVar;
    }

    public static Relation create(String elementStr, String sourceVarName,
            String targetVarName) {
        Var sourceVar = Var.alloc(sourceVarName);
        Var targetVar = Var.alloc(targetVarName);

        String tmp = elementStr.trim();
        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
        if (!isEnclosed) {
            tmp = "{" + tmp + "}";
        }

        Element element = ParserSPARQL10.parseElement(tmp);

        // TODO Find a generic flatten routine
        if (element instanceof ElementGroup) {
            ElementGroup group = (ElementGroup) element;
            List<Element> elements = group.getElements();
            if (elements.size() == 1) {
                element = elements.get(0);
            }
        }

        Relation result = new Relation(element, sourceVar, targetVar);

        return result;
    }
}
