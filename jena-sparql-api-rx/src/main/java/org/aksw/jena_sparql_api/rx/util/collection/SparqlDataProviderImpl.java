package org.aksw.jena_sparql_api.rx.util.collection;

import java.util.List;

import org.apache.jena.query.SortCondition;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Element;


interface BindingQuery {
	ExprList getExprs();
	Long getLimit();
	Long getOffset();
	List<SortCondition> getSortConditions();
}


class BindingQueryImpl
	implements BindingQuery
{
	protected ExprList exprs;
	protected Long limit;
	protected long offset;
	protected List<SortCondition> sortConditions;
	
	@Override
	public ExprList getExprs() {
		return exprs;
	}
	
	@Override
	public Long getLimit() {
		return limit;
	}

	@Override
	public Long getOffset() {
		return offset;
	}

	@Override
	public List<SortCondition> getSortConditions() {
		return sortConditions;
	}
}


class SparqlBindingNode {
	
}

public class SparqlDataProviderImpl
{
	protected SparqlQueryConnection conn;
	protected Element baseElement;
	protected List<Var> project;
	protected ExprList filter;
	
	
}
