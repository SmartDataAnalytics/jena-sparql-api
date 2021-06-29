package org.aksw.jena_sparql_api.io.binseach.bz2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.io.util.channel.ReadableByteChannelDecoratorBase;
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


    /**
     * The argument for invoking this methods must be a seekable input streams that implements
     * hadoop's protocol for splittable codecs using READ_MODE.BYBLOCK.
     *
     * Whereas hadoop's protocol will alawys read 1 byte beyond the split boundary,
     * this wrapper will stop exactly at that boundary. Internally a push-back input stream is used
     * to push that single "read-ahead" byte back once it is encountered.
     *
     * A block boundary is advertised by a call to read() by returing -2.
     * This return value indicates that read() may be
     * called again and will return at least one more byte.
     * A return value of -1 indicates "end of file" just as usual.
     *
     * @param decodedIn
     * @return
     * @throws IOException
     */
    public static ReadableByteChannel advertiseEndOfBlock(InputStream decodedIn) throws IOException {
        org.apache.hadoop.fs.Seekable s = (org.apache.hadoop.fs.Seekable)decodedIn;

        long[] decodedStartPos = new long[] { s.getPos() };

        // We need to check one byte in advance to detect block boundaries
        PushbackInputStream pushbackIn = new PushbackInputStream(decodedIn, 1);

        // Decode *exactly* a single block:
        // The way the bzip2 codec works is that only *AFTER* reading one byte into the
        // next block the read method returns with the new position advertised.
        // However, we must avoid to read this one byte too many.
        // For this reason we use a pushback inputstream in order to read one byte ahead
        // and then decide whether it needs to be emitted or suppressed.
        ReadableByteChannel wrapper = new ReadableByteChannelDecoratorBase<ReadableByteChannel>(Channels.newChannel(pushbackIn)) {
            @Override
            public int read(ByteBuffer byteBuffer) throws IOException {

                int backupPos = byteBuffer.position();
                byte before = byteBuffer.get(backupPos);

                int result = super.read(byteBuffer);

                // If only a single byte was read and the position changed then
                // undo the read and indicate end-of-block (file)
                if (result == 1) {
                    long decodedPos = s.getPos();
                    boolean change = decodedStartPos[0] != decodedPos;

                    if (change) {
                        // Unread the byte
                        byte after = byteBuffer.get(backupPos);
                        pushbackIn.unread(after);

                        // Revert the buffer state
                        byteBuffer.put(backupPos, before);
                        byteBuffer.position(backupPos);

                        decodedStartPos[0] = decodedPos;

                        result = -1;
                    }
                }
                return result;
            }
        };
        return wrapper;
    }

}
