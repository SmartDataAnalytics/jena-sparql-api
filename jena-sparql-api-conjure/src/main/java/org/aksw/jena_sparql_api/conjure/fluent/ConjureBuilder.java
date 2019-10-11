package org.aksw.jena_sparql_api.conjure.fluent;

public interface ConjureBuilder {
	ConjureFluent fromUrl(String url);
	ConjureFluent fromVar(String name);

	ConjureFluent union(ConjureFluent ...conjureFluents);
	ConjureFluent coalesce(ConjureFluent ...conjureFluents);
}
