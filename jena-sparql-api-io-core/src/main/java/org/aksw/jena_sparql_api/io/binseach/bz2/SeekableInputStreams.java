package org.aksw.jena_sparql_api.io.binseach.bz2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.hadoop.fs.Seekable;

public class SeekableInputStreams
{
    public interface GetPosition { long call() throws IOException; }
    public interface SetPosition { void accept(long position) throws IOException; }

    public interface GetPositionFn<T> { long apply(T entity) throws IOException; }
    public interface SetPositionFn<T> { void apply(T entity, long position) throws IOException; }


    public static Seekable createSeekable(GetPosition getPosition, SetPosition setPosition) {
        return new Seekable() {
            @Override public void seek(long pos) throws IOException { setPosition.accept(pos); }
            @Override public long getPos() throws IOException { return getPosition.call(); }

            @Override
            public boolean seekToNewSource(long targetPos) throws IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T extends ReadableByteChannel> SeekableInputStream create(
            T channel,
            GetPositionFn<? super T> getPosition,
            SetPositionFn<? super T> setPosition
    ) {

        return create(
                Channels.newInputStream(channel),
                () -> getPosition.apply(channel),
                position -> setPosition.apply(channel, position));

    }


    public static <T extends ReadableByteChannel> SeekableInputStream create(
        InputStream in,
        GetPosition getPosition,
        SetPosition setPosition
    ) {
        Seekable seekable = createSeekable(getPosition, setPosition);
        return new SeekableInputStream(in, seekable);
    }


    public static SeekableInputStream create(
            InputStream in,
            Seekable seekable
        ) {
            return new SeekableInputStream(in, seekable);
        }
}
