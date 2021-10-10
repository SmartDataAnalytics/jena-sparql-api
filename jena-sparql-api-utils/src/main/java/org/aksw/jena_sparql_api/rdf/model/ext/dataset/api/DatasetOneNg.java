package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import org.apache.jena.query.Dataset;

public interface DatasetOneNg
    extends Dataset
{
    String getGraphName();
}
