package org.aksw.jena_sparql_api.shape.api;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;

public class ElementService
    extends Element1
{
    protected SparqlServiceReference service; // Optional service from which to resolve the relation assertions

    public ElementService(Element subElement) {
        super(subElement);
    }

    public SparqlServiceReference getService() {
        return service;
    }


}
