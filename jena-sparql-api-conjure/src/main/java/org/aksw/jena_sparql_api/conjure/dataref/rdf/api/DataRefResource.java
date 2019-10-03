package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.apache.jena.rdf.model.Resource;

/**
 * Base class for DataRefs backed by RDF
 * 
 * @author raven
 *
 */
public interface DataRefResource
	extends Resource, DataRef
{
	<T> T accept2(DataRefResourceVisitor<T> visitor);
}
