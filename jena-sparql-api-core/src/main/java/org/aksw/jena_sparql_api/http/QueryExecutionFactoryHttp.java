package org.aksw.jena_sparql_api.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackString;
import org.aksw.jena_sparql_api.core.QueryExecutionStreaming;
import org.aksw.jena_sparql_api.core.QueryExecutionStreamingWrapper;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/23/11
 *         Time: 9:47 PM
 */
public class QueryExecutionFactoryHttp
    extends QueryExecutionFactoryBackString
{
    private String service;

    private List<String> defaultGraphs = new ArrayList<String>();

    public QueryExecutionFactoryHttp(String service) {
        this(service, Collections.<String>emptySet());
    }

    public QueryExecutionFactoryHttp(String service, String defaultGraphName) {
        this(service, Collections.singleton(defaultGraphName));
    }

    public QueryExecutionFactoryHttp(String service, Collection<String> defaultGraphs) {
        this.service = service;
        this.defaultGraphs = new ArrayList<String>(defaultGraphs);
        Collections.sort(this.defaultGraphs);
    }

    @Override
    public String getId() {
        return service;
    }

    @Override
    public String getState() {
        return Joiner.on("|").join(defaultGraphs);
    }

    @Override
    public QueryExecutionStreaming createQueryExecution(String queryString) {
        QueryEngineHTTP engine = new QueryEngineHTTP(service, queryString);
        engine.setDefaultGraphURIs(defaultGraphs);

        QueryExecutionStreaming result = QueryExecutionStreamingWrapper.wrap(engine);
        
        return result;
    }
}
