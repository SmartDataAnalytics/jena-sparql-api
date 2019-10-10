package org.aksw.jena_sparql_api.conjure.fluent;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;

public class ConjureFluentImpl	
	implements ConjureFluent
{
	protected Op op;
	
	public ConjureFluentImpl(Op op) {
		super();
		this.op = op;
	}

	public static ConjureFluent wrap(Op op) {
		return new ConjureFluentImpl(op);
	}

	@Override
	public ConjureFluent construct(String queryStr) {
		return ConjureFluentImpl.wrap(OpConstruct.create(op, queryStr));
	}


	@Override
	public ConjureFluent update(String updateRequest) {
		return ConjureFluentImpl.wrap(OpUpdateRequest.create(op, updateRequest));
	}

	@Override
	public Op getOp() {
		return op;
	}

	@Override
	public ConjureFluent hdtHeader() {
		if(op instanceof OpDataRefResource) {
			OpDataRefResource x = ((OpDataRefResource)op);
			PlainDataRef dr = x.getDataRef();
			if(dr instanceof DataRefUrl) {
				DataRefUrl y = ((DataRefUrl)dr);
				y.hdtHeader(true);
			} else {
				throw new RuntimeException("hdtHeader needs to modify a DataRefUrl");
			}
		} else {
			throw new RuntimeException("hdtHeader needs to modify a OpDataRef");
		}
		return this;
	}
}
