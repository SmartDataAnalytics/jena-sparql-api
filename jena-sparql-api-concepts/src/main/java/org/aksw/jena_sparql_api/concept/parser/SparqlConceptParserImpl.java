package org.aksw.jena_sparql_api.concept.parser;

import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

public class SparqlConceptParserImpl
    implements SparqlConceptParser
{
    private Function<String, Element> elementParser;

    public SparqlConceptParserImpl(Function<String, Element> elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public Concept apply(String conceptStr) {
        Concept result = SparqlConceptParserImpl.parse(conceptStr, elementParser);
        return result;
    }

    public static Concept parse(String conceptStr, Function<String, Element> elementParser) {
        String[] splits = conceptStr.split("\\|", 2);
        if(splits.length != 2) {
            throw new RuntimeException("Invalid string: " + conceptStr);

        }

        // Remove leading ? of the varName
        String varName = splits[0].trim();
        if(varName.charAt(0) != '?') {
            throw new RuntimeException("var name must start with '?'");
        }
        varName = varName.substring(1);

        Concept result = create(splits[1], varName, elementParser);
        return result;
    }

    public static Concept create(String elementStr, String varName, Function<String, Element> elementParser) {
        Var var = Var.alloc(varName);

        String tmp = elementStr.trim();
//        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
//        if(!isEnclosed) {
//            tmp = "{" + tmp + "}";
//        }
//
//        //ParserSparql10 p;
//        tmp = "Select * " + tmp;

        Element element = elementParser.apply(tmp);

        //Element element = ParserSPARQL10.parseElement(tmp);

        //Element element = ParserSPARQL11.parseElement(tmp);

        // TODO Find a generic flatten routine
        if(element instanceof ElementGroup) {
            ElementGroup group = (ElementGroup)element;
            List<Element> elements = group.getElements();
            if(elements.size() == 1) {
                element = elements.get(0);
            }
        }

        Concept result = new Concept(element, var);

        return result;
    }
}
