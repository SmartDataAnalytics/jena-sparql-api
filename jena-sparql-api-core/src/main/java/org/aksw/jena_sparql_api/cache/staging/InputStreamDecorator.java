package org.aksw.jena_sparql_api.cache.staging;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamDecorator
    extends InputStream
{
    private InputStream decoratee;
    
    public InputStreamDecorator(InputStream decoratee) {
        this.decoratee = decoratee;
    }

    @Override
    public int available() throws IOException {
        return decoratee.available();
    }

    @Override
    public void mark(int readlimit) {
        decoratee.mark(readlimit);
    }
    
    @Override
    public boolean markSupported() {
        return decoratee.markSupported();
    }

    @Override
    public int read() throws IOException {
        return decoratee.read();
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return decoratee.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return decoratee.read(b, off, len);
    }
    
    @Override
    public void reset() throws IOException {
        decoratee.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return decoratee.skip(n);
    }
}