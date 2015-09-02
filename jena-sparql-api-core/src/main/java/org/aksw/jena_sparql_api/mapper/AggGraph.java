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

}
