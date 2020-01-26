package org.aksw.jena_sparql_api.conjure.fluent;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;

public interface ConjureBuilder {
	ConjureContext getContext();

	ConjureFluent fromDataRef(DataRef dataRef);
	ConjureFluent fromUrl(String url);
	ConjureFluent fromVar(String name);
	ConjureFluent fromEmptyModel();

	ConjureFluent seq(ConjureFluent ...conjureFluents);
	ConjureFluent union(ConjureFluent ...conjureFluents);
	ConjureFluent coalesce(ConjureFluent ...conjureFluents);

	
	ConjureFluent call(String macroName, ConjureFluent ...conjureFluents);

	//ConjureFluent when(String condition, ConjureFluent subFluent);
	//ConjureFluent when(String condition, ConjureFluent subFluent);
	//ConjureFluent when(String condition, ConjureFluent subFluent);
	
}
