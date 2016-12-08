package org.aksw.jena_sparql_api.sparql.ext.http;

import java.util.function.Supplier;

import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionHttp {
    public static String httpFn = "http://jsa.aksw.org/fn/http/";

    public static void register(Supplier<HttpClient> httpClientSupplier) {
        FunctionRegistry.get().put(httpFn + "get", new FunctionFactoryE_Http(httpClientSupplier));
        FunctionRegistry.get().put(httpFn + "encode_for_qsa", E_EncodeForQsa.class);
    }
}
