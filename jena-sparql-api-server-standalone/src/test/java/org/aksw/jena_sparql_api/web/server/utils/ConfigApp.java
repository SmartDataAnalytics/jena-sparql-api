package org.aksw.jena_sparql_api.web.server.utils;

import java.util.Arrays;
import java.util.Collections;

import org.aksw.jena_sparql_api.changeset.ChangeSetMetadata;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp;
import org.aksw.jena_sparql_api.update.DatasetListenerTrack;
import org.aksw.jena_sparql_api.update.FluentSparqlServiceFactory;
import org.aksw.jena_sparql_api.update.SparqlServiceFactoryEventSource;
import org.aksw.jena_sparql_api.web.utils.AuthenticatorUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.sparql.core.DatasetDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.common.base.Predicates;

@Configuration
//@ComponentScan({"org.aksw.jassa.web", "org.aksw.facete2.web"}) // TODO I think we can drop jassa.web from scannig by now
//@ComponentScan({"org.aksw.jena_sparql_api.web.utils.wip"})
//@Import(WebMvcConfigSnorql.class)
public class ConfigApp {

    @Bean
    @Qualifier("init")
    public SparqlServiceFactory coreSparqlServiceFactory() {
        final SparqlServiceFactory coreFactory = new SparqlServiceFactoryHttp();

        SparqlServiceFactory result = FluentSparqlServiceFactory
            .from(coreFactory)
            .configFactory()
                .configService()
                    .configQuery()
                        .withPagination(1000)
                        .selectOnly()
                        .end()
                    .end()
            .end()
            .create();

        return result;
    }

    @Bean
    @Autowired
    @Qualifier("init")
    public SparqlService sparqlServiceChangeSet(@Qualifier("init") SparqlServiceFactory ssf) {
    	UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("dba", "dba");
    	HttpClient httpClient = AuthenticatorUtils.prepareHttpClientBuilder(credentials).build();

        DatasetDescription ds = new DatasetDescription(Arrays.asList("http://jsa.aksw.org/test/changesets"), Collections.<String>emptyList());;
        SparqlService result = ssf.createSparqlService("http://localhost:8890/sparql", ds, httpClient);
        return result;
    }

    @Bean
    @Autowired
    @Primary
    public SparqlServiceFactory sparqlServiceFactory(@Qualifier("init") SparqlServiceFactory ssf, @Qualifier("init") SparqlService ssfChangeSet) {

        // TODO: The metadata must be injected on a per-request base because it holds a timestamp
        ChangeSetMetadata metadata = new ChangeSetMetadata("claus", "testing");
        SparqlServiceFactoryEventSource tmp = new SparqlServiceFactoryEventSource(ssf);
        //SinkChangeSetWriter sink = new SinkChangeSetWriter(metadata, ssfChangeSet);
        DatasetListener datasetListener = new DatasetListenerTrack(ssfChangeSet, metadata);
        tmp.getListeners().add(datasetListener);
        //tmp.getListeners().add(new DatasetListenerSink(sink));

        SparqlServiceFactory result = FluentSparqlServiceFactory
            .from(tmp)
            .configFactory()
                .defaultServiceUri("http://localhost:8890/sparql", Predicates.<String>alwaysFalse())
                //.defaultServiceUri("http://akswnc3.informatik.uni-leipzig.de/data/jassa/sparql", Predicates.<String>alwaysFalse())
            .end()
            .create();

        //SparqlService test = tmp.createSparqlService(null, null, null);

        return result;
    }

}



/*

        Function<QueryExecutionFactory, QueryExecutionFactory> x = FluentQueryExecutionFactoryFn.start().withPagination(1000l).withDefaultLimit(1000l, true).create();




        qef = x.apply(qef);


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
