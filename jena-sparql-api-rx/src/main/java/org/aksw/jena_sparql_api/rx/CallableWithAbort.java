package org.aksw.jena_sparql_api.rx;

import java.util.concurrent.Callable;

public interface CallableWithAbort<T>
    extends Callable<T>
{
    void abort();
}
