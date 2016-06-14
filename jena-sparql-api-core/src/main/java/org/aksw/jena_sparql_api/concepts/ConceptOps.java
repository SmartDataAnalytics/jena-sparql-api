package org.aksw.jena_sparql_api.concepts;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;

public class ConceptOps {

    public static Concept align(Concept base, Concept modifyee){
        Set<Var> vas = base.getVarsMentioned();
        Set<Var> vbs = modifyee.getVarsMentioned();
        Generator<Var> generator = VarGeneratorImpl.create("v");
        Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, generator);

        //if(!filter.getVar().equals(concept.getVar())) {
        varMap.put(modifyee.getVar(), base.getVar());
        //}

        NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);

        Concept result = modifyee.applyNodeTransform(nodeTransform);
        return result;
    }

    public static Concept union(Stream<Concept> concepts) {
        // Empty union results in false

        //concepts.reduce(
    }

    public static Concept intersect(Iterable<Concept> concepts) {

    }

    public static Concept intersect(Concept concept, Concept filter) {

        Concept result;

        if(filter != null && !filter.isSubjectConcept()) {
//            Set<Var> vas = concept.getVarsMentioned();
//            Set<Var> vbs = filter.getVarsMentioned();
//            Generator<Var> generator = VarGeneratorImpl.create("v");
//            Map<Var, Var> varMap = VarUtils.createDistinctVarMap(vas, vbs, true, generator);
//
//            //if(!filter.getVar().equals(concept.getVar())) {
//            varMap.put(filter.getVar(), concept.getVar());
//            //}
//
//            NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
//
//            Concept tmp = filter.applyNodeTransform(nodeTransform);

            Concept tmp = align(concept, filter);
            Element e = ElementUtils.mergeElements(concept.getElement(), tmp.getElement());
            result = new Concept(e, concept.getVar());
        } else {
            result = concept;
        }

        return result;
    }
}
