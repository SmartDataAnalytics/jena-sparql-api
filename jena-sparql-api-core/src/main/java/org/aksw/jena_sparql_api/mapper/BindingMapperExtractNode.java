package org.aksw.jena_sparql_api.mapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


public class BindingMapperExtractNode
    implements BindingMapper<Node>
{
    private Var var;
    
    public BindingMapperExtractNode(Var var) {
        this.var = var;
    }
    
    public Var getVar() {
        return var;
    }
    
    @Override
    public Node map(Binding binding, long rowNum) {
        Node result = binding.get(var);
        return result;
    }
}
