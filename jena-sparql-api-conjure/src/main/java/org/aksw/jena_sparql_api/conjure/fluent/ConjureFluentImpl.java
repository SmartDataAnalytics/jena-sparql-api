package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.Objects;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;

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
	public ConjureFluent construct(String queryStr) {
		String validatedString = context.getSparqlStmtParser().apply(queryStr).toString();
		return wrap(OpConstruct.create(context.getModel(), op, validatedString));
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
}
