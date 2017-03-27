package org.aksw.jena_sparql_api.rename_vars;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;


public class QueryExecutionFactoryRenameVars
	extends QueryExecutionFactoryDecorator
{
	protected int baseRenameId = 0;

	public QueryExecutionFactoryRenameVars(QueryExecutionFactory decoratee) {
		super(decoratee);
		// TODO Auto-generated constructor stub
	}

	@Override
	public QueryExecution createQueryExecution(Query baseQuery) {
		String baseRename = "v" + (baseRenameId++) + "_";
		Map<Var, Var> varMap = QueryUtils.createRandomVarMap(baseQuery, baseRename);

		Query query = QueryTransformOps.transform(baseQuery, varMap);

		System.out.println("Remapped: " + query);

		// Invert the map (TODO Make this a static util function)
		Map<Var, Var> inverseVarMap = varMap.entrySet().stream()
				.collect(Collectors.toMap(Entry::getValue, Entry::getKey));

		QueryExecution baseQe = super.createQueryExecution(query);
		QueryExecution result = new QueryExecutionRenameVars(baseQe, inverseVarMap);

		return result;
	}
}
