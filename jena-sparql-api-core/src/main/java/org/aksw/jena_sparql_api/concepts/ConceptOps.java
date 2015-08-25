package org.aksw.jena_sparql_api.concepts;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl;
import org.aksw.jena_sparql_api.utils.VarUtils;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.syntax.Element;

public class ConceptOps {

    public static Concept intersect(Concept concept, Concept filter) {
        
        Concept result;
        
        if(filter != null && !filter.isSubjectConcept()) {
            Set<Var> vas = concept.getVarsMentioned();
            Set<Var> vbs = filter.getVarsMentioned();
            Generator<Var> generator = VarGeneratorImpl.create("v"); 
            Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, generator);
            
            varMap.put(filter.getVar(), concept.getVar());
            
            NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
            
            Concept tmp = filter.applyNodeTransform(nodeTransform);
            
            Element e = ElementUtils.mergeElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
        } else {
            result = concept;
        }
        
        return result;
    }
}
