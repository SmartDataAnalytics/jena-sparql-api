package org.aksw.jena_sparql_api.web.servlets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.aksw.jena_sparql_api.web.utils.AuthenticatorUtils;
import org.aksw.jena_sparql_api.web.utils.ThreadUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;


@Service
@javax.ws.rs.Path("/path-finding")
public class PathFindingApi {

    //@Resource(name="jassa.sparqlServiceFactory")

    public PathFindingApi() {
    }

    @Autowired
    private SparqlServiceFactory sparqlServiceFactory;

    @Autowired
    private HttpServletRequest req;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void findPathsPost(
            @Suspended final AsyncResponse asyncResponse,
            @QueryParam("service-uri") String serviceUri,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri") List<String> namedGraphUris,
            @QueryParam("source-element") String sourceElement,
            @QueryParam("source-var") String sourceVar,
            @QueryParam("target-element") String targetElement,
            @QueryParam("target-var") String targetVar,
            @QueryParam("js-service-uri") String joinSummaryServiceUri,
            @QueryParam("js-graph-uri") List<String> joinSummaryGraphUris,
            @QueryParam("query") String queryString,
            @QueryParam("n-paths") Integer nPaths,
            @QueryParam("max-hops") Integer maxHops
    ) throws ClassNotFoundException, SQLException {
        findPaths(asyncResponse, serviceUri, defaultGraphUris, namedGraphUris, sourceElement, sourceVar, targetElement, targetVar, joinSummaryServiceUri, joinSummaryGraphUris, queryString, nPaths, maxHops);
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public void findPathsGet(
            @Suspended final AsyncResponse asyncResponse,
            @FormParam("service-uri") String serviceUri,
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri") List<String> namedGraphUris,
            @FormParam("source-element") String sourceElement,
            @FormParam("source-var") String sourceVar,
            @FormParam("target-element") String targetElement,
            @FormParam("target-var") String targetVar,
            @FormParam("js-service-uri") String joinSummaryServiceUri,
            @FormParam("js-graph-uri") List<String> joinSummaryGraphUris,
            @FormParam("query") String queryString,
            @FormParam("n-paths") Integer nPaths,
            @FormParam("max-hops") Integer maxHops
    ) throws ClassNotFoundException, SQLException {
        findPaths(asyncResponse, serviceUri, defaultGraphUris, namedGraphUris, sourceElement, sourceVar, targetElement, targetVar, joinSummaryServiceUri, joinSummaryGraphUris, queryString, nPaths, maxHops);
    }

    public void findPaths(
            final AsyncResponse response,
            final String serviceUri,
            final List<String> defaultGraphUris,
            final List<String> namedGraphUris,
            final String sourceElement,
            final String sourceVar,
            final String targetElement,
            final String targetVar,
            final String joinSummaryServiceUri,
            final List<String> joinSummaryGraphUris,
            final String queryString,
            final Integer nPaths,
            final Integer maxHops
    ) throws ClassNotFoundException, SQLException {

        // Must parse the authenticator here (outside of the async thread)
        UsernamePasswordCredentials credentials = AuthenticatorUtils.parseCredentials(req);
        HttpClient httpClient = AuthenticatorUtils.prepareHttpClientBuilder(credentials).build();


        ThreadUtils.start(response, new Runnable() {
            @Override
            public void run() {
                int _nPaths = nPaths != null? nPaths : 3;
                int _maxHops = maxHops != null ? maxHops : 3;


                DatasetDescription datasetDescription = new DatasetDescription(defaultGraphUris, namedGraphUris);

                Concept sourceConcept = Concept.create(sourceElement, sourceVar);
                Concept targetConcept = Concept.create(targetElement, targetVar);

                SparqlService sparqlService = sparqlServiceFactory.createSparqlService(serviceUri, datasetDescription, httpClient);
                QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
                Model joinSummaryModel;

                List<String> jss = joinSummaryGraphUris != null ? joinSummaryGraphUris : Collections.<String>emptyList();

                DatasetDescription jsDs = new DatasetDescription(jss, Collections.<String>emptyList());

                if(joinSummaryServiceUri != null && !joinSummaryServiceUri.isEmpty()) {

                    // TODO Add support for authenticating at the join summary service
                    SparqlService jsSparqlService = sparqlServiceFactory.createSparqlService(joinSummaryServiceUri, jsDs, null);
                    QueryExecutionFactory jsQef = jsSparqlService.getQueryExecutionFactory();
                    joinSummaryModel = ConceptPathFinder.createJoinSummary(jsQef);
                } else {
                    joinSummaryModel = ConceptPathFinder.createDefaultJoinSummaryModel(qef);
                }

                List<SimplePath> paths = ConceptPathFinder.findPaths(qef, sourceConcept, targetConcept, _nPaths, _maxHops, joinSummaryModel);

                String result;

                // if there is a queryString, we will use sparql mode, otherwise, we will just return the json

                if(queryString != null && !queryString.isEmpty()) {

                    Model model = ConceptPathFinder.createModel(paths);
                    QueryExecutionFactoryModel pathSparqlService = new QueryExecutionFactoryModel(model);
                    QueryExecution qe = pathSparqlService.createQueryExecution(queryString);
                    ResultSet rs = qe.execSelect();

                    result = SparqlFormatterUtils._formatJson(rs);
//                    Writer writer = new JsonWriter();
//                    writer.wr

                }
                else {
                    List<String> tmp = new ArrayList<String>();
                    for(SimplePath path : paths) {
                        tmp.add(path.toPathString());
                    }

                    Gson gson = new Gson();
                    result = gson.toJson(tmp);
                }

                //return result;

                response.resume(result);
            }
        });
    }


    /**
     * Input: A JSon object with the fields:
     * {
     *     service: { serviceIri: '', defaultGraphIris: [] }
     *     sourceConcept: { elementStr: '', varName: '' }
     *     targetConcept:
     * }
     *
     *
     * @param serviceDesc A json object describing the service.
     * @param startConcept
     * @param destConcept
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	public String findPaths(@QueryParam("query") String json) throws ClassNotFoundException, SQLException {
//		Gson gson = new Gson();
//		PathDesc pathDesc = gson.fromJson(json, PathDesc.class);
//
//		ConceptDesc sourceDesc = pathDesc.getSourceConcept();
//		Concept sourceConcept = Concept.create(sourceDesc.getElementStr(), sourceDesc.getVarName());
//
//		ConceptDesc targetDesc = pathDesc.getTargetConcept();
//		Concept targetConcept = Concept.create(targetDesc.getElementStr(), targetDesc.getVarName());
//
//		ServiceDesc serviceDesc = pathDesc.getService();
//		QueryExecutionFactory service = sparqlServiceFactory.createSparqlService(serviceDesc.getServiceIri(), serviceDesc.getDefaultGraphIris());
//
//		List<Path> paths = ConceptPathFinder.findPaths(service, sourceConcept, targetConcept);
//
//		List<String> tmp = new ArrayList<String>();
//		for(Path path : paths) {
//			tmp.add(path.toPathString());
//		}
//
//		String result = gson.toJson(tmp);
//		return result;
//	}
}
