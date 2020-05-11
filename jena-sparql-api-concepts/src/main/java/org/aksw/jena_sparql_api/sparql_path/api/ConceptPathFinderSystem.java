package org.aksw.jena_sparql_api.sparql_path.api;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.rxjava3.core.Single;

public interface ConceptPathFinderSystem {
    Single<Model> computeDataSummary(SparqlQueryConnection dataConnection);

    ConceptPathFinderFactory<?> newPathFinderBuilder();
}
