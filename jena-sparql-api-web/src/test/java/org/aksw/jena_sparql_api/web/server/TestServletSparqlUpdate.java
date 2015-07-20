package org.aksw.jena_sparql_api.web.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.Test;

public class TestServletSparqlUpdate {
    @Test
    public void test1() throws Exception {
        int port = 7533;

        Server server = ServerUtils.startServer(port, new WebAppInitializer());


        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:" + port + "/sparql");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("service-uri", "http://localhost:8890/sparql"));
        params.add(new BasicNameValuePair("using-graph-uri", "http://jsa.aksw.org/test/data/"));
        params.add(new BasicNameValuePair("update", "Prefix ex: <http://example.org/> Delete Data{ ex:s ex:p ex:o }"));
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
}
