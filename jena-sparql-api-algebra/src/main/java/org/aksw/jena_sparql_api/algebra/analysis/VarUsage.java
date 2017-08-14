package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Multimap;

public class VarUsage {
    protected Set<Var> referencedVars;
    protected Set<Var> nonUnique;
    protected Multimap<Var, Var> varDeps;
    protected Set<Set<Var>> uniqueSets;

    // newly defined vars

    public VarUsage(
            Set<Var> referencedVars,
            Set<Var> nonUnique,
            Multimap<Var, Var> varDeps,
            Set<Set<Var>> uniqueSets) {
        super();
        this.referencedVars = referencedVars;
        this.nonUnique = nonUnique;
        this.varDeps = varDeps;
        this.uniqueSets = uniqueSets;
    }

    public Set<Var> getReferencedVars() {
        return referencedVars;
    }

    public Set<Var> getNonUnique() {
        return nonUnique;
    }

    public Multimap<Var, Var> getVarDeps() {
        return varDeps;
    }

    public Set<Set<Var>> getUniqueSets() {
        return uniqueSets;
    }

    @Override
    public String toString() {
        return "VarUsage[ proj:" + varDeps.keySet() + ", refs: " + referencedVars +
                ", deps: " + varDeps + ", non-uniq: " + nonUnique + ", uniq sets: " + uniqueSets + "]";
    }


    public static Set<Var> getMandatoryVars(VarUsage varUsage) {
        Set<Var> result = new HashSet<Var>();
        result.addAll(varUsage.referencedVars);
        result.addAll(varUsage.varDeps.values());
        return result;
    }

}
