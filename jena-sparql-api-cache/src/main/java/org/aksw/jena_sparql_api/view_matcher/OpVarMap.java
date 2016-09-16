package org.aksw.jena_sparql_api.view_matcher;

import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

public class OpVarMap
{
    protected Map<Op, Op> opMapping;
    protected Iterable<Map<Var, Var>> varMapping;
    
    public OpVarMap(Map<Op, Op> opMapping,
            Iterable<Map<Var, Var>> varMapping) {
        super();
        this.opMapping = opMapping;
        this.varMapping = varMapping;
    }

    public Map<Op, Op> getOpMapping() {
        return opMapping;
    }

    public Iterable<Map<Var, Var>> getVarMapping() {
        return varMapping;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((opMapping == null) ? 0 : opMapping.hashCode());
		result = prime * result + ((varMapping == null) ? 0 : varMapping.hashCode());
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
		if (opMapping == null) {
			if (other.opMapping != null)
				return false;
		} else if (!opMapping.equals(other.opMapping))
			return false;
		if (varMapping == null) {
			if (other.varMapping != null)
				return false;
		} else if (!varMapping.equals(other.varMapping))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OpVarMap [opMapping=" + opMapping + ", varMapping=" + Iterables.toString(varMapping) + "]";
	}
}
