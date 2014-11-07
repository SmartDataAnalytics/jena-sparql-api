import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingWrapped;


public class BindingMapped
    //extends BindingWrapped
    implements Binding
{
    private Binding binding;
    private Map<Var, Var> varMap;

    public BindingMapped(Binding binding, Map<Var, Var> varMap) {
        //super(binding);
        this.binding = binding;
        this.varMap = varMap;
    }

    @Override
    public Iterator<Var> vars() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(Var var) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node get(Var var) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }
}
