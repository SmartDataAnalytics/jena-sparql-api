package org.aksw.jena_sparql_api.web.server;

import java.util.Arrays;
import java.util.Collections;

import org.aksw.jena_sparql_api.changeset.ChangeSetMetadata;
import org.aksw.jena_sparql_api.changeset.SinkChangeSetWriter;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp;
import org.aksw.jena_sparql_api.update.DatasetListenerSink;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.update.SparqlServiceFactoryEventSource;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class ConfigApp {

    @Bean
    @Qualifier("init")
    public SparqlServiceFactory coreSparqlServiceFactory() {
        final SparqlServiceFactory coreFactory = new SparqlServiceFactoryHttp();

        SparqlServiceFactory result = FluentSparqlServiceFactory
            .from(coreFactory)
            .config()
                .configQuery()
                    .withPagination(1000l)
                    .selectOnly()
                .end()
            .end().create();


/*
        SparqlServiceFactory result = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri, DatasetDescription datasetDescription, Object authenticator) {
                SparqlService coreService = coreFactory.createSparqlService(serviceUri, datasetDescription, authenticator);

                // Create a QueryExecutionFactory that wraps all query executions with QueryExecutionBaseSelect

                //QueryExecutionFactory qef = coreService.getQueryExecutionFactory();
                //qef = new QueryExecutionFactoryPaginated(qef, 1000);
                //qef = new QueryExecutionFactorySelect(qef);

                //SparqlService r = SparqlServiceImpl.create(qef, coreService.getUpdateExecutionFactory());
                // TODO This cast is due to a misdesign of the API - need to fix this.
                SparqlService r = FluentSparqlService.from(coreService)
                    .configureQuery()
                        .withPagination(1000l)
                    .end().create();



                return r;

            }
        };

*/
        return result;
    }

    @Bean
    @Autowired
    @Qualifier("init")
    public SparqlService sparqlServiceChangeSet(@Qualifier("init") SparqlServiceFactory ssf) {
        HttpAuthenticator authenticator = new SimpleAuthenticator("dba", "dba".toCharArray());
        DatasetDescription ds = new DatasetDescription(Arrays.asList("http://jsa.aksw.org/test/changesets"), Collections.<String>emptyList());;
        SparqlService result = ssf.createSparqlService("http://localhost:8890/sparql", ds, authenticator);
        return result;
    }

    @Bean
    @Autowired
    @Primary
    public SparqlServiceFactory sparqlServiceFactory(@Qualifier("init") SparqlServiceFactory ssf, @Qualifier("init") SparqlService ssfChangeSet) {

        ChangeSetMetadata metadata = new ChangeSetMetadata("claus", "testing");
        SparqlServiceFactoryEventSource result = new SparqlServiceFactoryEventSource(ssf);
        SinkChangeSetWriter sink = new SinkChangeSetWriter(metadata, ssfChangeSet);
        result.getListeners().add(new DatasetListenerSink(sink));

        return result;
    }

}
