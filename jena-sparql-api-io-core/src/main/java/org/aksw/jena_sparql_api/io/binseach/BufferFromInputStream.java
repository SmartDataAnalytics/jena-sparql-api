package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.concurrent.ThreadSafe;

import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.jena_sparql_api.io.api.ChannelFactory;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

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
    implements ChannelFactory<Seekable>
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

    protected int minReadSize;

    /** Maximum number to read from the dataSupplier in one request */
    protected int maxReadSize;

    /**
     * Flag to indicate that the dataSupplier has been consumed
     * This is the case when dataSupplier(buffer) returns -1
     */
    protected boolean isDataSupplierConsumed;


    public long getKnownDataSize() {
        return knownDataSize;
    }

    public boolean isDataSupplierConsumed() {
        return isDataSupplierConsumed;
    }


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
        @Override
        public String toString() {
            return "BucketPointer [idx=" + idx + ", pos=" + pos + "]";
        }
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
        implements SeekableByteChannel, Seekable
    {
        protected long pos = 0;

        // The pointer remain null as long as the position could not be
        // converted to a valid pointer into the buckets
        protected BucketPointer pointer = null;

        public ByteArrayChannel(long pos, BucketPointer pointer) {
            this.pos = pos;
            this.pointer = pointer;
        }

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

        // @Override
//        public int compareToPrefix(byte[] prefix) throws IOException {
//        	loadDataUpTo(this.pos + prefix.length);
//
//        	if(pointer == null) {
//        		pointer = getPointer(buckets, activeEnd, pos);
//        		if(pointer == null) {
//        			throw new RuntimeException("Outside of range")
//        		}
//        	}
//
//        	int i = pointer.idx;
//        	int p = pointer.pos;
//
//        	int n = prefix.length;
//        	for(int i = 0; i < n; ++i) {
//        		byte a = prefix[i];
//
//
//
//
//        	}
//
//        }


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

        /**
         * First checks whether there is at least one more byte available
         * and afterwards returns the currently known size
         */
        @Override
        public long size() throws IOException {
            // ensureCapacityInActiveBucket();
            checkNext(1, false);

            // loadDataUpTo(Long.MAX_VALUE);
            return knownDataSize;
        }

        /** Loads all data into the buffer and returns the total size */
        // @Override
        public long loadAll() throws IOException {
            loadDataUpTo(Long.MAX_VALUE);
            return knownDataSize;
        }


        @Override
        public SeekableByteChannel truncate(long size) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Seekable clone() {
            return new ByteArrayChannel(pos, pointer);
        }

        @Override
        public long getPos() throws IOException {
            return position();
        }

        @Override
        public void setPos(long pos) throws IOException {
            position(pos);
        }

        @Override
        public void posToStart() throws IOException {
            position(-1);
        }

        /**
         * Set the position to the end of the stream
         * immediately loads all data.
         */
        @Override
        public void posToEnd() throws IOException {
            loadDataUpTo(Long.MAX_VALUE);
            pos = knownDataSize;
            pointer = null;
        }

        @Override
        public boolean isPosBeforeStart() throws IOException {
            return pos < 0;
        }

        @Override
        public boolean isPosAfterEnd() throws IOException {
            loadDataUpTo(pos + 1);

            boolean result = pos >= knownDataSize;
            return result;
        }

//        @Override
//        public int checkNext(int len) throws IOException {
//            throw new UnsupportedOperationException();
//        }

        /*
         * TODO This method does not update the pointer
        @Override
        public boolean prevPos(int len) throws IOException {
            long newPos = pos - len;
            boolean result = newPos >= 0;
            if(result) {
                pos -= len;
            }
            return result;
        }
        */


//        @Override
//        public int posToNext(byte delimiter, boolean changePos) throws IOException {
//            int result = 0;
//
//            long tmpPos = pos;
//            outer: for(;;) {
//                loadDataUpTo(tmpPos + 1); // TODO Ensure we preload a good chunk
//                if(this.pointer == null) {
//                    this.pointer = getPointer(buckets, activeEnd, pos);
//                }
//
//                byte[] currentBucket = buckets[pointer.idx];
//
//                // Copy the end marker to avoid race conditions when reading
//                // its two attributes
//                BucketPointer end = activeEnd;
//
//                boolean isInLastBucket = pointer.idx == end.idx;
//                int remainingBucketLen = isInLastBucket
//                    ? end.pos - pointer.pos
//                    : currentBucket.length - pointer.pos
//                    ;
//
//                for(int i = 0; i < remainingBucketLen; ++i) {
//                    if(currentBucket[i] == delimiter) {
//                        tmpPos += i;
//                        break outer;
//                    }
//                }
//            }
//
//            return result;
//        }

//        @Override
//        public int checkPrev(int len) throws IOException {
//            throw new UnsupportedOperationException();
//        }

        @Override
        public String readString(int len) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int checkNext(int len, boolean changePos) throws IOException {
            // Remaining bytes between pos and knowDataSize
            long remainingKnownBytes = knownDataSize - 1 - pos;
            if (remainingKnownBytes < len) {
                loadDataUpTo(pos + len);

                remainingKnownBytes = knownDataSize - 1 - pos;
            }

            int r = Math.min(len, Ints.saturatedCast(remainingKnownBytes));
//            if(r == 0) {
//                System.out.println("reached end");
//            }

            if (changePos) {
//                if(r <= 0) {
//                    pos = knownDataSize;
//                }

                if(pointer != null) {
                    int remaining = r;

                    for(;;) {
                        int available = buckets[pointer.idx].length - 1 - pointer.pos;
                        if(remaining > available) {
                            //int d = available - pointer.pos;
                            remaining -= available;
                            ++pointer.idx;
                            pointer.pos = -1;
                        } else {
                            pointer.pos += remaining;
                            break;
                        }
                    }
                }

                pos += r;
            }

            return r;
        }

        @Override
        public byte get() throws IOException {
            if (pointer == null) {
                loadDataUpTo(pos);
                pointer = getPointer(buckets, activeEnd, pos);
            }

            // Corner case: if we are positioned at the end of the bucket
            // we invoke read which does all this complex handling
            byte result;
            if (pointer.pos == buckets[pointer.idx].length) {
                ByteBuffer tmp = ByteBuffer.allocate(1);
                read(tmp);
                result = tmp.get(0);
            } else {
                result = buckets[pointer.idx][pointer.pos];
            }
            return result;
        }

        /**
         * The method assumes that the current position is in the valid range
         *
         */
        @Override
        public int checkPrev(int len, boolean changePos) throws IOException {
            long delta = len > pos ? pos : len;
            if (changePos) {
                // Update the pointer (if it was set)
                if(pointer != null) {
                    int remaining = Ints.checkedCast(delta);

                    //while(remaining > 0) {
                    for(;;) {
                        if(remaining > pointer.pos) {
                            if(pointer.idx == 0) {
                                pointer.pos = 0;
                                break;
                            } else {
                                remaining -= pointer.pos;
                                --pointer.idx;
                                pointer.pos = buckets[pointer.idx].length;
                            }
                        } else {
                            pointer.pos -= remaining;
                            break;
                        }
                    }
                }

                // pointer = getPointer(buckets, activeEnd, delta);
                pos -= delta;
            }

            return (int)delta;
        }
    }

    public Seekable newChannel() {
        return new ByteArrayChannel(0, null);
    }

    public BufferFromInputStream(
            int initialBucketSize,
            InputStream dataSupplier) {
        if (initialBucketSize <= 0) {
            throw new IllegalArgumentException("Bucket size must not be 0");
        }

        this.buckets = new byte[8][];
        buckets[0] = new byte[initialBucketSize];
        this.dataSupplier = dataSupplier;
        this.minReadSize = 8192;
        this.maxReadSize = 8192;
        this.activeEnd = new BucketPointer(0, 0);
    }

    protected int nextBucketSize() {
        long activeSize = buckets[activeEnd.idx].length;

        int maxBucketSize = Integer.MAX_VALUE / 2;
        int nextSize = Math.min(Ints.saturatedCast(activeSize * 2), maxBucketSize);
        return nextSize;

    }

    public int doRead(ByteArrayChannel reader, ByteBuffer dst) {
        int result = 0;

        BucketPointer pointer = reader.pointer;
        if (pointer == null) {
            BucketPointer end = activeEnd;

            // Try to translate the logical linear position to a physical pointer
            pointer = getPointer(buckets, end, reader.pos);
            if (pointer == null) {
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
                if(result == 0) {
                    result = -1;
                }
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

            if (remainingBucketLen == 0) {
                if (isInLastBucket) {
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


    /**
     * Preload data up to including the requested position.
     * It is inclusive in order to allow for checking whether the requested position is in range.
     *
     * @param requestedPos
     */
    protected void loadDataUpTo(long requestedPos) {
        while (!isDataSupplierConsumed && knownDataSize <= requestedPos) {
            synchronized(this) {
                if (!isDataSupplierConsumed && knownDataSize <= requestedPos) {
                    // System.out.println("load upto " + requestedPos);
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

            len = Math.max(len, minReadSize);
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
                    throw new IllegalStateException("Invalid return value: " + n);
                }
            }
        }
    }

    protected void ensureCapacityInActiveBucket() {
        byte[] activeBucket = buckets[activeEnd.idx];
        int capacity = activeBucket.length - activeEnd.pos;
        if (capacity == 0) {
            int nextBucketSize = nextBucketSize();
            if(nextBucketSize == 0) {
                throw new IllegalStateException("Bucket of size 0 generated");
            }

            int newEndIdx = activeEnd.idx + 1;
            if (newEndIdx >= buckets.length) {
                // Double number of buckets
                int numNewBuckets = buckets.length * 2;
                byte[][] newBuckets = new byte[numNewBuckets][];
                System.arraycopy(buckets, 0, newBuckets, 0, buckets.length);
                buckets = newBuckets;
            }

            // Allocate a new bucket
            // System.out.println("Allocating " + nextBucketSize);
            buckets[newEndIdx] = new byte[nextBucketSize];
            activeEnd = new BucketPointer(newEndIdx, 0);
        }
    }


    public static void main(String[] args) throws Exception {
        int n = 10000;
        byte[] data = new byte[10 * n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < 10; ++j) {
                data[i * 10 + j] = (byte)((byte)'a' + j);
            }
        }

        InputStream in = new ByteArrayInputStream(data);
        BufferFromInputStream b = BufferFromInputStream.create(in, 2);
        Seekable s = b.newChannel();

        // System.out.println("Pos changed? " + s.nextPos(10000 * 10 - 1));
        ByteBuffer buf = ByteBuffer.allocate(5);

        // IOUtils.read(s, buf);
        s.nextPos(5001);
        s.read(buf);
        // s.read(buf);
        System.out.println(Arrays.toString(buf.array()));

        for (int i = 0; i < 10; ++i) {

            long pos = s.getPos();
            byte ch = s.get(i);
            System.out.println("i: " + i + " pos: " + pos + " ch: " + ch);
        }

    }


    public static void main2(String[] args) throws Exception {

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
                        try(Seekable channel = buffer.newChannel()) {
                            channel.setPos(start);
                            while(channel.read(buf) > 0);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    // Assert.assertArrayEquals(expectedData, actualData);
                    if (expectedData.equals(actualData)) {
                        throw new RuntimeException("Actual and expected results differed");
                    }
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
