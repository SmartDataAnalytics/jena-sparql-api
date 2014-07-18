package org.aksw.jena_sparql_api.geo;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.Accumulator;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class AccLiteral
    implements Accumulator
{
    private NodeValue value = null;
    private int i = 0;
    private BindingMapper<NodeValue> bindingMapper;
    
    public AccLiteral(BindingMapper<NodeValue> bindingMapper) {
        this.bindingMapper = bindingMapper;
    }
    
    @Override
    public void accumulate(Binding binding, FunctionEnv functionEnv) {
        NodeValue node = bindingMapper.map(binding, i++);
        
        value = node;
    }

    @Override
    public NodeValue getValue() {
        //NodeValue result = NodeValue.makeNode(value);
        //return result;
        return value;
    }
}