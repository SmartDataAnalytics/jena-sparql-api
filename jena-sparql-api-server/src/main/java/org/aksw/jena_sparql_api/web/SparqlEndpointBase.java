package org.aksw.jena_sparql_api.web;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionAndType;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;


/**
 * Jersey resource for an abstract SPARQL endpoint based on the AKSW SPARQL API.
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public abstract class SparqlEndpointBase {

    private @Context HttpServletRequest req;


    @Deprecated
    public QueryExecution createQueryExecution(Query query, @Context HttpServletRequest req) {
        QueryExecutionAndType tmp = createQueryExecution(query.toString());
        QueryExecution result = tmp.getQueryExecution();
        return result;
    }

    // IMPORTANT: You only need to override either this or the following method

    public QueryExecution createQueryExecution(Query query) {
        throw new RuntimeException("Not implemented");
    }


    /**
     * Override this for special stuff, such as adding the EXPLAIN keyword
     *
     * @param queryString
     * @return
     */
    public QueryExecutionAndType createQueryExecution(String queryString) {
        //Query query = new Query();
        //query.setPrefix("bif", "http://www.openlinksw.com/schemas/bif#");

        //QueryFactory.parse(query, queryString, "http://example.org/base-uri/", Syntax.syntaxSPARQL_11);
        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);


        QueryExecution qe = createQueryExecution(query);

        QueryExecutionAndType result = new QueryExecutionAndType(qe, query.getQueryType());

        return result;
    }


    public Response processQuery(HttpServletRequest req, String queryString, String format) throws Exception {
        StreamingOutput so = processQueryToStreaming(queryString, format);
        Response response = Response.ok(so).build();
        return response;
    }

    public StreamingOutput processQueryToStreaming(String queryString, String format)
            throws Exception
    {
        QueryExecutionAndType qeAndType = createQueryExecution(queryString);

        StreamingOutput result = ProcessQuery.processQuery(qeAndType, format);
        return result;
    }




//    @GET
//    @Produces(MediaType.APPLICATION_XML)
//    public Response executeQueryXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//
//        if(queryString == null) {
//            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//            return Response.status(Status.BAD_REQUEST).entity(so).build(); // TODO: Return some error HTTP code
//        }
//
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
//    }


    @GET
    @Produces(MediaType.APPLICATION_XML)
    public void executeQueryXml(@Suspended final AsyncResponse asyncResponse, @QueryParam("query") String queryString) {
            //throws Exception {

        if(queryString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
        } else {
            processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_XML);
        }

        //return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
    }

    //@Produces(MediaType.APPLICATION_XML)
//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    public Response executeQueryXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
//            throws Exception {
//
//        if(queryString == null) {
//            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//            return Response.ok(so).build(); // TODO: Return some error HTTP code
//        }
//
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
//    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void executeQueryXmlPostAsync(@Suspended final AsyncResponse asyncResponse, @FormParam("query") String queryString) {
            //throws Exception {

        if(queryString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            //.entity("Connection Callback").build());

            asyncResponse.resume(Response.ok(so).build()); // TODO: Return some error HTTP code
        }

        //return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_XML);
    }



//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
//    public Response executeQueryJson(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
//    }


  @GET
  @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
    public void executeQueryJson(@Suspended final AsyncResponse asyncResponse, @QueryParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_Json);
    }








//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
//    public Response executeQueryJsonPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
//    }





    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
    public void executeQueryXmlPost(@Suspended final AsyncResponse asyncResponse, @FormParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_Json);
    }

    public void processQueryAsync(final AsyncResponse asyncResponse, String queryString, final String format) {
        final QueryExecutionAndType qeAndType = createQueryExecution(queryString);


        asyncResponse
        .register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse arg0) {

                qeAndType.getQueryExecution().abort();

                System.out.println("DISCONNECT");
                if(true) {
                asyncResponse.resume(
                    Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Connection Callback").build());
                } else {
                    asyncResponse.cancel();
                }
            }
        });

//        asyncResponse
//        .setTimeoutHandler(new TimeoutHandler() {
//           @Override
//           public void handleTimeout(AsyncResponse asyncResponse) {
//               asyncResponse.resume(
//                   Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                   .entity("Operation time out.").build());
//          }
//        });
//
//        asyncResponse.setTimeout(600, TimeUnit.SECONDS);

        new Thread(new Runnable() {
            @Override
            public void run() {

                StreamingOutput result;
                try {
                    result = ProcessQuery.processQuery(qeAndType, format);
                } catch (Exception e) {
                    qeAndType.getQueryExecution().abort();
                    throw new RuntimeException();
                }
                asyncResponse.resume(result);
            }
        }).start();
    }












    //@Produces("application/rdf+xml")
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @GET
//    @Produces("application/rdf+xml") //HttpParams.contentTypeRDFXML)
//    public Response executeQueryRdfXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
//    }

    @GET
    @Produces("application/rdf+xml") //HttpParams.contentTypeRDFXML)
    public void executeQueryRdfXml(@Suspended final AsyncResponse asyncResponse, @QueryParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces("application/rdf+xml")// HttpParams.contentTypeRDFXML)
//    public Response executeQueryRdfXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
//    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/rdf+xml")// HttpParams.contentTypeRDFXML)
    public void executeQueryRdfXmlPost(@Suspended final AsyncResponse asyncResponse, @FormParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_RdfXml);
    }




//    @GET
//    @Produces("application/sparql-results+xml")
//    public Response executeQueryResultSetXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
//    }

    @GET
    @Produces("application/sparql-results+xml")
    public void executeQueryResultSetXml(@Suspended final AsyncResponse asyncResponse, @QueryParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_XML);
    }



//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces("application/sparql-results+xml")
//    public Response executeQueryResultSetXmlPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
//    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/sparql-results+xml")
    public void executeQueryResultSetXmlPost(@Suspended final AsyncResponse asyncResponse, @FormParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_Text);
    }



//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response executeQueryText(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
//    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void executeQueryText(@Suspended final AsyncResponse asyncResponse, @QueryParam("query") String queryString) {
        processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_Text);
    }



//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response executeQueryTextPost(@Context HttpServletRequest req, @FormParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
//    }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_PLAIN)
  public void executeQueryTextPost(@Suspended final AsyncResponse asyncResponse, @FormParam("query") String queryString) {
      processQueryAsync(asyncResponse, queryString, SparqlFormatterUtils.FORMAT_Text);
  }

}

