package org.aksw.jena_sparql_api.web.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.CompletionCallback;
import javax.ws.rs.container.ConnectionCallback;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionAndType;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUpdate;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.aksw.jena_sparql_api.web.utils.ThreadUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Jersey resource for an abstract SPARQL endpoint based on the AKSW SPARQL API.
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public abstract class SparqlEndpointBase {

    private static final Logger logger = LoggerFactory.getLogger(SparqlEndpointBase.class);

    //private @Context HttpServletRequest req;

    protected SparqlStmtParser defaultSparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

    protected SparqlStmtParser getSparqlStmtParser() {
        return defaultSparqlStmtParser;
    }

//    public SparqlEndpointBase() {
//
//    }

    @Deprecated
    public QueryExecution createQueryExecution(Query query, @Context HttpServletRequest req) {
        QueryExecutionAndType tmp = createQueryExecutionAndType(query.toString());
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
    public QueryExecutionAndType createQueryExecutionAndType(String queryString) {
        //Query query = new Query();
        //query.setPrefix("bif", "http://www.openlinksw.com/schemas/bif#");

        //QueryFactory.parse(query, queryString, "http://example.org/base-uri/", Syntax.syntaxSPARQL_11);
        // TODO We should not have to parse the query string here, as this is the parser's job
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);


        QueryExecution qe = createQueryExecution(query);

        QueryExecutionAndType result = new QueryExecutionAndType(qe, query.getQueryType());

        return result;
    }

    public QueryExecutionAndType createQueryExecutionAndType(Query query) {
        QueryExecution qe = createQueryExecution(query);
        QueryExecutionAndType result = new QueryExecutionAndType(qe, query.getQueryType());
        return result;
    }

//    public QueryExecutionAndType createQueryExecution(SparqlStmtQuery stmt) {
//        QueryExecutionAndType result = stmt.isParsed()
//                ? createQueryExecution(stmt.getQueryString())
//                : createQueryExecution(stmt.getQuery())
//                ;
//
//        return result;
//    }

    /**
     * Override this method to provide your own classifier of whether a statement is a query or update request
     * @param queryString
     * @return
     */
    public SparqlStmt classifyStmt(String stmtStr) {
        SparqlStmtParser sparqlStmtParser = getSparqlStmtParser();
        SparqlStmt result = sparqlStmtParser.apply(stmtStr);
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
        QueryExecutionAndType qeAndType = createQueryExecutionAndType(queryString);

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
    public void executeQueryXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
            //throws Exception {

        if(queryString == null && updateString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
        } else {
            processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_XML);
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
    @Produces(MediaType.APPLICATION_XML)
    public void executeQueryXmlPostAsync(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
            //throws Exception {

        if(queryString == null) {
            queryString = updateString;
        }


        if(queryString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            //.entity("Connection Callback").build());

            asyncResponse.resume(Response.ok(so).build()); // TODO: Return some error HTTP code
        } else {
        //  return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
            processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_XML);
        }
    }



//    @GET
//    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
//    public Response executeQueryJson(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Json);
//    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/sparql-results+json"})
    public void executeQueryJson(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_Json);
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
    public void executeQueryXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateStr) {
        if(queryString == null) {
            queryString = updateStr;
        }
        processStmtAsync(asyncResponse, queryString, updateStr, SparqlFormatterUtils.FORMAT_Json);
    }

    public void processStmtAsync(final AsyncResponse response, String queryStr, String updateStr, final String format) {
        if(queryStr == null && updateStr == null) {
            throw new RuntimeException("No query/update statement provided");
        }

        if (queryStr != null && updateStr != null) {
            throw new RuntimeException("Both 'query' and 'update' statement strings provided in a single request");
        }

        String stmtStr = queryStr != null ? queryStr : updateStr;

        SparqlStmt stmt = classifyStmt(stmtStr);

        if(stmt.isQuery()) {
            processQueryAsync(response, stmt.getAsQueryStmt(), format);
        } else if(stmt.isUpdateRequest()) {
            processUpdateAsync(response, stmt.getAsUpdateStmt());
        } else {
            throw new RuntimeException("Unknown request type: " + queryStr);
        }
    }

    public void processQueryAsync(final AsyncResponse response, SparqlStmtQuery stmt, final String format) {

//        QueryExecutionAndType tmp;
//
//        try {
//            tmp = createQueryExecution(queryString);
//        } catch(Exception e) {
//
////            response.resume(
////                    Response.status(Response.Status.SERVICE_UNAVAILABLE)
////                    .entity("Connection Callback").build());
////
////            return;
//            throw new RuntimeException(e);
//        }

        final QueryExecutionAndType qeAndType = stmt.isParsed()
                ? createQueryExecutionAndType(stmt.getQuery())
                : createQueryExecutionAndType(stmt.getOriginalString())
                ;
//        if(stmt.isParsed()) {
//
//        }
//
//      final QueryExecutionAndType qeAndType = createQueryExecution(stmt.getOriginalString());



//        asyncResponse
//        .register(new CompletionCallback() {
//
//            @Override
//            public void onComplete(Throwable arg0) {
//                System.out.println("COMPLETE");
//            }
//        });

        response
        .register(new ConnectionCallback() {
            @Override
            public void onDisconnect(AsyncResponse disconnect) {
                logger.debug("Client disconnected");

                qeAndType.getQueryExecution().abort();

//                if(true) {
//                disconnect.resume(
//                    Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                    .entity("Connection Callback").build());
//                } else {
//                    disconnect.cancel();
//                }
            }
        });

        response
        .register(new CompletionCallback() {
            @Override
            public void onComplete(Throwable t) {
                if(t == null) {
                    logger.debug("Successfully completed query execution");
                } else {
                    logger.debug("Failed query execution");
                }
                // Redundant close
                // qeAndType.getQueryExecution().close();
            }
        });

//        response
//        .setTimeoutHandler(new TimeoutHandler() {
//           @Override
//           public void handleTimeout(AsyncResponse asyncResponse) {
//               logger.debug("Timout on request");
//               asyncResponse.resume(
//                   Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                   .entity("Operation time out.").build());
//          }
//        });
//
//        response.setTimeout(600, TimeUnit.SECONDS);

        ThreadUtils.start(response, new Runnable() {
            @Override
            public void run() {
                try {
                    StreamingOutput result = ProcessQuery.processQuery(qeAndType, format);
                    response.resume(result);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
    public void executeQueryRdfXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString
            ) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_RdfXml);
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
    public void executeQueryRdfXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("query") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_RdfXml);
    }




//    @GET
//    @Produces("application/sparql-results+xml")
//    public Response executeQueryResultSetXml(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_XML);
//    }

    @GET
    @Produces("application/sparql-results+xml")
    public void executeQueryResultSetXml(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_XML);
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
    public void executeQueryResultSetXmlPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_Text);
    }



//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response executeQueryText(@Context HttpServletRequest req, @QueryParam("query") String queryString)
//            throws Exception {
//        return processQuery(req, queryString, SparqlFormatterUtils.FORMAT_Text);
//    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void executeQueryText(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("query") String queryString,
            @QueryParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_Text);
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
    public void executeQueryTextPost(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("query") String queryString,
            @FormParam("update") String updateString) {
        processStmtAsync(asyncResponse, queryString, updateString, SparqlFormatterUtils.FORMAT_Text);
    }




    /*
     * UPDATE
     */





//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public void executeUpdateGet(@Suspended final AsyncResponse asyncResponse,
//            @QueryParam("update") String updateRequestStr)
//        throws Exception
//    {
//        processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//    }


//    @POST
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.APPLICATION_JSON)
//    public void executeUpdatePost(@Suspended final AsyncResponse asyncResponse,
//            @FormParam("update") String updateRequestStr)
//        throws Exception
//    {
//        processUpdateAsync(asyncResponse, new SparqlStmtUpdate(updateRequestStr));
//    }

//    public void executeUpdateAny(@Suspended final AsyncResponse asyncResponse,
//            String serviceUri,
//            String queryString,
//            List<String> usingGraphUris,
//            List<String> usingNamedGraphUris)
//        throws Exception
//    {
//        if(queryString == null) {
//            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
//            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
//        } else {
//            processUpdateAsync(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
//        }
//    }


//    public UpdateProcessor createUpdateProcessor(String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
//        HttpAuthenticator authenticator = AuthenticatorUtils.parseAuthenticator(req);
//
//        SparqlServiceFactory ssf = getSparqlServiceFactory();
//        UpdateProcessor result = createUpdateProcessor(ssf, serviceUri, requestStr, usingGraphUris, usingNamedGraphUris, authenticator);
//        return result;
//    }
//
//    public UpdateProcessor createUpdateProcessor(String updateRequestStr) {
//        throw new RuntimeException("For update requests, this method must be overriden");
//    }
//
//    public static UpdateProcessor createUpdateProcessor(SparqlServiceFactory ssf, String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris, HttpAuthenticator authenticator) {
//        // TODO Should we use UsingList or DatasetDescription? The latter feels more natural to use.
////      UsingList usingList = new UsingList();
////      usingList.addAllUsing(NodeUtils.convertToNodes(usingGraphUris));
////      usingList.addAllUsingNamed(NodeUtils.convertToNodes(usingNamedGraphUris));
//        DatasetDescription datasetDescription = new DatasetDescription(usingGraphUris, usingNamedGraphUris);
//
//
//        SparqlService sparqlService = ssf.createSparqlService(serviceUri, datasetDescription, authenticator);
//
//        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
//
//        UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
//        UpdateProcessor result = uef.createUpdateProcessor(updateRequest);
//        return result;
//    }
    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) { //UpdateRequest updateRequest);
        throw new UnsupportedOperationException("The method for handling SPARQL update requests has not been overridden");
    }

    public void processUpdateAsync(final AsyncResponse response, SparqlStmtUpdate stmt) { //String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
        UpdateProcessor updateProcessor = createUpdateProcessor(stmt.getAsUpdateStmt()); //serviceUri, requestStr, usingGraphUris, usingNamedGraphUris);

        DatasetGraph dg = updateProcessor.getDatasetGraph();
        if (dg != null) {
            Txn.executeWrite(dg, () -> {
                updateProcessor.execute();
            });
        } else {
            updateProcessor.execute();
        }


//      QueryExecutionAndType tmp;
//
//      try {
//          tmp = createQueryExecution(queryString);
//      } catch(Exception e) {
//
////          response.resume(
////                  Response.status(Response.Status.SERVICE_UNAVAILABLE)
////                  .entity("Connection Callback").build());
////
////          return;
//          throw new RuntimeException(e);
//      }

//      final QueryExecutionAndType qeAndType = tmp;


//      asyncResponse
//      .register(new CompletionCallback() {
//
//          @Override
//          public void onComplete(Throwable arg0) {
//              System.out.println("COMPLETE");
//          }
//      });

      response
      .register(new ConnectionCallback() {
          @Override
          public void onDisconnect(AsyncResponse disconnect) {
              logger.debug("Client disconnected");

              // TODO Abort
              //qeAndType.getQueryExecution().abort();

//              if(true) {
//              disconnect.resume(
//                  Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                  .entity("Connection Callback").build());
//              } else {
//                  disconnect.cancel();
//              }
          }
      });

      response
      .register(new CompletionCallback() {
          @Override
          public void onComplete(Throwable t) {
              if(t == null) {
                  logger.debug("Successfully completed query execution");
              } else {
                  logger.debug("Failed query execution");
              }
              //qeAndType.getQueryExecution().close();
              // TODO Close
          }
      });

//      response
//      .setTimeoutHandler(new TimeoutHandler() {
//         @Override
//         public void handleTimeout(AsyncResponse asyncResponse) {
//             logger.debug("Timout on request");
//             asyncResponse.resume(
//                 Response.status(Response.Status.SERVICE_UNAVAILABLE)
//                 .entity("Operation time out.").build());
//        }
//      });
//
//      response.setTimeout(600, TimeUnit.SECONDS);

      ThreadUtils.start(response, new Runnable() {
          @Override
          public void run() {
              try {
                  String result = "{\"success\": true}";
                  //StreamingOutput result = ProcessQuery.processQuery(qeAndType, format);
                  response.resume(result);
              } catch(Exception e) {
                  throw new RuntimeException(e);
              }
          }
      });
  }



}

