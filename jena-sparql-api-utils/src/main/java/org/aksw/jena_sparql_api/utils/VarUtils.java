package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

public class VarUtils {

    public static Var parseVar(String str) {
        // Remove leading ? of the varName
        String varName = str.trim();
        if(varName.charAt(0) != '?') {
            throw new RuntimeException("var name must start with '?'");
        }
        varName = varName.substring(1);

        Var result = Var.alloc(varName);
        return result;
    }


    /**
     * Create a generator which yields fresh variables that is not contained in the array 'vars'.
     * The new var name will have the given prefix
     *
     */
    public static Generator<Var> createVarGen(String prefix, Collection<Var> excludeVars) {
        prefix = prefix == null ? "v" : prefix;

        Generator<Var> result = VarGeneratorBlacklist.create(prefix, excludeVars);
        //      Set<//Var> excludeVarNames = getVarNames(excludeVars);
        //var generator = GenSym.create(prefix);
        //var genVarName = new GeneratorBlacklist(generator, excludeVarNames);

        //var result = new VarGen(genVarName);

        return result;
    }

    /**
     * Returns a list of variable names as strings for a given iterable of Var objects.
     *
     * @param vars
     * @return
     */
    public static List<String> getVarNames(Iterable<Var> vars) {
        List<String> result = new ArrayList<String>();

        for(Var var : vars) {
            result.add(var.getName());
        }

        return result;
    }

    public static List<Var> toList(Collection<String> varNames) {
        List<Var> result = new ArrayList<Var>(varNames.size());
        for(String varName : varNames) {
            Var var = Var.alloc(varName);
            result.add(var);
        }

        return result;
    }

    public static Set<Var> toSet(Collection<String> varNames) {
        Set<Var> result = new HashSet<Var>();
        for(String varName : varNames) {
            Var var = Var.alloc(varName);
            result.add(var);
        }

        return result;
    }


    public static List<String> map(Collection<String> varNames, Map<Var, Var> varMap) {
        List<String> result = new ArrayList<String>(varNames.size());
        for(String varName : varNames) {
            Var sourceVar = Var.alloc(varName);
            Var targetVar = varMap.get(sourceVar);

            if(targetVar == null) {
                targetVar = sourceVar;
            }

            String targetVarName = targetVar.getVarName();
            result.add(targetVarName);
        }

        return result;
    }

    public static Var applyNodeTransform(Var var, NodeTransform nodeTransform) {
        Var result = applyNodeTransform(var, nodeTransform, var);
        return result;
    }

    public static Var applyNodeTransform(Var var, NodeTransform nodeTransform, Var defaultVar) {
        Var tmp = (Var)nodeTransform.apply(var);
        Var result = tmp == null ? defaultVar : tmp;
        return result;
    }

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

    public static Map<Var, Var> createJoinVarMap(Collection<Var> sourceVars, Collection<Var> targetVars, List<Var> sourceJoinVars, List<Var> targetJoinVars, Generator<Var> generator) {

        if (sourceJoinVars.size() != targetJoinVars.size()) {
            throw new RuntimeException("Cannot join on different number of columns");
        }

        Map<Var, Var> result = VarUtils.createDistinctVarMap(sourceVars, targetVars, true, generator);

        for (int i = 0; i < sourceJoinVars.size(); ++i) {
            Var sourceJoinVar = sourceJoinVars.get(i);
            Var targetJoinVar = targetJoinVars.get(i);

            // Map targetVar to sourceVar
            result.put(targetJoinVar, sourceJoinVar);
            // rename[targetVar.getName()] = sourceVar;
        }

        return result;
    }




}