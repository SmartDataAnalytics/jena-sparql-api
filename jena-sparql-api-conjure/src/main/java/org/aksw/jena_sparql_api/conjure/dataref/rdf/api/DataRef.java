package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRef;
import org.apache.jena.rdf.model.Resource;

/**
 * Base class for DataRefs backed by RDF
 * 
 * @author raven
 *
 */
public interface DataRef
	extends Resource, PlainDataRef
{
	<T> T accept2(DataRefVisitor<T> visitor);
}
