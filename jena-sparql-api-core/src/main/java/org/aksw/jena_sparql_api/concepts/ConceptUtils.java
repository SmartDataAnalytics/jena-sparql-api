package org.aksw.jena_sparql_api.concepts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.PatternVars;

public class ConceptUtils {
    public static Concept listDeclaredProperties = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> || ?t = <http://www.w3.org/2002/07/owl#ObjectProperty> || ?t = <http://www.w3.org/2002/07/owl#DataTypeProperty>)", "s");
    public static Concept listDeclaredClasses = Concept.create("?s a ?t . Filter(?t = <http://www.w3.org/2000/01/rdf-schema#Class> || ?t = <http://www.w3.org/2002/07/owl#Class>)", "s");
    public static Concept listUsedClasses = Concept.create("?s a ?t", "t");
    
    public static Concept listAllPredicates = Concept.create("?s ?p ?o", "p");
    public static Concept listAllGraphs = Concept.create("Graph ?g { ?s ?p ?o }", "g");
    
    public static Set<Var> getVarsMentioned(Concept concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        Set<Var> result = SetUtils.asSet(tmp);
        return result;
    }
    
    public static Map<Var, Var> createDistinctVarMap(Set<Var> workload, Set<Var> blacklist, Generator generator) {
        Set<String> varNames = new HashSet<String>(VarUtils.getVarNames(blacklist));
        Generator gen = new GeneratorBlacklist(generator, varNames);
        
        Map<Var, Var> result = new HashMap<Var, Var>();
        for(Var var : workload) {
            boolean isBlacklisted = blacklist.contains(var); 
            
            Var t;
            if(isBlacklisted) {
                String name = generator.next();
                t = Var.alloc(name);
            } else {
                t = var;
            }
            
            result.put(var, t);
        }
        
        return result;
    }
    
    /**
     * Creates a generator that does not yield variables part of the concept (at the time of creation)
     * @param concept
     * @return
     */
    public static Generator createGenerator(Concept concept) {
        Collection<Var> tmp = PatternVars.vars(concept.getElement());
        List<String> varNames = VarUtils.getVarNames(tmp);
        
        Generator base = Gensym.create("v");
        Generator result = new GeneratorBlacklist(base, varNames); 

        return result;
    }
    
    // Create a fresh var that is not part of the concept
    public static Var freshVar(Concept concept) {
        Generator gen = createGenerator(concept);
        String varName = gen.next();
        Var result = Var.alloc(varName);
        return result;
    }
    
    public static Concept renameVar(Concept concept, Var targetVar) {        
        
        Concept result;
        if(concept.getVar().equals(targetVar)) {
            // Nothing to do since we are renaming the variable to itself
            result = concept;
        } else {
            // We need to rename the concept's var, thereby we need to rename
            // any occurrences of targetVar
            Set<Var> conceptVars = getVarsMentioned(concept);
            Map<Var, Var> varMap = createDistinctVarMap(conceptVars, Collections.singleton(targetVar), Gensym.create("v"));
            varMap.put(concept.getVar(), targetVar);
            Element replElement = ElementUtils.substituteNodes(concept.getElement(), varMap);
            Var replVar = varMap.get(concept.getVar());
            result = new Concept(replElement, replVar);            
        }

        return result;
    }
    
    /**
     * Select Distinct ?g { Graph ?g { ?s ?p ?o } }
     * 
     * @return
     */
    /*
    public static Concept listGraphs() {

        Triple triple = new Triple(Vars.s, Vars.p, Vars.o);
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        
        ElementGroup group = new ElementGroup();
        group.addTriplePattern(triple);

        ElementNamedGraph eng = new ElementNamedGraph(Vars.g, group);

        Concept result = new Concept(eng, Vars.g);
        return result;
    }
    */

}
