package org.aksw.jena_sparql_api.io.binseach.bz2;

import java.io.IOException;

import org.apache.hadoop.fs.Seekable;

public interface SeekableDecorator
    extends Seekable
{
    Seekable getSeekable();

    @Override
    default void seek(long l) throws IOException {
        getSeekable().seek(l);
    }

    @Override
    default long getPos() throws IOException {
        return getSeekable().getPos();
    }

    @Override
    default boolean seekToNewSource(long l) throws IOException {
        return getSeekable().seekToNewSource(l);
    }
}
