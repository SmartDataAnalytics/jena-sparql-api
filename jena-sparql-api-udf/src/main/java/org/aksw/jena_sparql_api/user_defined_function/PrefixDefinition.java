package org.aksw.jena_sparql_api.user_defined_function;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

public interface PrefixDefinition
	extends Resource
{
	@Iri("http://ns.aksw.org/jena/udf/prefix")
	String getPrefix();
	
	@Iri("http://ns.aksw.org/jena/udf/iri")
	Resource getIri();

	default PrefixMapping addTo(PrefixMapping pm) {
		String prefix = getPrefix();
		Resource r = getIri();
		String iri = r.getURI();
		pm.setNsPrefix(prefix, iri);
		return pm;
	}
}
