package org.aksw.jena_sparql_api.batch;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.sparql.ext.http.JenaExtensionHttp;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.term.JenaExtensionTerm;
import org.apache.http.client.HttpClient;
import org.springframework.context.ApplicationContext;

public class JenaExtensionBatch {

    public static void initJenaExtensions(ApplicationContext context) {
        //ApplicationContext baseContext = initBaseContext();
        @SuppressWarnings("unchecked")
        Supplier<HttpClient> httpClientSupplier = (Supplier<HttpClient>)context.getBean("httpClientSupplier");
        
        JenaExtensionJson.register();
        JenaExtensionHttp.register(httpClientSupplier);
        JenaExtensionTerm.register();        
    }

}
