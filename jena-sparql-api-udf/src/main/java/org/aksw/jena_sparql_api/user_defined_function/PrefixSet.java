package org.aksw.jena_sparql_api.user_defined_function;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

public interface PrefixSet
	extends Resource
{
	@Iri("http://ns.aksw.org/jena/udf/mapping")
	Set<PrefixDefinition> getDefinitions();
	
	default PrefixMapping addTo(PrefixMapping pm) {
		Set<PrefixDefinition> set = getDefinitions();
		for(PrefixDefinition def : set) {
			String prefix = def.getPrefix();
			Resource r = def.getIri();
			String iri = r.getURI();
			pm.setNsPrefix(prefix, iri);
		}
		
		return pm;
	}
}
