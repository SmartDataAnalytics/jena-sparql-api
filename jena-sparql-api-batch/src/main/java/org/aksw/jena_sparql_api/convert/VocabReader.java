package org.aksw.jena_sparql_api.convert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Reads a value from a given resource that is stored in a certain vocabulary.
 * NULL if no appropriate data was found on the resource. 
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public interface VocabReader<T> {
    T read(Model model, Resource base);
}
