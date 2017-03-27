package org.aksw.jena_sparql_api.convert;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public interface VocabWriter<T> {
    void write(Model model, Resource base, T value);
}
