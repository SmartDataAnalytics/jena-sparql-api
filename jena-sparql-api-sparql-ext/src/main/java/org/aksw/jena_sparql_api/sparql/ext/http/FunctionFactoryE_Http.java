package org.aksw.jena_sparql_api.sparql.ext.http;

import org.apache.http.client.HttpClient;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.function.FunctionFactory;

public class FunctionFactoryE_Http
    implements FunctionFactory
{
    protected Supplier<HttpClient> httpClientSupplier;

    public FunctionFactoryE_Http(Supplier<HttpClient> httpClientSupplier) {
        this.httpClientSupplier = httpClientSupplier;
    }

    @Override
    public E_Http create(String arg0) {
        E_Http result = new E_Http(httpClientSupplier);
        return result;
    }
}
