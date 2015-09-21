package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.aksw.commons.collections.SetUtils;

import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadBlock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;

public class AggDatasetGraph
    implements Agg<DatasetGraph>
{
    public QuadPattern quadPattern;

    public AggDatasetGraph(QuadPattern quadPattern) {
        super();
        this.quadPattern = quadPattern;
    }

    @Override
    public AccDatasetGraph createAccumulator() {
        AccDatasetGraph result = new AccDatasetGraph(quadPattern);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        Set<Var> result = SetUtils.asSet(OpVars.mentionedVars(new OpQuadBlock(quadPattern)));
        return result;
    }

    public static AggDatasetGraph create(QuadPattern quadPattern) {
    	AggDatasetGraph result = new AggDatasetGraph(quadPattern);
    	return result;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((quadPattern == null) ? 0 : quadPattern.hashCode());
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
		AggDatasetGraph other = (AggDatasetGraph) obj;
		if (quadPattern == null) {
			if (other.quadPattern != null)
				return false;
		} else if (!quadPattern.equals(other.quadPattern))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AggGraph [template=" + quadPattern + "]";
	}
}
