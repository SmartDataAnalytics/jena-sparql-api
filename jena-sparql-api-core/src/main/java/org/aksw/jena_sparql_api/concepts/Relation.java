package org.aksw.jena_sparql_api.concepts;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;

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

    public Var getSourceVar() {
        return sourceVar;
    }

    public Var getTargetVar() {
        return targetVar;
    }

    public Element getElement() {
        return element;
    }

    public Relation reverse()
    {
        Relation result = new Relation(element, targetVar, sourceVar);
        return result;
    }

    public Concept getSourceConcept() {
        Concept result = new Concept(element, sourceVar);
        return result;
    }

    public Concept getTargetConcept() {
        Concept result = new Concept(element, targetVar);
        return result;
    }
    public static Relation create(String elementStr, String sourceVarName,
            String targetVarName) {
        SparqlElementParser parser = SparqlElementParserImpl.create(Syntax.syntaxARQ, null);
        Relation result = create(elementStr, sourceVarName, targetVarName, parser);
        return result;
    }

    public static Relation create(String elementStr, String sourceVarName,
            String targetVarName, Function<String, ? extends Element> elementParser) {
        Var sourceVar = Var.alloc(sourceVarName);
        Var targetVar = Var.alloc(targetVarName);

        String tmp = elementStr.trim();
        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
        if (!isEnclosed) {
            tmp = "{" + tmp + "}";
        }

        Element element = elementParser.apply(tmp);//ParserSPARQL10.parseElement(tmp);

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


    public Set<Var> getVarsMentioned() {
        Set<Var> result = SetUtils.asSet(PatternVars.vars(element));
        result.add(sourceVar);
        result.add(targetVar);
        return result;
    }

    public Relation applyNodeTransform(NodeTransform nodeTransform) {
        Var s = VarUtils.applyNodeTransform(sourceVar, nodeTransform);
        Var t = VarUtils.applyNodeTransform(targetVar, nodeTransform);
        Element e = ElementUtils.applyNodeTransform(element, nodeTransform);

        Relation result = new Relation(e, s, t);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result
                + ((sourceVar == null) ? 0 : sourceVar.hashCode());
        result = prime * result
                + ((targetVar == null) ? 0 : targetVar.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Relation other = (Relation) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (sourceVar == null) {
            if (other.sourceVar != null)
                return false;
        } else if (!sourceVar.equals(other.sourceVar))
            return false;
        if (targetVar == null) {
            if (other.targetVar != null)
                return false;
        } else if (!targetVar.equals(other.targetVar))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = sourceVar + " " + targetVar + " | " + element;
        return result;
        /*
        return "Relation [sourceVar=" + sourceVar + ", targetVar=" + targetVar
                + ", element=" + element + "]";
         */
    }

    /**
     * An empty relation does is equivalent to a zero-length path, i.e.
     * it navigates from a set of resources to the same set of resources.
     *
     * It is expressed as an empty graph pattern (ElementGroup), and
     * equal variables in source and target;
     *
     * @return
     */
    public static boolean isEmpty(Relation relation) {
        boolean result;

        Element e = relation.getElement();
        if(e instanceof ElementGroup) {
            ElementGroup g = (ElementGroup)e;
            result = g.getElements().isEmpty();

            //relation.getSourceVar().equals(relation.getTargetVar())
        } else {
            result = false;
        }

        return result;
    }

}
