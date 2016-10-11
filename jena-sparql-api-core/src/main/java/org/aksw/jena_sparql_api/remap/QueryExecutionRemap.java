package org.aksw.jena_sparql_api.remap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.utils.BindingUtils;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class QueryExecutionRemap
	extends QueryExecutionDecorator
{
	protected Map<Var, Var> varMap;

	public QueryExecutionRemap(QueryExecution decoratee, Map<Var, Var> varMap) {
		super(decoratee);
		this.varMap = varMap;
	}

	@Override
	public ResultSet execSelect() {
		ResultSet rs = super.execSelect();

		List<String> varNames = rs.getResultVars();
		Iterator<Binding> it = ResultSetUtils.toIteratorBinding(rs);
		Iterator<Binding> iu = Iterators.transform(it, b -> BindingUtils.rename(b, varMap));

		ResultSet result = ResultSetUtils.create(varNames, iu);
		return result;
	}
}
