package org.aksw.jena_sparql_api.views.index;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;

/**
 * Rewrite a query by rewriting its algebra
 * 
 * @author raven
 *
 */
public class QueryRewriterOpRewrite 
	implements QueryRewriter
{
	protected Function<Query, Op> queryToOp;
	protected Function<Op, Op> opRewriter;
	protected Function<Op, Query> opToQuery;	
	
	public QueryRewriterOpRewrite(Function<Query, Op> queryToOp, Function<Op, Op> opRewriter,
			Function<Op, Query> opToQuery) {
		super();
		this.queryToOp = queryToOp;
		this.opRewriter = opRewriter;
		this.opToQuery = opToQuery;
	}

	public Function<Query, Op> getQueryToOp() {
		return queryToOp;
	}

	public Function<Op, Op> getOpRewriter() {
		return opRewriter;
	}

	public Function<Op, Query> getOpToQuery() {
		return opToQuery;
	}

	@Override
	public Query rewrite(Query query) {
		Op op = queryToOp.apply(query);
		Op rewrite = opRewriter.apply(op);
		Query result = opToQuery.apply(rewrite);
		return result;
	}
	
	public static QueryRewriterOpRewrite createDefault(Function<Op, Op> opRewriter) {
		QueryRewriterOpRewrite result = new QueryRewriterOpRewrite(Algebra::compile, opRewriter, OpAsQuery::asQuery);
		return result;
	}

	/**
	 * Compiles to quad form
	 * 
	 * @param opRewriter
	 * @return
	 */
	public static QueryRewriterOpRewrite createDefaultQuadForm(Function<Op, Op> opRewriter) {
		QueryRewriterOpRewrite result = new QueryRewriterOpRewrite(
				(op) -> Algebra.toQuadForm(Algebra.compile(op)),
				opRewriter,
				OpAsQuery::asQuery);
		return result;
	}
}
