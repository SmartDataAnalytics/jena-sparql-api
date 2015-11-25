package org.aksw.jena_sparql_api.convert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public interface VocabWriter<T> {
    void write(Model model, Resource base, T value);
}
