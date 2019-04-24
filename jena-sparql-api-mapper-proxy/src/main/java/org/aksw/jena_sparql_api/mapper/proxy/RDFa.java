package org.aksw.jena_sparql_api.mapper.proxy;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

public class RDFa {
	public static final PrefixMapping prefixes = new PrefixMappingImpl()
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/jena-extended.json", Lang.JSONLD))
			.setNsPrefixes(RDFDataMgr.loadModel("rdf-prefixes/rdfa11.json", Lang.JSONLD))
			.lock();
}
