package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.io.InputStream;

public interface BinarySearcher
    extends AutoCloseable
{
    InputStream search(byte[] prefix) throws IOException;

    // Add default method for CharSequence?

    default InputStream search(String str) throws IOException {
        InputStream result = search(str == null ? null : str.getBytes());
        return result;
    }

    @Override
    void close() throws Exception;
}
