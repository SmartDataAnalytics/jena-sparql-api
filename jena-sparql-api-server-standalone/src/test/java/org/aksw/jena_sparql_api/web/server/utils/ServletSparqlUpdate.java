package org.aksw.jena_sparql_api.web.server.utils;

import javax.ws.rs.Path;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.web.servlets.ServletSparqlUpdateBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Path("/sparql")
public class ServletSparqlUpdate
    extends ServletSparqlUpdateBase
{
    @Autowired
    private SparqlServiceFactory ssf;

    @Override
    protected SparqlServiceFactory getSparqlServiceFactory() {
        return ssf;
    }

}
