package org.aksw.jena_sparql_api.io.endpoint;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamSource {
    InputStream openInputStream()  throws IOException;
}
