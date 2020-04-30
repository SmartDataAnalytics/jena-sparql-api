package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for purely relative navigation along data which allows for navigating along
 * data of unknown or infinite size.
 *
 * There are two related main features of this interface / trait:
 * One is is that it common matching methods are part of the interface - with emphasis on binary search.
 * The other is, that due to this integrated functionality, internal structures can be abstracted:
 * A matcher can thus transparently run over a sequence of internal blocks without exposing these details.
 *
 * Implementation can provide their own optimized overrides of these matchers, thus
 * significantly speeding up lookups.
 *
 * Methods that perform matching can move the position one byte before or after the
 * backing data region by means of reducing the amount of copying of byte arrays.
 *
 *
 * @author raven
 *
 */
public interface Seekable
    extends ReadableByteChannel
{
    Seekable clone();

    /**
     * Default method to work around scala bug
     * https://github.com/scala/bug/issues/10501
     *
     * @return
     */
    default Seekable cloneObject() {
        return clone();
    }

    /**
     * Optional operation.
     * Get the position in this seekable
     *
     * @return
     */
    long getPos() throws IOException;

    /**
     * Optional operation.
     * Get the position in this seekable
     *
     * @return
     */
    void setPos(long pos) throws IOException;


    /**
     * Optional operation.
     * Move one unit before the start of the seekable; raises an exception on infinite seekables
     *
     * @return
     */
    void posToStart() throws IOException;

//    void posToBeforeStart() throws IOException;

    /**
     * Optional operation.
     * Move to one unit beyond the end of the seekable; raises an exception on infinite seekables
     *
     * @return
     */
    void posToEnd() throws IOException;

    /**
     * Move to 1 byte beyond the know end
     *
     * @throws IOException
     */
//    void posToAfterEnd() throws IOException;


    /**
     * Read a byte at the current position
     *
     * @return The byte at the current position if the position is valid
     * @throws IOException
     */
    // byte get() throws IOException;
    default byte get() throws IOException {
        // if(!isPosBeforeStart() && !isPosAfterEnd()) {
        long pos = getPos();
        byte[] arr = new byte[1];
        read(ByteBuffer.wrap(arr));
        byte result = arr[0];
        setPos(pos);

        return result;
    }


    /**
     * The state of a seekable may be one unit before the start.
     * In this state, if the seekable is non-empty, nextPos(1) must be a valid position
     *
     * @return
     */
    boolean isPosBeforeStart() throws IOException;

    /**
     * The state of a seekable may be one unit beyond the end.
     * In this state, if the seekable is non-empty, prevPos(1) must be a valid position
     * @return
     */
    boolean isPosAfterEnd() throws IOException;

    /**
     * Attempt to advance the position by the given number of bytes.
     *
     * Argument must not be negative.
     *
     * @param len
     * @return True if the position was changed by the requested amount of bytes. False means that the position was unchanged.
     */
    boolean nextPos(int len) throws IOException;

    /**
     * Attempt to step back the position by the given number of bytes.
     * Argument must not be negative.
     *
     * @param len
     * @return True if the position was changed by the *requested* amount of bytes. False means that the position was unchanged.
     */
    boolean prevPos(int len) throws IOException;


    /**
     * Relative positioning. Delegates to nextPos or prevPos based on sign of delta.
     *
     * @param delta
     * @return
     * @throws IOException
     */
    default boolean deltaPos(int delta) throws IOException {
        boolean result = delta > 0
                ? nextPos(delta)
                : delta < 0
                    ? prevPos(-delta)
                    : true; // we requested a delta of 0 and we succeeded to fulfill it

        return result;
    }

    /**
     * Attempt to read bytes at the current position without altering the position
     *
     * @param dst
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    default int peekNextBytes(byte[] dst, int offset, int len) throws IOException {
        int n = 0;
        if(!isPosAfterEnd()) {
            for(; n < len; ++n) {
                byte c = get();
                dst[offset + n] = c;
                if(!nextPos(1)) {
                    break;
                }
            }
        }
        prevPos(n);
        return n;
    }


    /**
     * Move the position to the next delimiter if it exists,
     * or one element past the end of data such that isPosAfterEnd() yields true.
     * Position is unchanged if already at a delimiter
     *
     * @param delimiter
     * @return true if the position was changed, false otherwise
     * @throws IOException
     */
    default boolean posToNext(byte delimiter) throws IOException {
        // potential HACK: Changing the position is not required to preload any data
        // hence, isPosAfterEnd may be true without a read attempt
        boolean result = false;
        while(!isPosAfterEnd() && get() != delimiter) {
            boolean posChanged = nextPos(1);
            if(!posChanged) {
                posToEnd();
                break;
            }
            result = true;
        }
        return result;
    }

    /**
     * Move the position to the previous delimiter if it exists,
     * or one element past the end of data such that isPosBeforeStart() yields true.
     * Position is unchanged if already at delimiter
     *
     *
     * @param delimiter
     * @return true if the position was changed, false otherwise
     * @throws IOException
     */
    default boolean posToPrev(byte delimiter) throws IOException {
        boolean result = false;
        while(!isPosBeforeStart() && get() != delimiter) {
            boolean posChanged = prevPos(1);
            if(!posChanged) {
                posToStart();
                break;
            }
            result = true;
        }
        return result;
    }


    @Override
    default int read(ByteBuffer dst) throws IOException {
        if(true) {
            throw new RuntimeException("This code is bugged because it does not return -1 on end");
        }
        int n = 0;
        if(!isPosAfterEnd()) {
            while(dst.remaining() > 0) {
                byte c = get();
                dst.put(c);
                ++n;
                if(!nextPos(1)) {
                    break;
                }
            }
        }

        return n;
    }


    // Convenience method
    String readString(int len) throws IOException;

    /**
     * Compare the bytes at the current position to a given sequence of bytes
     * If there are fewer bytes available in the seekable than provide for comparison,
     * then only that many are compared.
     *
     * This default implementation uses read(ByteBuffer).
     * Other implementations may override this behavior to compare the given prefix
     * directly against their internal data structures without the
     * intermediate buffer copy due to read.
     *
     * @param prefix
     * @return
     * @throws IOException
     */
    default int compareToPrefix(byte[] prefix) throws IOException {
        int n = prefix.length;
        byte[] buf = new byte[n];
        long pos = getPos();
        read(ByteBuffer.wrap(buf));

        int result = compareArrays(buf, prefix);

        // Reset position
        setPos(pos);

        return result;
    }


    // Throw an exception if lengths differ?
    public static int compareArrays(byte[] a, byte[] b) {
        int n = Math.min(a.length, b.length);

        int result = 0;
        for(int i = 0; i < n && result == 0; ++i) {
            byte x = a[i];
            byte y = b[i];
            result = Byte.compare(x, y);
        }

        // System.out.println(new String(a) + " " + (result < 0 ? "<" : result > 0 ? ">" : "=") + " " + new String(b));

        return result;
    }

    /**
     * Move the position to the next match
     *
     * TODO This we way we create a new matcher on every invocation which is far from optimal.
     * The only approach I see is to have the Matcher integrated into the seekable - this way
     * the seekable would have to expose posToPrevMatch() / posToNextMatch() methods.
     *
     *
     *
     * Note, that pattern usually uses Boyer-Moore if the pattern permits it,
     * so matching with it is very fast
     *
     * @param pattern
     * @return
     * @throws IOException
     */
//    default boolean posToNext(Pattern pattern) throws IOException {
//        boolean result = false;
//        try(Seekable clone = this.clone()) {
//            CharSequenceFromSeekable cs = new CharSequenceFromSeekable(clone);
//            Matcher m = pattern.matcher(cs);
//            if(m.find()) {
//                int start = m.start();
//                nextPos(start);
//            } else {
//                posToEnd();
//            }
//        }
//
//        return result;
//    }

//    default boolean posToPrev(Pattern pattern) throws IOException {
//        boolean result = false;
//        try(Seekable clone = this.clone()) {
//            CharSequenceFromSeekable cs = new CharSequenceFromSeekable(clone);
//            Matcher m = pattern.matcher(cs);
//            if(m.find()) {
//                int start = m.start();
//                nextPos(start);
//            } else {
//                posToEnd();
//            }
//        }
//
//        return result;
//    }



    /**
     * Delimiter-based binary search.
     * delimiter must not appear in prefix
     *
     * Result is the position of the match or -1 if no match was found.
     * Position is set to the first match.
     * TODO Position is undefined if there was no match - which is not optimal - we might want to reset it in that case
     *
     * To reiterate: For fwd / bwd searches, the contract is to move to the next match or beyond the start/end of the stream
     * We might want to change it that if there is a match, then move to it and return true, otherwise leave the position unchanged and return false
     *
     * @param min
     * @param max
     * @param delimiter
     * @param prefix
     * @return
     * @throws IOException
     */
    //long binarySearch(long min, long max, byte delimiter, byte[] prefix) throws IOException;

    default long binarySearch(long min, long max, byte delimiter, byte[] prefix) throws IOException {

        long middlePos = (min + max) / 2;
        setPos(middlePos);

        if(isPosAfterEnd()) {
            return Long.MIN_VALUE;
        }

        posToPrev(delimiter);
        long delimPos = getPos();

        // If the delimPos has not progressed over min then there is no match
        if(delimPos < min || min >= max) {
            return Long.MIN_VALUE;
        }

        // long lineStart = getPos();
        //long lineStart = delimPos + 1;
        nextPos(1);
        int cmp = compareToPrefix(prefix);

        // System.out.println(min + " - " + max);
//        String l = readLine();
//        System.out.println("Comparison of line at range " + min + " - " + max);
//        System.out.println("    " + l);
//        System.out.println("  and");
//        System.out.println("    " + new String(prefix));
//        System.out.println("  resulted in " + cmp);


        // if we have a byte comparison such as
        // [3].compareToPrefix([5]) which yields -1, then we need to search in
        // the higher segment
        long result;
        if(cmp == 0) {
            result = delimPos;
        } else if(cmp < 0) {
            long nextDelimPos;
            setPos(delimPos + 1);
            posToNext(delimiter);
            nextDelimPos = getPos();

            result = binarySearch(nextDelimPos, max, delimiter, prefix);
        } else { // if cmp > 0
            result = binarySearch(min, delimPos - 1, delimiter, prefix);
        }

        return result;
    }


    default long size() throws IOException {
        return -1;
    }
}
