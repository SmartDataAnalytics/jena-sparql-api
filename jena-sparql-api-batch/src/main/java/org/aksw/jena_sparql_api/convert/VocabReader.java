package org.aksw.jena_sparql_api.convert;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * Reads a value from a given resource that is stored in a certain vocabulary.
 * NULL if no appropriate data was found on the resource.
 *
 * TODO We need the capability to read all values of a certain vocabulary from a model
 *
 *
 * @author raven
 *
 * @param <T>
 */
public interface VocabReader<T> {
    T read(Model model, Resource base);
}
