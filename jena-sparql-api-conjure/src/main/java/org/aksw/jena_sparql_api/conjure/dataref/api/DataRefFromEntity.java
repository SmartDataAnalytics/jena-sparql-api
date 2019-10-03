package org.aksw.jena_sparql_api.conjure.dataref.api;

import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntity;

/**
 * DataRef from an entity object.
 * An (HTTP)Entity combines a supplier for InputStreams with metadata.
 * This allows for more informed decisions (over e.g. DataRefFromPath) in processing, as content types,
 * encodings, charsets, etc may be provided.
 * 
 * Note, that entities can be backed by Paths.
 * 
 * @author raven
 *
 */
public interface DataRefFromEntity
	extends DataRef
{
	RdfHttpEntity getEntity();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
