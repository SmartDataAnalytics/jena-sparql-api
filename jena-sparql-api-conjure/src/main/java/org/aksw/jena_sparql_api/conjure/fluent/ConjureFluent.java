package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

public interface ConjureFluent {
	Op getOp();
	
	// Not sure if this is the best place
	// hdtHeader is a modifier for a datarefUrl
	ConjureFluent hdtHeader();
	
	ConjureFluent cache();
	
	ConjureFluent construct(String queryStr);
	ConjureFluent update(String updateRequest);
	

	ConjureFluent set(String ctxVar, String selector, String path);

	default ConjureFluent construct(Query query) {
		return construct(query.toString());
	}
	
	default ConjureFluent update(UpdateRequest updateRequest) {
		return update(updateRequest.toString());
	}

	// We could create the queries programmatically in a util function
	// But we will validated them anyway with the parser
	
	default ConjureFluent ofProperty(String p) {
		return construct("CONSTRUCT WHERE { ?s <" + p + "> ?o");
	}
	
	
	default ConjureFluent everthing() {
		return construct(QLib.everything());
	}

	
	default ConjureFluent tripleCount() {
		return construct(QLib.tripleCount());
	}
	
	default ConjureFluent compose(Function<? super ConjureFluent, ? extends ConjureFluent> composer) {
		ConjureFluent result = composer.apply(this);
		return result;
	}
}
