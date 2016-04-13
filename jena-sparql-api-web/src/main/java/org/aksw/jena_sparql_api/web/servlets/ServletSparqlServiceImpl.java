package org.aksw.jena_sparql_api.web.servlets;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
//@Path("/sparql")
@Path("/")
public class ServletSparqlServiceImpl
    extends ServletSparqlServiceBase
{
    @Autowired
    protected SparqlServiceFactory ssf;

    @Autowired(required=false)
    protected SparqlStmtParser sparqlStmtParser;

    public ServletSparqlServiceImpl() {
        ssf = null;
    }

    @Override
    protected SparqlServiceFactory getSparqlServiceFactory() {
        return ssf;
    }

    @Override
    protected SparqlStmtParser getSparqlStmtParser() {
        SparqlStmtParser result = sparqlStmtParser != null ? sparqlStmtParser : super.getSparqlStmtParser();
        return result;
    };

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response executeRequestXml()
            throws Exception {

        InputStream r = new ClassPathResource("snorql/index.html").getInputStream();
        Response result;
        if(r == null) {
            result = Response.ok("SPARQL HTML front end not configured", MediaType.TEXT_HTML).build();
        } else {
            result = Response.ok(r, MediaType.TEXT_HTML).build();
        }

//        Response result = Response.te
//        Response result = Response.status(Status.NOT_FOUND).build();
        return result;
    }
}
