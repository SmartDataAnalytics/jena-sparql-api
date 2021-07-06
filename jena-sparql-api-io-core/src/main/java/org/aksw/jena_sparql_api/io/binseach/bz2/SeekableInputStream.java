package org.aksw.jena_sparql_api.io.binseach.bz2;

import java.io.InputStream;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.hadoop.fs.Seekable;



/** Combines Hadoop's Seekable and InputStream into one class */
public class SeekableInputStream
    extends ProxyInputStream implements SeekableDecorator
{
    protected Seekable seekable;

    /**
     * Constructs a new ProxyInputStream.
     *
     * @param proxy the InputStream to delegate to
     */
    public SeekableInputStream(InputStream proxy, Seekable seekable) {
        super(proxy);
        this.seekable = seekable;
    }

    @Override
    public Seekable getSeekable() {
        return seekable;
    }
}

