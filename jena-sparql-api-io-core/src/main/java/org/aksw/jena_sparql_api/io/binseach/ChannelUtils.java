package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

public class ChannelUtils {

    /**
     * Read fully from a src channel at position srcPosition into a dst buffer.
     * After this method returns src.position is reset to its original value
     */
    public static int readFully(SeekableByteChannel src, ByteBuffer dst, long srcPosition) throws IOException {
        long posBackup = src.position();
        src.position(srcPosition);
        int result = readFully(src, dst);
        src.position(posBackup);
        return result;
    }

    /** Method akin to Apache IOUtils.readFully without sanity checking */
    public static int readFully(ReadableByteChannel src, ByteBuffer dst) throws IOException {
        int count = 0;
        while (dst.remaining() > 0) {
            int n = src.read(dst);
            if (n < 0) {
                break;
            } else {
                count += n;
            }
        }

        return count;
    }

    public static int writeFully(WritableByteChannel tgt, ByteBuffer buffer) throws IOException {
        int result = 0;
        while (buffer.remaining() > 0) {
            result += tgt.write(buffer);
        }
        return result;
    }

    public static int read(SeekableByteChannel src, ByteBuffer dst, long position) throws IOException {
        long posBackup = src.position();
        src.position(position);

        int result = src.read(dst);
        src.position(posBackup);
        return result;
    }

    public static int write(SeekableByteChannel dst, ByteBuffer src, long position) throws IOException {
        long posBackup = dst.position();
        dst.position(position);

        int result = dst.write(src);
        dst.position(posBackup);
        return result;
    }

    public static long transferTo(
            SeekableByteChannel src,
            long position,
            long count,
            WritableByteChannel
            target,
            int blockSize) throws IOException {
        long posBackup = src.position();

        src.position(position);

        ByteBuffer buffer = ByteBuffer.allocate(blockSize);
        long done = 0;
        long remaining;
        while ((remaining = count - done) > 0) {
            buffer.position(0);

            int limit = remaining > blockSize ? blockSize : (int)remaining;
            buffer.limit(limit);

            int n = readFully(src, buffer);
            if (n == 0) {
                break;
            } else {
                done += n;
            }

            buffer.position(0);
            buffer.limit(n);
            writeFully(target, buffer);
        }

        src.position(posBackup);

        return done;
    }

    public static long transferFrom(SeekableByteChannel dst, ReadableByteChannel src, long position, long count, int blockSize) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(blockSize);

        long posBackup = dst.position();
        dst.position(position);

        long done = 0;
        long remaining;
        while ((remaining = count - done) > 0) {
            buffer.position(0);

            int limit = remaining > blockSize ? blockSize : (int)remaining;
            buffer.limit(limit);

            int contentSize = readFully(src, buffer);
            if (contentSize == 0) {
                break;
            }
            done += contentSize;

            buffer.position(0);
            buffer.limit(contentSize);
            writeFully(dst, buffer);
        }

        dst.position(posBackup);

        return done;
    }

    public static long readScattered(ReadableByteChannel src, ByteBuffer[] dsts, int offset, int length) throws IOException {
        long result = 0;
        for (int i = offset; i < length; ++i) {
            ByteBuffer dst = dsts[i];
            int n = readFully(src, dst);
            if (n == 0) {
                break;
            }
            result += n;
        }
        return result;
    }

    public static long writeScattered(WritableByteChannel dst, ByteBuffer[] srcs, int offset, int length) throws IOException {
        long result = 0;
        for (int i = offset; i < length; ++i) {
            ByteBuffer src = srcs[i];
            int n = writeFully(dst, src);
            if (n == 0) {
                break;
            }
            result += n;
        }
        return result;
    }

}