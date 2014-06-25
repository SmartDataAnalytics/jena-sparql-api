package org.aksw.jena_sparql_api.web.servlets;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Path;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;


@Service
@javax.ws.rs.Path("/path-finding")
public class PathFindingApi {

    //@Resource(name="jassa.sparqlServiceFactory")
    
    public PathFindingApi() {        
    }
    
    @Autowired
    private SparqlServiceFactory sparqlServiceFactory;
	
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String findPaths(
            @QueryParam("service-uri") String serviceUri,
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("source-element") String sourceElement,
            @QueryParam("source-var") String sourceVar,
            @QueryParam("target-element") String targetElement,
            @QueryParam("target-var") String targetVar,
            @QueryParam("js-service-uri") String joinSummaryServiceUri,
            @QueryParam("js-graph-uri") List<String> joinSummaryGraphUris,
            @QueryParam("query") String queryString,
            @QueryParam("n-paths") Integer nPaths,
            @QueryParam("max-hops") Integer maxHops
    )
        throws ClassNotFoundException, SQLException {
        
        nPaths = nPaths != null? nPaths : 3;
        maxHops = maxHops != null ? maxHops : 3;
        
        Concept sourceConcept = Concept.create(sourceElement, sourceVar);        
        Concept targetConcept = Concept.create(targetElement, targetVar);
        
        QueryExecutionFactory sparqlService = sparqlServiceFactory.createSparqlService(serviceUri, defaultGraphUris);

        Model joinSummaryModel;
        
        if(joinSummaryServiceUri != null && !joinSummaryServiceUri.isEmpty()) {
            
            if(joinSummaryGraphUris == null) {
                joinSummaryGraphUris = Collections.emptyList();
            }
            
            QueryExecutionFactory jsSparqlService = sparqlServiceFactory.createSparqlService(joinSummaryServiceUri, joinSummaryGraphUris);
            joinSummaryModel = ConceptPathFinder.createJoinSummary(jsSparqlService);
        } else {
            joinSummaryModel = ConceptPathFinder.createDefaultJoinSummaryModel(sparqlService); 
        }
        
        List<Path> paths = ConceptPathFinder.findPaths(sparqlService, sourceConcept, targetConcept, nPaths, maxHops, joinSummaryModel);
        
        String result;
        
        // if there is a queryString, we will use sparql mode, otherwise, we will just return the json
        
        if(queryString != null && !queryString.isEmpty()) {
            
            Model model = ConceptPathFinder.createModel(paths);
            QueryExecutionFactoryModel pathSparqlService = new QueryExecutionFactoryModel(model);
            QueryExecution qe = pathSparqlService.createQueryExecution(queryString);
            ResultSet rs = qe.execSelect();
            
            result = SparqlFormatterUtils._formatJson(rs);
//            Writer writer = new JsonWriter();
//            writer.wr
            
        }
        else {
            List<String> tmp = new ArrayList<String>();
            for(Path path : paths) {
                tmp.add(path.toPathString());
            }
            
            Gson gson = new Gson();
            result = gson.toJson(tmp);
        }
        
        return result;
    
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
