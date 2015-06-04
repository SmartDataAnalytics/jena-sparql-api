package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

public class TableUtils {
    public static Table transform(Table table, NodeTransform transform) {
        List<Var> oldVars = table.getVars();

        List<Var> newVars = new ArrayList<Var>(oldVars.size());
        for(Var o : oldVars) {
            Var n = (Var)transform.convert(o);
            newVars.add(n);
        }

        //List<Binding> newBindings = new ArrayList<Binding>(table.size());
        Table result = new TableN(newVars);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding o = it.next();

            Binding n = BindingUtils.transformKeys(o, transform);
            result.addBinding(n);
        }

        return result;
    }

}
