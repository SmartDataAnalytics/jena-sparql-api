package org.aksw.jena_sparql_api.conjure.fluent;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.apache.jena.query.Query;
import org.apache.jena.update.UpdateRequest;

public interface ConjureFluent {
	Op getOp();
	
	// Not sure if this is the best place
	// hdtHeader is a modifier for a datarefUrl
	ConjureFluent hdtHeader();
	

	ConjureFluent construct(String queryStr);
	
	default ConjureFluent construct(Query query) {
		return construct(query.toString());
	}


	ConjureFluent update(String updateRequest);
	
	default ConjureFluent update(UpdateRequest updateRequest) {
		return update(updateRequest.toString());
	}

	// We could create the queries programmatically in a util function
	// But we will validated them anyway with the parser
	
	default ConjureFluent ofProperty(String p) {
		return construct("CONSTRUCT WHERE { ?s <" + p + "> ?o");
	}
	
	default ConjureFluent tripleCount() {
		return construct("CONSTRUCT { ?s <urn:count> ?o }"
			+ "{ { SELECT (COUNT(*) AS ?c) { ?s ?p ?o } } }");
	}
}
