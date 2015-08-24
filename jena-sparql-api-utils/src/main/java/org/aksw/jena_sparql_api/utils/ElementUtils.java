package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;



public class ElementUtils {
    
    /**
     * Returns a map that maps *each* variable from vbs to a name that does not appear in vas.
     * 
     * @param excludeSymmetry if true, exclude mappings from a var in vbs to itself.
     */    
    public static Map<Var, Var> createDistinctVarMap(Collection<Var> vas, Collection<Var> vbs, boolean excludeSymmetry, Generator<Var> generator) {
            //var vans = vas.map(VarUtils.getVarName);
    
        if (generator == null) {
            generator = new VarGeneratorBlacklist(new VarGeneratorImpl(Gensym.create("v")), vas);
        }
    
        // Rename all variables that are in common
        Map<Var, Var> result = new HashMap<Var, Var>();
    
        for(Var oldVar : vbs) {
            Var newVar;
            if (vas.contains(oldVar)) {
                newVar = generator.next();
            } else {
                newVar = oldVar;
            }
    
            boolean isSame = oldVar.equals(newVar);
            if(!(excludeSymmetry && isSame)) {            
                result.put(oldVar, newVar);
            }
        }
    
        return result;
    }
    
    
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