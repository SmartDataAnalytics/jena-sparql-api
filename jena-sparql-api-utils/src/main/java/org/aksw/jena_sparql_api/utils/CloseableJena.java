package org.aksw.jena_sparql_api.utils;

import java.io.Closeable;

public class CloseableJena
    implements Closeable
{
    protected org.apache.jena.atlas.lib.Closeable closeable;

    public CloseableJena(org.apache.jena.atlas.lib.Closeable jenaCloseable) {
        this.closeable = jenaCloseable;
    }

    @Override
    public void close() {
        if(closeable != null) {
            closeable.close();
        }
    }
}
