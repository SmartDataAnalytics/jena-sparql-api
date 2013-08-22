package org.aksw.jena_sparql_api.cache.core;

import com.hp.hpl.jena.rdf.model.Model;

// TODO Replace with Factory1<Model>
public interface ModelProvider
{
    Model getModel();
}
