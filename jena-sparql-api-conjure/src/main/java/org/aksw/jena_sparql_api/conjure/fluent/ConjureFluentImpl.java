package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.apache.jena.query.Query;

public class ConjureFluentImpl	
	implements ConjureFluent
{
	protected ConjureContext context;
	protected Op op;
	
	public ConjureFluentImpl(ConjureContext context, Op op) {
		super();
		this.context = Objects.requireNonNull(context);
		this.op = Objects.requireNonNull(op);
	}
	
	public ConjureFluent wrap(Op op) {
		return new ConjureFluentImpl(context, op);
	}

	@Override
	public ConjureFluent construct(Collection<String> queryStrs) {
		List<String> validatedStrs = new ArrayList<>();
		for(String str : queryStrs) {
			String validatedStr = context.getSparqlStmtParser().apply(str).toString();
			validatedStrs.add(validatedStr);
		}
		return wrap(OpConstruct.create(context.getModel(), op, validatedStrs));
	}

	@Override
	public ConjureFluent views(Collection<Query> queries) {
		List<String> strs = queries.stream().map(Object::toString).collect(Collectors.toList());
		ConjureFluent result = wrap(OpQueryOverViews.create(context.getModel(), op, strs));
		return result;
	}

	@Override
	public ConjureFluent views(String... queryStrs) {
		List<String> validatedStrings = new ArrayList<>(); 
		for(String queryStr : queryStrs) {
			String tmp = context.getSparqlStmtParser().apply(queryStr).toString();
			validatedStrings.add(tmp);
		}
		
		ConjureFluent result = wrap(OpQueryOverViews.create(context.getModel(), op, validatedStrings));
		return result;
	}


	@Override
	public ConjureFluent update(String updateRequest) {
		String validatedString = context.getSparqlStmtParser().apply(updateRequest).toString();
		return wrap(OpUpdateRequest.create(context.getModel(), op, validatedString));
	}

	@Override
	public Op getOp() {
		return op;
	}

	@Override
	public ConjureFluent hdtHeader() {
		return wrap(OpHdtHeader.create(context.getModel(), op));

//		if(op instanceof OpDataRefResource) {
//			OpDataRefResource x = ((OpDataRefResource)op);
//			PlainDataRef dr = x.getDataRef();
//			if(dr instanceof DataRefUrl) {
//				DataRefUrl y = ((DataRefUrl)dr);
//				y.hdtHeader(true);
//			} else {
//				throw new RuntimeException("hdtHeader needs to modify a DataRefUrl");
//			}
//		} else {
//			throw new RuntimeException("hdtHeader needs to modify a OpDataRef");
//		}
//		return this;
	}

	@Override
	public ConjureFluent cache() {
		return wrap(OpPersist.create(context.getModel(), op));
	}

	@Override
	public ConjureFluent set(String ctxVarName, String selector, String path) {
		String parsedSelctor = context.getSparqlStmtParser().apply(selector).toString();
		// TODO Parse path
		return wrap(OpSet.create(context.getModel(), op, ctxVarName, null, parsedSelctor, path));
	}

	@Override
	public ConjureFluent stmts(Collection<String> stmtStrs) {
		List<String> validatedStrs = new ArrayList<>();
		for(String str : stmtStrs) {
			String validatedStr = context.getSparqlStmtParser().apply(str).toString();
			validatedStrs.add(validatedStr);
		}
		return wrap(OpStmtList.create(context.getModel(), op, validatedStrs));
	}
}
