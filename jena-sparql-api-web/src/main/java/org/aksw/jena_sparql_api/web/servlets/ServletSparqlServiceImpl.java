package org.aksw.jena_sparql_api.web.servlets;

import javax.ws.rs.Path;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


//@Service
//@Path("/sparql")
//public class ServletSparqlServiceImpl
//{
//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public String test() {
//        return "hello";
//    }
//}


@Service
@Path("/sparql")
public class ServletSparqlServiceImpl
    extends ServletSparqlServiceBase
{
    @Autowired
    private SparqlServiceFactory ssf;

    public ServletSparqlServiceImpl() {
        ssf = null;
    }

    @Override
    protected SparqlServiceFactory getSparqlServiceFactory() {
        return ssf;
    }
}
