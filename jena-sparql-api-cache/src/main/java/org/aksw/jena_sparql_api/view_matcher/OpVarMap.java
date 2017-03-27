package org.aksw.jena_sparql_api.view_matcher;

import java.util.Collections;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

public class OpVarMap
	// extends Entry<Map<Op, Op>, Iterable<Map<Var, Var>>>
{
    protected Map<Op, Op> opMap;
    protected Iterable<Map<Var, Var>> varMap;

    public OpVarMap(Map<Op, Op> opMapping,
            Iterable<Map<Var, Var>> varMapping) {
        super();
        this.opMap = opMapping;
        this.varMap = varMapping;
    }

    public OpVarMap(Map<Op, Op> opMapping,
            Map<Var, Var> varMapping) {
        super();
        this.opMap = opMapping;
        this.varMap = Collections.singleton(varMapping);
    }

    public Map<Op, Op> getOpMap() {
        return opMap;
    }

    public Iterable<Map<Var, Var>> getVarMaps() {
        return varMap;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((opMap == null) ? 0 : opMap.hashCode());
		result = prime * result + ((varMap == null) ? 0 : varMap.hashCode());
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
		OpVarMap other = (OpVarMap) obj;
		if (opMap == null) {
			if (other.opMap != null)
				return false;
		} else if (!opMap.equals(other.opMap))
			return false;
		if (varMap == null) {
			if (other.varMap != null)
				return false;
		} else if (!varMap.equals(other.varMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OpVarMap [opMapping=" + opMap + ", varMapping=" + Iterables.toString(varMap) + "]";
	}
}
