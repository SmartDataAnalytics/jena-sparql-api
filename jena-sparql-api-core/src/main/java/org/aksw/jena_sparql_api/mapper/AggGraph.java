package org.aksw.jena_sparql_api.mapper;

import java.util.Set;

import org.aksw.commons.collections.SetUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.PatternVars;
import com.hp.hpl.jena.sparql.syntax.Template;

public class AggGraph
    implements Agg<Graph>
{
    public Template template;

    public AggGraph(Template template) {
        super();
        this.template = template;
    }

    @Override
    public AccGraph createAccumulator() {
        AccGraph result = new AccGraph(template);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        BasicPattern bgp = template.getBGP();
        Set<Var> result = SetUtils.asSet(PatternVars.vars(new ElementTriplesBlock(bgp)));
        return result;
    }

    public static AggGraph create(Template template) {
    	AggGraph result = new AggGraph(template);
    	return result;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((template == null) ? 0 : template.hashCode());
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
		AggGraph other = (AggGraph) obj;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AggGraph [template=" + template + "]";
	}
}
