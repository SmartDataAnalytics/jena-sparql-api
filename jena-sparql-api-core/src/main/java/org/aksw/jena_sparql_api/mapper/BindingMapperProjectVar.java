package org.aksw.jena_sparql_api.mapper;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;


public class BindingMapperProjectVar
    implements BindingMapperVarAware<Node>
{
    private Var var;

    public BindingMapperProjectVar(Var var) {
        this.var = var;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public Node apply(Binding binding, Long rowNum) {
        Node result = binding.get(var);
        return result;
    }

    public static BindingMapperProjectVar create(Var var) {
        BindingMapperProjectVar result = new BindingMapperProjectVar(var);
        return result;
    }

    @Override
    public Set<Var> getVarsMentioned() {
        return Collections.singleton(var);
    }
}
