package org.aksw.commons.io.seekable.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/*
 *
 * checkNext(horizon, changePos); Return the number of available bytes up to the horizon.
 *   Optionally move as many bytes as possible
 *
 *
 * posToNext(delim, changePos):
 *
 * As a rule of thumb: use global position to 'jump' to a location (use case: binary search)
 *    for example, with fixed size pages, an absolute pos translates to an index in a page using
 *    (pos / pageSize, pos % pageSize).
 *
 * use relative position to 'iterate' to it (positioning is most likely within the same internal buffer)
 *
 */

/**
 * Interface that enables relative navigation over data of fixed finite
 * but possibly initially unknown size.
 * Start and end positions can be 'discovered' when a relative operation causes.
 * A Seekable is a ReadableByteChannel but in addition it includes methods for relative
 * seeks and pattern matching methods.
 *
 * The rationale is, that certain operations can be carried out faster if they are pushed
 * to underlying implementation.
 * For example, consider comparing a fixed sequence of bytes to a Seekable: Instead of repeatedly
 * requesting a copies of bytes from the channel, the comparision can be pushed to the seekable which
 * may find out it can delegate the request to an internal buffer. Or it may detect that the
 * operation crosses internal buffer boundaries and handle this case accordingly.
 * The other aspect is, that for relative seeks it is assumed that checking
 * the resources associated with the most recent position first is most likely to
 * generate lookup hits.
 * So with this we can skip a check for whether a global position change should be translated into a relative one.
 *
 *
 * A seekable is backed by a data supplying entity such as a byte array, a ByteBuffer, a FileChannel
 * or a composite thereof.
 *
 *
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
     * Get one byte relative to the current position
     * 
     * @param relPos
     * @return
     * @throws IOException
     */
    default byte get(int relPos) throws IOException {
        byte result;
        if(relPos > 0) {
            if (nextPos(relPos)) {
            	result = get();
            	prevPos(relPos);
            } else {
            	throw new RuntimeException("No data " + relPos + " byte from current position " + getPos());
            }
        } else if(relPos < 0) {
            int tmp = -relPos;
            if (prevPos(tmp)) {
            	result = get();
            	nextPos(tmp);
            } else {
            	throw new RuntimeException("No data " + relPos + " byte from current position " + getPos());            	
            }
        } else {
            result = get();
        }

        return result;
    }

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
     * If the position is valid before the call it will always be valid when
     * the call returns - i.e. in that case isPosBeforeStart and isPosAfterEnd will
     * always be false.
     * Argument must not be negative.
     *
     * @param len
     * @return True if the position was changed by the requested amount of bytes. False means that the position was unchanged.
     */
    default boolean nextPos(int len) throws IOException {
        int available = checkNext(len, false);
        boolean result = available == len;
        if(result) {
            checkNext(len, true);
        }
        return result;
    }

    /**
     * Attempt to advance the position by the given number of bytes.
     * Return the number of bytes by which the position was changed.
     * Returning less bytes than requested implies that a end position
     * was reached which cannot be passed. This method cannot pass
     * beyond the end - i.e. isPosAfterEnd cannot change from false
     * to true by  calling this method.
     *
     *
     * @param len
     * @return
     * @throws IOException
     */
    int checkNext(int len, boolean changePos) throws IOException;


    /**
     * Attempt to step back the position by the given number of bytes.
     * Argument must not be negative.
     *
     * @param len
     * @return True if the position was changed by the *requested* amount of bytes. False means that the position was unchanged.
     */
    default boolean prevPos(int len) throws IOException {
        int r = checkPrev(len, false);
        boolean result = r == len;
        if(result) {
            checkPrev(len, true);
        }

        return result;
    }

    int checkPrev(int len, boolean changePos) throws IOException;

    /**
     * Attempt to advance the position in backward direction by the given number of bytes.
     * Return the number of bytes by which the position was changed.
     * Returning less bytes than requested implies that a start position
     * was reached which cannot be passed.
     *
     * @param len
     * @return
     * @throws IOException
     */
//    default int forcePrevPos(int len) throws IOException {
//        int r = checkPrev(len);
//        prevPos(r);
//        return r;
//    }

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
     * Move the position to the next delimiter if it exists.
     * Positive result is the number of bytes the position was advanced by this invocation.
     * Negative result indicates that the number of bytes until the end of the seekable - i.e.
     * within that number of bytes no match was found.
     *
     *
     *
     * Move the position to the next delimiter if it exists,
     * or one element past the end of data such that isPosAfterEnd() yields true.
     * Position is unchanged if already at a delimiter
     *
     * @param delimiter
     * @return true if the position was changed, false otherwise
     * @throws IOException
     */
    default boolean posToNext(byte delimiter) throws IOException {
        boolean result = false;
        for(;;) {
            boolean isPosAfterEnd = isPosAfterEnd();
            if(!isPosAfterEnd) {
                byte b = get();
                //System.out.println(b);
                if(b == delimiter) {
                    result = true;
                    break;
                }

                boolean posChanged = nextPos(1);
                if(!posChanged) {
                    posToEnd();
                    break;
                }
            } else {
                break;
            }
        }

        return result;

        // potential HACK: Changing the position is not required to preload any data
        // hence, isPosAfterEnd may be true without a read attempt
//        boolean result = false;
//        while(!isPosAfterEnd() && get() != delimiter) {
//            boolean posChanged = nextPos(1);
//            if(!posChanged) {
//                posToEnd();
//                break;
//            }
//            result = true;
//        }
//        return result;
    }

    /**
     *
     * 0: match but 0 bytes moved
     *
     * negative values indicate no match. add +1 to get the number of bytes moved:
     * -1: no match and 0 bytes moved
     * -10: no match and 9 bytes moved
     *
     *
     * @param delimiter
     * @param changePos If no delimiter is found, move the pos to the end
     * @return
     */
    default int posToNext(byte delimiter, boolean changePos) throws IOException {
        long startPos = getPos();
        byte[] buffer = new byte[1024];

        int delta = 0;
        boolean foundMatch = false;
        outer: for(;;) {
            int n = read(ByteBuffer.wrap(buffer));
            if(n < 0) {
                break;
            }
            for(int i = 0; i < n; ++i) {
                if(buffer[i] == delimiter) {
                    delta += i;
                    foundMatch = true;
                    break outer;
                }
            }
            delta += n;
        }

        int result;
        if(foundMatch) {
            result = delta;
            setPos(startPos + delta);
        } else {
            if(!changePos) {
                setPos(startPos);
            }
            result = -(delta + 1);
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
        for(;;) {
            boolean isBeforeStart = isPosBeforeStart();
            if(!isBeforeStart) {
                byte b = get();
                // System.out.println(b);
                if(b == delimiter) {
                    result = true;
                    break;
                }

                boolean posChanged = prevPos(1);
                if(!posChanged) {
                    posToStart();
                    break;
                }
            } else {
                break;
            }
        }

        return result;
//        while(!isPosBeforeStart() && get() != delimiter) {
//            boolean posChanged = prevPos(1);
//            if(!posChanged) {
//                posToStart();
//                break;
//            }
//            result = true;
//        }
//        return result;
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

        // Ready fully
        ByteBuffer bb = ByteBuffer.wrap(buf);
        while(read(bb) != -1);

        int totalRead = bb.position();
        int result = compareArrays(buf, prefix); // , totalRead);

        // Reset position
        setPos(pos);

        return result;
    }


    // Throw an exception if lengths differ?
    public static int compareArrays(byte[] a, byte[] b) { //, int maxLength) {
        int n = Math.min(a.length, b.length);
        //n = Math.min(n, maxLength);

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
        // System.out.println("[" + min + ", " + max + "[");

        long middlePos = (min + max) / 2;
        setPos(middlePos);

        if(isPosAfterEnd()) {
            return Long.MIN_VALUE;
        }

//        posToPrev(delimiter);

        if(middlePos != -1) {
            posToNext(delimiter);
        }

        long delimPos = getPos();
        nextPos(1);

        // If the delimPos has not progressed over min then there is no match
//        if(delimPos < min || min >= max) {
        if(min >= max) {
            return Long.MIN_VALUE;
        }

        // long lineStart = getPos();
        //long lineStart = delimPos + 1;
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
            setPos(delimPos);
            //setPos(delimPos + 1); // This operation cannot move past the end
            //posToNext(delimiter);
            checkNext(1, true);
            nextDelimPos = getPos();
            boolean failedNext = delimPos == nextDelimPos;
            if(failedNext) { // The key is greater than anything in the last block
                result = Long.MIN_VALUE;
            } else {

                result = binarySearch(nextDelimPos, max, delimiter, prefix);
            }
        } else { // if cmp > 0
            if(delimPos >= max) {
                result = Long.MIN_VALUE;
            } else {
                result = binarySearch(min, delimPos - 1, delimiter, prefix);
            }
        }

        return result;
    }


    @Override
    void close() throws IOException;

    /** The currently known size (of the underlying entity) */
    default long size() throws IOException {
        return -1;
    }
}
