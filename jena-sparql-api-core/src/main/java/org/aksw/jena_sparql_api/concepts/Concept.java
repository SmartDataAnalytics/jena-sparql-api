package org.aksw.jena_sparql_api.concepts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.lang.ParserSPARQL10;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

/**
 * A concept combines a SPARQL graph pattern (element) with a variable.
 *
 *
 * @author raven
 *
 */
public class Concept {
    private Element element;//List<Element> elements;
    private Var var;

    /**
     * Util method to parse strings that use a pipe as a separator between variable and sparql string
     * ?s | ?s a ex:Airport
     *
     * @param str
     * @return
     */
    public static Concept parse(String str) {
        Concept result = parse(str, null);
        return result;
    }

    public static Concept parse(String str, PrefixMapping pm) {
        String[] splits = str.split("\\|", 2);
        if(splits.length != 2) {
            throw new RuntimeException("Invalid string: " + str);

        }

        // Remove leading ? of the varName
        String varName = splits[0].trim();
        if(varName.charAt(0) != '?') {
            throw new RuntimeException("var name must start with '?'");
        }
        varName = varName.substring(1);

        Concept result = create(splits[1], varName);
        return result;
    }

    public static Concept create(String elementStr, String varName) {
        Concept result = create(elementStr, varName, null);
        return result;
    }

    public static Concept create(String elementStr, String varName, PrefixMapping prefixMapping) {
        Var var = Var.alloc(varName);

        String tmp = elementStr.trim();
        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
        if(!isEnclosed) {
            tmp = "{" + tmp + "}";
        }

        Element element = ParserSPARQL10.parseElement(tmp);
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

    /**
     * True if the concept is isomorph to { ?s ?p ?o }, ?s
     *
     * @return
     */
    public boolean isSubjectConcept() {
        if(element instanceof ElementTriplesBlock) {
            List<Triple> triples = ((ElementTriplesBlock)element).getPattern().getList();

            if(triples.size() == 1) {

                Triple triple = triples.get(0);

                // TODO Refactor into e.g. ElementUtils.isVarsOnly(element)
                boolean condition =
                        triple.getSubject().isVariable() &&
                        triple.getSubject().equals(var) &&
                        triple.getPredicate().isVariable() &&
                        triple.getObject().isVariable();

                if(condition) {
                    return true;
                }
            }
        }

        return false;
    }

    public Concept applyNodeTransform(NodeTransform nodeTransform) {
        Var tmpVar = (Var)nodeTransform.convert(var);

        Element e = ElementUtils.applyNodeTransform(element, nodeTransform);
        Var v = tmpVar == null ? var : tmpVar;

        Concept result = new Concept(e, v);
        return result;
    }

    public Set<Var> getVarsMentioned() {
        Set<Var> result = SetUtils.asSet(PatternVars.vars(element));
        result.add(var); // Var should always be part of element - but better add it here explicitly
        return result;
    }

    public Concept(Element element, Var var) {
        super();
        this.element = element;
        this.var = var;
    }

    public Concept(List<Element> elements, Var var) {
        ElementGroup group = new ElementGroup();

        for(Element item : elements) {
            if(item instanceof ElementTriplesBlock) {
                ElementTriplesBlock tmp = (ElementTriplesBlock)item;
                for(Triple t : tmp.getPattern()) {
                    group.addTriplePattern(t);
                }
            } else {
                group.addElement(item);
            }
        }

        this.element = group;
        this.var = var;
    }


    public Element getElement() {
        return element;
    }

    public List<Element> getElements() {
        return ElementUtils.toElementList(element);
    }

    public Var getVar() {
        return var;
    }


    /**
     * Create a new concept that has no variables with the given one in common
     *
     *
     *
     * @param that
     * @return
     */
    public Concept makeDistinctFrom(Concept that) {

        Set<String> thisVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(this.getElement())));
        Set<String> thatVarNames = new HashSet<String>(VarUtils.getVarNames(PatternVars.vars(that.getElement())));

        Set<String> commonVarNames = Sets.intersection(thisVarNames, thatVarNames);
        Set<String> combinedVarNames = Sets.union(thisVarNames, thatVarNames);

        GeneratorBlacklist generator = new GeneratorBlacklist(Gensym.create("v"), combinedVarNames);

        BindingHashMap binding = new BindingHashMap();
        for(String varName : commonVarNames) {
            Var oldVar = Var.alloc(varName);
            Var newVar = Var.alloc(generator.next());

            binding.add(oldVar, newVar);
        }

        Op op = Algebra.compile(this.element);
        Op substOp = Substitute.substitute(op, binding);
        Query tmp = OpAsQuery.asQuery(substOp);

        //Element newElement = tmp.getQueryPattern();
        ElementGroup newElement = new ElementGroup();
        newElement.addElement(tmp.getQueryPattern());

        /*
        if(newElement instanceof ElementGroup) {


            ElementPathBlock) {
        }
            List<TriplePath> triplePaths = ((ElementPathBlock)newElement).getPattern().getList();

            ElementTriplesBlock block = new ElementTriplesBlock();
            for(TriplePath triplePath : triplePaths) {
                block.addTriple(triplePath.asTriple());
            }

            newElement = block;
            //newElement = new ElementTriplesBlock(pattern);
        }
        */

        Var tmpVar = (Var)binding.get(this.var);

        Var newVar = tmpVar != null ? tmpVar : this.var;

        Concept result = new Concept(newElement, newVar);
        return result;
    }

    public Query asQuery() {
        Query result = new Query();
        result.setQuerySelectType();

        result.setQueryPattern(element);
        result.setDistinct(true);
        result.getProjectVars().add(var);

        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + ((var == null) ? 0 : var.hashCode());
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
        Concept other = (Concept) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (var == null) {
            if (other.var != null)
                return false;
        } else if (!var.equals(other.var))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Concept [element=" + element + ", var=" + var + "]";
    }
}
