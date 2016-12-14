package org.aksw.jena_sparql_api.util.collection;

import java.util.Optional;

/**
 * Generic interface to access associated (aspects / APIs / whatever to call it) of objects.
 * The default implementation of unwrap will try to cast object the method is called on
 * as the requested class.
 *
 *
 * @author raven
 *
 */
public interface Contextual {

	default public <X> Optional<X> unwrap(Class<X> clazz, boolean reflexive) {
    	@SuppressWarnings("unchecked")
		Optional<X> result = reflexive && clazz.isAssignableFrom(this.getClass())
    		? Optional.of((X)this)
    		: this instanceof Delegated
    			? ((Delegated)this).getDelegate().unwrap(clazz, reflexive)
    			: Optional.empty();

    	return result;
	}
}
