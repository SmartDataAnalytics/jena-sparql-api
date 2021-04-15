package org.aksw.jena_sparql_api.rx.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.CollectionFromTable;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TransformUnionQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class ResultSetMappers {

	/**
	 * Convert a mapper that takes a SparqlQueryConnection as input into one that accepts a
	 * Dataset instead.
	 * 
	 * @param <T>
	 * @param fn
	 * @return
	 */
	public static <T> Function<Dataset, T> wrapForDataset(Function<? super SparqlQueryConnection, T> fn) {
		return dataset -> {
			T result;
			try(SparqlQueryConnection conn = RDFConnectionFactory.connect(dataset)) {
				result = fn.apply(conn);
			}
			return result;
		};
	}

	
	
	public static Function<? super SparqlQueryConnection, Table> createTableMapper(Query rawKeyQuery) {
		// Algebra::unionDefaultGraph was deprecated in jena4 the fallback is TransformUnionQuery::transform
		Query keyQuery = QueryUtils.applyOpTransform(rawKeyQuery, TransformUnionQuery::transform);
	
		List<Var> projectVars = keyQuery.getProjectVars();
	
		Function<? super SparqlQueryConnection, Table> result = conn -> {
			Function<List<Binding>, Table> buffer = SparqlRx.createTableBuffer(projectVars);
			Table table = SparqlRx.execSelectRaw(() -> conn.query(keyQuery))
				.toList()
				.map(buffer::apply)
				.blockingGet();
			return table;
		};
		
		return result;
	}

	public static Function<? super SparqlQueryConnection, Collection<Node>> createMultiNodeMapper(Query rawKeyQuery) {
		List<Var> projectVars = rawKeyQuery.getProjectVars();
		if(projectVars.size() != 1) {
			throw new RuntimeException("Key query must have exactly 1 result var");
		}
	
		Var projectVar = projectVars.get(0);
		Function<Binding, Node> mapper = ResultSetMappers.createNodeMapper(projectVar);
		
		return createTableMapper(rawKeyQuery)
			.andThen(table ->
				new CollectionFromTable<>(table, null, mapper::apply));
	}

	public static Function<? super SparqlQueryConnection, Node> createNodeMapper(Query rawKeyQuery, Node defaultValue) {
		return createMultiNodeMapper(rawKeyQuery)
					.andThen(nodes -> Iterables.getFirst(nodes, defaultValue));
	}

	public static Function<? super SparqlQueryConnection, Collection<List<Node>>> createTupleMapper(Query rawKeyQuery) {
		List<Var> projectVars = rawKeyQuery.getProjectVars();
		Function<Binding, List<Node>> mapper = ResultSetMappers.createTupleMapper(projectVars);
		
		return createTableMapper(rawKeyQuery)
			.andThen(table ->
				new CollectionFromTable<>(table, null, mapper::apply));
	}

	public static Function<Binding, List<Node>> createTupleMapper(List<Var> vars) {
		int n = vars.size();
		return binding -> {
			List<Node> r = new ArrayList<>(n);
			for(Var v : vars) {
				Node node = binding.get(v);
				r.add(node);
			}
			return r;
		};
	}

	public static Function<Binding, Node> createNodeMapper(Var var) {
		return binding -> {
			Node r = binding.get(var);
			return r;
		};
	}

}
