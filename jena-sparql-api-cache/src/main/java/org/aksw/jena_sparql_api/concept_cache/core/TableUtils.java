package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.NodeTransform;

public class TableUtils {


	public static Table createTable(ResultSet rs) {

	    List<Var> vars = VarUtils.toList(rs.getResultVars());

	    Table result = TableFactory.create(vars);

	    while(rs.hasNext()) {
	        Binding binding = rs.nextBinding();
	        result.addBinding(binding);
	    }

	    return result;
	}


    public static Table transform(Table table, NodeTransform transform) {
        List<Var> oldVars = table.getVars();

        List<Var> newVars = new ArrayList<Var>(oldVars.size());
        for(Var o : oldVars) {
            Var n = (Var)transform.apply(o);
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
