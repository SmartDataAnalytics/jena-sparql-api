package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.junit.Assert;

import com.github.jsonldjava.shaded.com.google.common.collect.Range;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

interface ChannelFactory<T extends Channel>
    extends AutoCloseable
{
    T newChannel();
}

/**
 * Implementation of a byte array that caches data in buckets from
 * an InputStream.
 *
 * Instances of these class are thread safe, but the obtained channels are not; each channel should only be operated on
 * by one thread.
 *
 * Differences to BufferedInputStream
 * - this class caches all data read from the inputstream hence there is no mark / reset mechanism
 * - buffer is split into buckets (no data copying required when allocating more space)
 * - data is loaded on demand based on (possibly concurrent) requests to the seekable channels obtained with
 *   newChannel()
 *
 * Closest known-to-me Hadoop counterpart is BufferedFSInputStream (which is based on BufferedInputStream)
 *
 * @author raven
 *
 */
@ThreadSafe
public class BufferFromInputStream
    implements ChannelFactory<ReadableByteChannel>
{
    /** The buffered data */
    protected byte[][] buckets;

    /**
     * End marker with two components (idx, pos)
     *
     * it is wrapped in an object to enable atomic replacement of the reference
     * The pointer is monotonous in the sense that the end marker's logical linear location is only increased
     * Reading an old version while a new one has been set will only cause a read
     * to return on the old boundary, but a subsequent synchronized check for whether loading
     * of additional data is needed is then made anyway
     */
    protected BucketPointer activeEnd;

    /** The number of cached bytes. Corresponds to the linear representation of activeEnd.  */
    protected long knownDataSize = 0;


    /** Supplier for additional data */
    protected InputStream dataSupplier;

    /** Maximum number to read from the dataSupplier in one request */
    protected int maxReadSize;

    /**
     * Flag to indicate that the dataSupplier has been consumed
     * This is the case when dataSupplier(buffer) returns -1
     */
    protected boolean isDataSupplierConsumed;


    /**
     * @param maxReadSize Maximum number of bytes to request form the input stream at once
     *
     * @param in
     * @param maxReadSize
     * @param preconfiguredBucketSizes
     * @return
     */
    public static BufferFromInputStream create(InputStream in, int maxReadSize, int ... preconfiguredBucketSizes) {
        BufferFromInputStream result = new BufferFromInputStream(maxReadSize, in);
        return result;
    }


    public static long getPosition(byte[][] buckets, int idx, int pos) {
        long result = 0;
        for(int i = 0; i < idx; ++i) {
            result += buckets[i].length;
        }

        result += pos;
        return result;
    }

    public static class BucketPointer {
        public BucketPointer(int idx, int pos) {
            super();
            this.idx = idx;
            this.pos = pos;
        }

        int idx;
        int pos;
    }


    /**
     *
     *
     * @param buckets
     * @param pos
     * @return Pointer to a valid location in the know data block or null
     */
    public static BucketPointer getPointer(byte[][] buckets, BucketPointer end, long pos) {
        long tmp = pos;
        int i;

        int eidx = end.idx;
        int epos = end.pos;
        for(i = 0; i < eidx; ++i) {
            byte[] bucket = buckets[i];
            int n = bucket.length;
            long r = tmp - n;
            if(r < 0) {
                break;
            } else {
                tmp -= n;
            }
        }

        BucketPointer result = i == end.idx && tmp > epos
                ? null
                : new BucketPointer(i, Ints.checkedCast(tmp));
        return result;
    }

    public class ByteArrayChannel
        extends ReadableByteChannelBase
        implements SeekableByteChannel
    {
        protected long pos = 0;

        // The pointer remain null as long as the position could not be
        // converted to a valid pointer into the buckets
        protected BucketPointer pointer = null;

        /**
         * Setting a position outside of the size of the entity
         * is valid will will cause a read to immediately return an EOF
         * See {@link ReadableByteChannel}
         *
         * @param pos
         * @return
         */
        @Override
        public ByteArrayChannel position(long pos) {
            this.pos = pos;
            this.pointer = null;
            return this;
        }

        @Override
        public long position() {
            return this.pos;
        }

        @Override
        public int readActual(ByteBuffer dst) throws IOException {
            int result = doRead(this, dst);
            return result;
        }

        // TODO We could allow writes to the buffer in the future

        @Override
        public int write(ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long size() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public SeekableByteChannel truncate(long size) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    public SeekableByteChannel newChannel() {
        return new ByteArrayChannel();
    }

    public BufferFromInputStream(
            int initialBucketSize,
            InputStream dataSupplier) {
        if(initialBucketSize <= 0) {
            throw new IllegalArgumentException("Bucket size must not be 0");
        }

        this.buckets = new byte[8][];
        buckets[0] = new byte[initialBucketSize];
        this.dataSupplier = dataSupplier;
        this.maxReadSize = 8192;
        this.activeEnd = new BucketPointer(0, 0);
    }

    protected int nextBucketSize() {
        int activeSize = buckets[activeEnd.idx].length;

        int nextSize = Ints.saturatedCast(activeSize * 2);
        return nextSize;

    }

    public int doRead(ByteArrayChannel reader, ByteBuffer dst) {
        int result = 0;

        BucketPointer pointer = reader.pointer;
        if(pointer == null) {
            BucketPointer end = activeEnd;

            // Try to translate the logical linear position to a physical pointer
            pointer = getPointer(buckets, end, reader.pos);
            if(pointer == null) {
                if(isDataSupplierConsumed) {
                    return -1;
                } else {
                    long requestedPos = reader.pos;
                    loadDataUpTo(requestedPos);
                    end = activeEnd;
                    pointer = getPointer(buckets, end, reader.pos);

                    if(pointer == null) {
                        if(isDataSupplierConsumed) {
                            return -1;
                        } else {
                            throw new IllegalStateException("Should not happen: Could not map pointer position despite all data known");
                        }
                    }
                }
            }

            // Cache a valid pointer with the channel
            reader.pointer = pointer;
        }

        int bucketIdx = pointer.idx;
        int bucketPos = pointer.pos;

        for(;;) {
            int remainingDstLen = dst.remaining();
            if(remainingDstLen == 0) {
                break;
            }

            byte[] currentBucket = buckets[bucketIdx];

            // Copy the end marker to avoid race conditions when reading
            // its two attributes
            BucketPointer end = activeEnd;

            boolean isInLastBucket = bucketIdx == end.idx;
            int remainingBucketLen = isInLastBucket
                ? end.pos - bucketPos
                : currentBucket.length - bucketPos
                ;

            if(remainingBucketLen == 0) {
                if(isInLastBucket) {
                    if(result != 0) {
                        // We have already read something on this iteration, return
                        break;
                    } else {
                        // We reached the bucket end and have not read anything so far
                        if(!isDataSupplierConsumed) {
                            synchronized(this) {
                                if (bucketPos == end.pos && bucketIdx == end.idx && !isDataSupplierConsumed) {
                                    loadData(dst.limit());
                                    continue;
                                }
                            }
                        } else {
                            result = -1;
                        }
                    }
                } else {
                    ++bucketIdx;
                    bucketPos = 0;
                    continue;
                }
            }

            int n = Math.min(remainingDstLen, remainingBucketLen);
            dst.put(currentBucket, bucketPos, n);
            result += n;
            pointer.pos = bucketPos += n;
            reader.pos += n;
            pointer.idx = bucketIdx;
            //pos += n;
        }

        return result;
    }


    protected void loadDataUpTo(long requestedPos) {
        while(!isDataSupplierConsumed && knownDataSize < requestedPos) {
            synchronized(this) {
                if(!isDataSupplierConsumed && knownDataSize < requestedPos) {
                    int needed = Ints.saturatedCast(requestedPos - knownDataSize);
                    loadData(needed);
                }
            }
        }
    }

    /**
     * fetch a chunk from the input stream
     */
    protected void loadData(int needed) {
        if (!isDataSupplierConsumed) {
            ensureCapacityInActiveBucket();

            byte[] activeBucket = buckets[activeEnd.idx];
            int len = Math.min(needed, maxReadSize);
            len = Math.min(len, activeBucket.length - activeEnd.pos);

            if(len != 0) {
                int n;
                try {
                    n = dataSupplier.read(activeBucket, activeEnd.pos, len);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if(n > 0) {
                    activeEnd.pos += n;
                    knownDataSize += n;
                } else if (n == -1) {
                    isDataSupplierConsumed = true;
                } else if (n == 0) {
                    throw new IllegalStateException("Data supplier returned 0 bytes");
                } else {
                    throw new IllegalStateException("Invalid return value");
                }
            }
        }
    }

    protected void ensureCapacityInActiveBucket() {
        byte[] activeBucket = buckets[activeEnd.idx];
        int capacity = activeBucket.length - activeEnd.pos;
        if(capacity == 0) {
            int nextBucketSize = nextBucketSize();
            if(nextBucketSize == 0) {
                throw new IllegalStateException("Bucket of size 0 generated");
            }

            int newEndIdx = activeEnd.idx + 1;
            if(newEndIdx >= buckets.length) {
                // Double number of buckets
                int numNewBuckets = buckets.length * 2;
                byte[][] newBuckets = new byte[numNewBuckets][];
                System.arraycopy(buckets, 0, newBuckets, 0, buckets.length);
                buckets = newBuckets;
            }

            // Allocate a new bucket
            buckets[newEndIdx] = new byte[nextBucketSize];
            activeEnd = new BucketPointer(newEndIdx, 0);
        }
    }



    public static void main(String[] args) throws Exception {

        // TODO Create a test case:
        // Put some data into a byte array, wrap it with a ByteArayInputStream which
        // is then wrapped with the BufferFromInputStream
        // Then let threads read random data ranges and compare the same ranges with the original
        // byte array

        Random rand = new Random(0);
        Stopwatch sw = Stopwatch.createUnstarted();

        for(int i = 0; i < 1000; ++i) {

            if(i == 100) {
                sw.start();
            }

            // System.out.println("Run #" + i);

            int dataLength = rand.nextInt(10000);
            byte[] baseData = new byte[dataLength];
            rand.nextBytes(baseData);

            int maxReadLength = rand.nextInt(1000) + 1;

            InputStream in = new ByteArrayInputStream(baseData);
            BufferFromInputStream buffer = BufferFromInputStream.create(in, maxReadLength);

            // Test 100 ranges in parallel
            IntStream.range(0, 1000).mapToObj(x -> x).collect(Collectors.toList()).stream()
                .map(x -> {
                    int start = rand.nextInt(dataLength);
                    int tmpLen = rand.nextInt(dataLength / 2);
                    int end = Math.min(start + tmpLen, baseData.length);
                    int len = end - start;
                    // System.out.println("  Range #" + x + ": start=" + start + " len=" + len);

                    byte[] expectedData = new byte[len];
                    System.arraycopy(baseData, start, expectedData, 0, len);

                    byte[] actualData = new byte[len];
                    try {
                        ByteBuffer buf = ByteBuffer.wrap(actualData);
                        try(ReadableByteChannel channel = buffer.newChannel().position(start)) {
                            while(channel.read(buf) > 0);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    Assert.assertArrayEquals(expectedData, actualData);
                    return "foo";
                }).count();

        }
        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);

/*
        Path ntFile = Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt");
        InputStream in = Files.newInputStream(ntFile, StandardOpenOption.READ);
        BufferFromInputStream loader = BufferFromInputStream.create(in, 8192);

        // cached input stream
        try(InputStream cin = Channels.newInputStream(loader.newChannel().position(80001))) {
            System.out.println(IOUtils.toString(new BoundedInputStream(cin, 1024), StandardCharsets.UTF_8));
        }

        System.out.println("REPEAT");
        try(InputStream cin = Channels.newInputStream(loader.newChannel().position(20))) {
            System.out.println(IOUtils.toString(new BoundedInputStream(cin, 1024), StandardCharsets.UTF_8));
        }
*/

    }


    @Override
    public void close() throws Exception {
        dataSupplier.close();
    }
}
