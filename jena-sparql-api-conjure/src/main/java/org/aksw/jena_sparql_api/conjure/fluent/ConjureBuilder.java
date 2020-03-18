package org.aksw.jena_sparql_api.conjure.fluent;

import java.util.function.Function;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.apache.jena.rdf.model.Model;

public interface ConjureBuilder {
	ConjureContext getContext();

	ConjureFluent fromDataRef(DataRef dataRef);
	
	/**
	 * Lambda based creation which passes in the context's model, so all triples can be
	 * directly added to it
	 *  
	 * @param dataRefFn
	 * @return
	 */
	ConjureFluent fromDataRefFn(Function<? super Model, ? extends DataRef> dataRefFn);

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
