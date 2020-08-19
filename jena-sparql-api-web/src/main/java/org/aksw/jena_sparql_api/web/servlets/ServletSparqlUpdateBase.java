package org.aksw.jena_sparql_api.web.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
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

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.syntax.UpdateRequestUtils;
import org.aksw.jena_sparql_api.web.utils.AuthenticatorUtils;
import org.aksw.jena_sparql_api.web.utils.ThreadUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ServletSparqlUpdateBase {


    private static final Logger logger = LoggerFactory.getLogger(ServletSparqlUpdateBase.class);

    protected @Context HttpServletRequest req;

    protected abstract SparqlServiceFactory getSparqlServiceFactory();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdateGet(@Suspended final AsyncResponse asyncResponse,
            @QueryParam("service-uri") String serviceUri,
            @QueryParam("update") String queryString,
            @QueryParam("using-graph-uri") List<String> usingGraphUris,
            @QueryParam("using-named-graph-uri") List<String> usingNamedGraphUris)
        throws Exception
    {
        executeUpdateAny(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void executeUpdatePost(@Suspended final AsyncResponse asyncResponse,
            @FormParam("service-uri") String serviceUri,
            @FormParam("update") String queryString,
            @FormParam("using-graph-uri") List<String> usingGraphUris,
            @FormParam("using-named-graph-uri") List<String> usingNamedGraphUris)
        throws Exception
    {
        executeUpdateAny(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
    }

    public void executeUpdateAny(@Suspended final AsyncResponse asyncResponse,
            String serviceUri,
            String queryString,
            List<String> usingGraphUris,
            List<String> usingNamedGraphUris)
        throws Exception
    {
        if(queryString == null) {
            StreamingOutput so = StreamingOutputString.create("<error>No query specified. Append '?query=&lt;your SPARQL query&gt;'</error>");
            asyncResponse.resume(Response.status(Status.BAD_REQUEST).entity(so).build()); // TODO: Return some error HTTP code
        } else {
            processUpdateAsync(asyncResponse, serviceUri, queryString, usingGraphUris, usingNamedGraphUris);
        }
    }


    public UpdateProcessor createUpdateProcessor(String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
        UsernamePasswordCredentials credentials = AuthenticatorUtils.parseCredentials(req);
        HttpClient httpClient = AuthenticatorUtils.prepareHttpClientBuilder(credentials).build();

        SparqlServiceFactory ssf = getSparqlServiceFactory();
        UpdateProcessor result = createUpdateProcessor(ssf, serviceUri, requestStr, usingGraphUris, usingNamedGraphUris, httpClient);
        return result;
    }


    public static UpdateProcessor createUpdateProcessor(SparqlServiceFactory ssf, String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris, HttpClient httpClient) {
        // TODO Should we use UsingList or DatasetDescription? The latter feels more natural to use.
//      UsingList usingList = new UsingList();
//      usingList.addAllUsing(NodeUtils.convertToNodes(usingGraphUris));
//      usingList.addAllUsingNamed(NodeUtils.convertToNodes(usingNamedGraphUris));
        DatasetDescription datasetDescription = new DatasetDescription(usingGraphUris, usingNamedGraphUris);


        SparqlService sparqlService = ssf.createSparqlService(serviceUri, datasetDescription, httpClient);

        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

        UpdateRequest updateRequest = UpdateRequestUtils.parse(requestStr);
        UpdateProcessor result = uef.createUpdateProcessor(updateRequest);
        return result;
    }

    public void processUpdateAsync(final AsyncResponse response, String serviceUri, String requestStr, List<String> usingGraphUris, List<String> usingNamedGraphUris) {
        UpdateProcessor updateProcessor = createUpdateProcessor(serviceUri, requestStr, usingGraphUris, usingNamedGraphUris);

        updateProcessor.execute();


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
