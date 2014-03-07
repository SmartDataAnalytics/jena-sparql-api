package org.aksw.jena_sparql_api.cache.staging;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.commons.collections.IClosable;

public class InputStreamClosable
    extends InputStreamDecorator
{
    private IClosable closable;

    public InputStreamClosable(InputStream decoratee, IClosable closable) {
        super(decoratee);
        this.closable = closable;
    }

    @Override
    public void close() throws IOException {
        closable.close();
        super.close();
    }
}