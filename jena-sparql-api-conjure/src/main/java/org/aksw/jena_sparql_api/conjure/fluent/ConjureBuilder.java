package org.aksw.jena_sparql_api.conjure.fluent;

public interface ConjureBuilder {
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
