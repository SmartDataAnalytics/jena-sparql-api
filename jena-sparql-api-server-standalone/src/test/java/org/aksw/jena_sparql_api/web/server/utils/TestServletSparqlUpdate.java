package org.aksw.jena_sparql_api.web.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.utils.UpdateExecutionUtils;
import org.aksw.jena_sparql_api.server.utils.WebAppInitializerSparqlService;
import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.jena.sparql.core.DatasetDescription;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class TestServletSparqlUpdate {
    // TODO Make this test work in offline mode
    //@Test
    public void test1() throws Exception {
        int port = 7533;


        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigApp.class);
        SparqlServiceFactory ssf = (SparqlServiceFactory) ctx.getBean("sparqlServiceFactory");

        SparqlService ssDBpedia = ssf.createSparqlService("http://dbpedia.org/sparql", new DatasetDescription(Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()), null);
        String s = "Construct { ?s ?p ?o } { ?s ?p ?o { Select Distinct ?s { ?s a <http://dbpedia.org/ontology/Person> } Limit 10 } }";


        SparqlService ssLocal = ssf.createSparqlService("http://localhost:8890/sparql", new DatasetDescription(Collections.singletonList("http://jsa.aksw.org/test/data/"), Collections.<String>emptyList()), null);
        UpdateExecutionUtils.copyByConstruct(ssLocal, ssDBpedia, s, 1000);


        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ConfigApp.class);

        Server server = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));




        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:" + port + "/sparql");


        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("service-uri", "http://localhost:8890/sparql"));
        params.add(new BasicNameValuePair("using-graph-uri", "http://jsa.aksw.org/test/data/"));
        //params.add(new BasicNameValuePair("update", "Prefix ex: <http://example.org/> Delete Data{ ex:s ex:p ex:o }"));
        params.add(new BasicNameValuePair("update", "Prefix ex: <http://example.org/> Delete { ?s ?p ?o } Where { ?s ?x <http://dbpedia.org/class/yago/Creator109614315> ; ?p ?o }"));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));


        HttpResponse response = httpClient.execute(httpPost);
        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        HttpEntity respEntity = response.getEntity();

        if (respEntity != null) {
            // EntityUtils to get the response content
            String content =  EntityUtils.toString(respEntity);
            System.out.println("Response: " + content);
        }

        server.stop();
        server.join();

    }

    //@Test
    public void test2() throws Exception {
        int port = 7533;
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ConfigApp.class);

        Server server = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));
        server.join();

//      ResourceHandler resHandler = new ResourceHandler();
//      resHandler.setBaseResource(Resource.newClassPathResource("/snorql/"));
//      server.setHandler(resHandler);


        //http://stackoverflow.com/questions/29658240/spring-boot-jersey-allow-jersey-to-serve-static-content
        //SpringApplication.run(MySpringBootServletInitializer.class);

//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigApp.class);
//        SparqlServiceFactory ssf = (SparqlServiceFactory) ctx.getBean("sparqlServiceFactory");

        //SparqlService ssDBpedia = ssf.createSparqlService("http://akswnc3.informatik.uni-leipzig.de/data/jassa/sparql", new DatasetDescription(Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList()), null);
        //String s = "Construct { ?s ?p ?o } { ?s ?p ?o { Select Distinct ?s { ?s a <http://dbpedia.org/ontology/Person> } Limit 10 } }";


        //SparqlService ssLocal = ssf.createSparqlService("http://localhost:8890/sparql", new DatasetDescription(Collections.singletonList("http://jsa.aksw.org/test/data/"), Collections.<String>emptyList()), null);
        //UpdateExecutionUtils.copyByConstruct(ssLocal, ssDBpedia, s, 1000);



    }
}
