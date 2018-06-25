package org.aksw.jena_sparql_api.web.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUpdate;
import org.aksw.jena_sparql_api.web.utils.AuthenticatorUtils;
import org.aksw.jena_sparql_api.web.utils.DatasetDescriptionRequestUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.springframework.util.Assert;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;


/**
 * Proxy implementation based on a Sparql service object
 * @author raven
 *
 */
public abstract class ServletSparqlServiceBase
    extends SparqlEndpointBase
{
//    private static final Logger logger = LoggerFactory.getLogger(ServletSparqlServiceBase.class);

    protected @Context HttpServletRequest req;

    //protected abstract SparqlStmtParser sparqlStmtParser;

    protected abstract SparqlServiceFactory getSparqlServiceFactory();

    /**
     * Important: If no SPARQL service is specified, null is returned.
     * This means, that it is up to the SparqlServiceFactory to
     * - use a default service
     * - reject invalid service requests
     *
     *
     * @return
     */
    protected String getServiceUri() {
        String result;
        try {
            result = ServletRequestUtils.getStringParameter(req, "service-uri");
        } catch (ServletRequestBindingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    protected UsernamePasswordCredentials getCredentials() {
    	UsernamePasswordCredentials result = AuthenticatorUtils.parseCredentials(req);
        return result;
    }

    protected DatasetDescription getDatasetDescription() {
        DatasetDescription result = DatasetDescriptionRequestUtils.extractDatasetDescriptionAny(req);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        SparqlServiceFactory ssf = getSparqlServiceFactory();
        Assert.notNull(ssf, "Got null for sparqlServiceFactory");

        String serviceUri = getServiceUri();

        DatasetDescription datasetDescription = getDatasetDescription();
        UsernamePasswordCredentials credentials = getCredentials();
        HttpClient httpClient = AuthenticatorUtils.prepareHttpClientBuilder(credentials).build();

        SparqlService ss = ssf.createSparqlService(serviceUri, datasetDescription, httpClient);
        QueryExecution result = ss.getQueryExecutionFactory().createQueryExecution(query);
        return result;
    }

    @Override
    public UpdateProcessor createUpdateProcessor(SparqlStmtUpdate stmt) {
        SparqlServiceFactory ssf = getSparqlServiceFactory();
        Assert.notNull(ssf, "Got null for SparqlServiceFactory");
        String serviceUri = getServiceUri();

        DatasetDescription datasetDescription = getDatasetDescription();
        UsernamePasswordCredentials credentials = getCredentials();
        HttpClient httpClient = AuthenticatorUtils.prepareHttpClientBuilder(credentials).build();

        SparqlService ss = ssf.createSparqlService(serviceUri, datasetDescription, httpClient);
        UpdateExecutionFactory uef = ss.getUpdateExecutionFactory();
        UpdateProcessor result;
        if(stmt.isParsed()) {
            UpdateRequest updateRequest = stmt.getUpdateRequest();
            result = uef.createUpdateProcessor(updateRequest);
        } else {
            String updateRequestStr = stmt.getOriginalString();
            result = uef.createUpdateProcessor(updateRequestStr);
        }
        return result;
    }

}
