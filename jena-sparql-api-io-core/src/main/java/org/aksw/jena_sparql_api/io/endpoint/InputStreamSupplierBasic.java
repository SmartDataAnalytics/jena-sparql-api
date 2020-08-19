package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.rxjava3.core.Single;

public class InputStreamSupplierBasic
    implements InputStreamSupplier
{
    protected InputStreamSource inputStreamSource;

    public InputStreamSupplierBasic(InputStreamSource inputStreamSource) {
        super();
        this.inputStreamSource = inputStreamSource;
    }

    @Override
    public Single<InputStream> execStream() throws IOException {
        return Single.just(inputStreamSource.openInputStream());
    }

    public static InputStreamSupplier wrap(InputStreamSource inputStreamSource) {
        return new InputStreamSupplierBasic(inputStreamSource);
    }
}
