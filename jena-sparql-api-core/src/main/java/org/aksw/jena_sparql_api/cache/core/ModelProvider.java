package org.aksw.jena_sparql_api.cache.core;

import org.apache.jena.rdf.model.Model;

// TODO Replace with Factory1<Model>
public interface ModelProvider
{
    Model getModel();
}
