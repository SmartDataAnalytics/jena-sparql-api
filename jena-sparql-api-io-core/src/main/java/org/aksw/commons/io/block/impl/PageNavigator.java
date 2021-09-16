package org.aksw.commons.io.block.impl;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.ref.Ref;

import com.google.common.primitives.Ints;

/**
 * An object for (predominantly relative) positioning over a
 * sequence of fixed size pages
 *
 *
 * @author raven
 *
 */
public class PageNavigator
    extends AutoCloseableWithLeakDetectionBase
    implements Seekable
{
    protected PageManager pageManager;

    // Copy of pageManager.getPageSize()
    protected int pageSize;

    /**
     * Current page
     */
    protected long page = 0;

    /**
     * Index is relative to the the page's buffer.position()
     */
    protected int index = 0;

    /*
     * Fields for caching attributes of the
     * buffer at the last valid position
     * Initialization happens in getBufferForPage()
     */

    protected Ref<? extends Page> pageObj = null;

    protected ByteBuffer pageBuffer = null;
    protected int displacement;
    protected long bufferForPage = -1;
    protected int absMaxIndexInPage;
    protected int absMinIndexInPage;
    protected int relMinIndexInPage;
    protected int relMaxIndexInPage;


    protected long minPos;
    protected long minPage;
    protected int minIndex;

    protected long maxPos;
    protected long maxPage;
    protected int maxIndex;

    public PageNavigator(PageManager pageManager) {
        this(pageManager, 0, pageManager.getEndPos());
    }


    /**
     * Clone the state of the navigator, allowing independent positioning
     *
     */
    public synchronized PageNavigator clone() {
        PageNavigator result = new PageNavigator(pageManager, minPos, maxPos);

        result.page = this.page;
        result.index = this.index;

        result.pageObj = this.pageObj == null ? null : this.pageObj.acquire("clone");
        result.pageBuffer = result.pageObj == null ? null : result.pageObj.get().newBuffer();

        result.displacement = this.displacement;
        result.bufferForPage = this.bufferForPage;
        result.absMaxIndexInPage = this.absMaxIndexInPage;
        result.absMinIndexInPage = this.absMaxIndexInPage;
        result.relMinIndexInPage = this.relMinIndexInPage;
        result.relMaxIndexInPage = this.relMaxIndexInPage;

        result.minPos = this.minPos;
        result.minPage = this.minPage;
        result.minIndex = this.minIndex;

        result.maxPos = this.maxPos;
        result.maxPage = this.maxPage;
        result.maxIndex = this.maxIndex;

        return result;
    }

    /**
     *
     *
     * @param pageManager
     * @param minPos inclusive
     * @param maxPos exclusive
     */
    public PageNavigator(PageManager pageManager, long minPos, long maxPos) {
        super();

        if(minPos > maxPos) {
            throw new IndexOutOfBoundsException("min pos must not exceed max " + minPos + " " + maxPos);
        }

        long endPos = pageManager.getEndPos();
        minPos = Math.min(minPos, endPos);
        maxPos = Math.min(maxPos, endPos);

        this.pageManager = pageManager;
        this.pageSize = pageManager.getPageSize();
        this.minPos = minPos;
        this.maxPos = maxPos;

        this.minPage = getPageForPos(minPos);
        this.minIndex = getIndexForPos(minPos);

        this.maxPage = getPageForPos(maxPos);
        this.maxIndex = getIndexForPos(maxPos);

//		if(pageSize == 0) {
//			throw new RuntimeException("Page size must never be 0");
//		}

        updateRelCache(page);
    }

    /**
     * Limit the
     * @param length
     * @return
     */
    public PageNavigator limitNext(long length) {
        long pos = getPos();
        long targetPos = pos + length;
        this.maxPos = Math.min(this.maxPos, targetPos);
        this.maxPage = getPageForPos(maxPos);
        this.maxIndex = getIndexForPos(maxPos);
        updateRelCache(page);

        if(pos > targetPos) {
            posToEnd();
        }

        return this;
    }

    /**
     * Limit the maxmimum position by length bytes from the current position.
     *
     * @param length
     * @return
     */
    public PageNavigator limitPrev(long length) {
        long pos = getPos();
        long targetPos = pos - length;
        this.minPos = Math.max(this.minPos, targetPos);
        this.minPage = getPageForPos(minPos);
        this.minIndex = getIndexForPos(minPos);
        updateRelCache(page);

        if(pos < targetPos) {
            posToStart();
        }

        return this;
    }

    public long getPos() {
        long result = page * pageSize + index;
        return result;
    }

    @Override
    public boolean isPosAfterEnd() {
        boolean result = page > maxPage || (page == maxPage && index >= maxIndex);
        return result;
    }

    @Override
    public boolean isPosBeforeStart() {
        boolean result = page < minPage || (page == minPage && index < minIndex);
        return result;
    }

    /**
     * If a page with the given offset exists, the following fields are updated:
     * - displacement
     * - absMinIndexInPage
     * - absMaxIndexInPage
     *
     * Note, that the relative caches are not updated
     *
     *
     * @param page
     * @return
     * @throws IOException
     */
    public ByteBuffer getBufferForPage(long page) throws IOException {
        if(page == bufferForPage) {
            return pageBuffer;
        } else {
//			System.out.println("Loading page " + page);
//			if(page == -1) {
//				System.out.println("wtf");
//			}
            if(page < minPage || page > maxPage) {
                return null;
            }

            // If there is a prior page, release it
            if(pageObj != null) {
                try {
                    pageObj.close();
                } catch(Exception e) {
                    throw new IOException(e);
                }
            }

            pageObj = pageManager.requestBufferForPage(page);
            ByteBuffer buf = pageObj.get().newBuffer();
            if(buf != null) {
                pageBuffer = buf;
                bufferForPage = page;

                displacement = buf.position();
//				absMinIndexInPage = page == minPage ? displacement + minIndex : displacement;
//				absMaxIndexInPage = page == maxPage ? displacement + maxIndex : buf.limit();
            } else {
                // Special case, where the position after the last byte
                // is on the next page
                // This leads to a dummy zero-size page
                displacement = 0;
            }

            updateRelCache(page);

            absMinIndexInPage = displacement + relMinIndexInPage;
            absMaxIndexInPage = displacement + relMaxIndexInPage;

            return buf;
        }
    }

    /**
     * Update relative min and max index for the given page,
     * taking one byte before and after the selected range into account
     *
     * @param page
     */
    public void updateRelCache(long page) {
        relMinIndexInPage = getRelMinIndex(page);
        relMaxIndexInPage = getRelMaxIndex(page);
    }

    public int getRelMaxIndex(long page) {
        int result = page < maxPage
            ? pageSize
            : page == maxPage
                ? maxIndex
                : 0;

        return result;
    }

    public int getRelMinIndex(long page) {
        int result = page > minPage
            ? 0
            : page == minPage
                ? minIndex
                : pageSize - 1; // page < minPage

        return result;
    }

    public ByteBuffer getBufferForPos(long pos) throws IOException {
        long page = getPageForPos(pos);
        ByteBuffer result = getBufferForPage(page);
        return result;
    }

    public long getPageForPos(long pos) {
        long result = pageSize == 0 ? 0 : pos / pageSize;
        return result;
    }

    public int getIndexForPos(long pos) {
        int result = pageSize == 0 ? 0 : (int)(pos % pageSize);
        return result;
    }

    public void posToStart() {
        setPos(minPage, minIndex - 1);

//		page = minPage;
//		index = minIndex;
    }

    public void posToEnd() {
        setPos(maxPage, maxIndex);
    }

    public void setPos(long page, int index) {
        this.page = page;
        this.index = index;
        updateRelCache(page);
    }

    public void setPos(long pos) {
        page = getPageForPos(pos);
        index = getIndexForPos(pos);
        setPos(page, index);
        // pageBuffer = getBufferForPage(page);
    }

    public long getMinPos() {
        return minPos;
    }

    public long getMaxPos() {
        return maxPos;
    }

    /**
     * Get the byte at the current position
     * Does not change the position
     *
     * Throws an exception if beyond end of data
     *
     * @return
     * @throws IOException
     */
    @Override
    public byte get() throws IOException {
        ByteBuffer buf = getBufferForPage(page);
        byte result = buf.get(displacement + index);
//		byte result = pageBuffer.get(displacement + index);
        return result;
    }

//	public byte get(int delta) throws IOException {
//
//
//		ByteBuffer buf = getBufferForPage(page);
//		byte result = buf.get(index);
//		return result;
//	}


//	public boolean hasMoreData() throws IOException {
//		ByteBuffer buf = getBufferForPage(page);
//		if(buf != null) {
//			int o = buf.position();
//			int r = buf.limit(); //buf.remaining();
//			if(o + index + 1 < r) {
//				return true;
//			} else { // index >= r
//				buf = getBufferForPage(page + 1);
//				if(buf != null) {
//					int r2 = buf.remaining();
//					return r2 > 0;
//				} else {
//					return false;
//				}
//			}
//		} else {
//			return false;
//		}
//	}

    public boolean canNextPos() {
        if(index + 1 < relMaxIndexInPage) {
            return true;
        } else {
            int nextRelMaxIndex = getRelMaxIndex(page + 1);
            boolean result = nextRelMaxIndex > 0;
            return result;
        }
    }

    public boolean canPrevPos() {
        if(index - 1 >= relMinIndexInPage) {
            return true;
        } else {
            int nextRelMinIndex = getRelMinIndex(page - 1);
            boolean result = nextRelMinIndex < pageSize - 1;
            return result;
        }
    }

//	public boolean nextPos() throws IOException {
//		return nextPos(1);
//	}


    @Override
    public int checkNext(int len, boolean changePos) throws IOException {
        long pos = getPos();
        int r = Math.min(Ints.saturatedCast(maxPos - pos), len);

        if(changePos) {
            nextPos(r);
        }

        return r;
    }

    @Override
    public int checkPrev(int len, boolean changePos) throws IOException {
        long pos = getPos();
        int r = Math.min(Ints.saturatedCast(pos - minPos), len);

        if(changePos) {
            prevPos(r);
        }

        return r;
    }

    /**
     * Attempts to advance the position by delta bytes
     * and returns true if this succeeded.
     * Position is unchanged if insufficient bytes are available
     *
     * Cannot advance beyond the last byte.
     *
     * @return
     * @throws IOException
     */
    @Override
    public boolean nextPos(int delta) throws IOException {
        int nextIndex = index + delta;

        if(nextIndex < relMaxIndexInPage) {
            index = nextIndex;
            return true;
        } else {
            boolean simpleDelta = delta < pageSize; // avoid division
            long tgtPage;
            int tgtIndex;
            if(simpleDelta) {
                tgtPage = page + 1;
                tgtIndex = nextIndex - relMaxIndexInPage;
            } else {
                long p = getPos() + delta;
                tgtPage = getPageForPos(p);
                tgtIndex = getIndexForPos(p);
            }

            ByteBuffer buf = getBufferForPage(tgtPage);
            if(buf != null) {
                // FIXME I think we are missing a check whether for the last page tgtIndex is in the valid range
                // nextPos(x) where x exceeds endPos would then incorrectly return true
                page = tgtPage;
                index = tgtIndex;
                setPos(page, index);
                return true;
            } else {
                return false;
            }
        }
    }

//	@Override
//	public boolean prevPos() throws IOException {
//		return prevPos(1);
//	}

    public boolean prevPos(int delta) throws IOException {
        //getBufferForPage(page);
        // getBufferForPage(page);

        int prevIndex = index - delta;
//		boolean isPrevIndexInRange = page > minPage
//				? prevIndex > 0
//				: prevIndex > minIndex;

        if(prevIndex >= relMinIndexInPage) {
            index = prevIndex;
            return true;
        } else {
            boolean simpleDelta = delta < pageSize; // avoid division
            long tgtPage;
            int tgtIndex;
            if(simpleDelta) {
                tgtPage = page - 1;
                tgtIndex = pageSize - (relMinIndexInPage - prevIndex);
            } else {
                long p = getPos() - delta;
                tgtPage = getPageForPos(p);
                tgtIndex = getIndexForPos(p);
            }
            ByteBuffer buf = getBufferForPage(tgtPage);
            if(buf != null) {
                page = tgtPage;
                index = tgtIndex;
                setPos(page, index);

//				--page;
//				index = pageSize - 1;
                return true;
            } else {
                return false;
            }
        }
    }

//	public boolean prevPos() throws IOException {
//		if(page >= 0) {
//			if(index > 0) {
//				--index;
//			} else {
//				--page;
////				ByteBuffer buf = getBufferForPage(page);
////				index = buf.remaining() - 1;
//				index = pageSize - 1;
//			}
//			return true;
//		} else {
//			return false;
//		}
//	}


    /**
     * Advances the position to the next matching delimiter or
     * one byte past the end of the stream.
     *
     *
     * Returns true if and only if the position was changed.
     * Note, true does NOT mean that the byte at the new position matches delim.
     * A match is implied if isPosAfterEnd() is false.
     *
     * @param delimiter
     * @return
     * @throws IOException
     */
    @Override
    public boolean posToNext(byte delimiter) throws IOException {
        long p = page;
        int i = index;
        ByteBuffer buffer;
        outer: for(; (buffer = getBufferForPage(p)) != null; ++p) {
            // int absMaxIndexInPage = page == maxPage ? maxIndex : buffer.limit();
            //int displacement = buffer.position();
            int absGetIndexInPage = displacement + i;

            for(; absGetIndexInPage < absMaxIndexInPage; ++absGetIndexInPage) {
                byte a = buffer.get(absGetIndexInPage);
                if(a == delimiter) {
                    i = absGetIndexInPage - displacement;
                    break outer;
                }
            }

            if(p == maxPage) {
                i = absGetIndexInPage - displacement;
                // if(absGetIndexInPage == absMaxIndexInPage) {
                if(i == pageSize) {
                    i = 0;
                    ++p;
                }
                break;
            }
            i = 0;
        }

        if(i != index || p != page) {
            setPos(p, i);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean posToPrev(byte delimiter) throws IOException {
        long p = page;
        int i = index;
        ByteBuffer buffer;
        outer: for(; (buffer = getBufferForPage(p)) != null; --p) {
//			int relMinIndexInPage = page == minPage ? minIndex : 0;
            //int displacement = buffer.position();
            int absGetIndexInPage = displacement + i;
//            int absMinIndexInPage = displacement + relMinIndexInPage;

            //for(; i >= r; --i, --x) {
            for(; absGetIndexInPage >= absMinIndexInPage; --absGetIndexInPage) {
                byte c = buffer.get(absGetIndexInPage);
                if(c == delimiter) {
                    i = absGetIndexInPage - displacement;
                    break outer;
                }
            }

            if(p == minPage) {
                i = absGetIndexInPage - displacement;
                if(i == -1) {
                    i = pageSize - 1;
                    --p;
                }
                break;
            }
            i = pageSize - 1;
        }

        if(i != index || p != page) {
            setPos(p, i);
            return true;
        } else {
            return false;
        }
    }
    /**
     * Returns the position of the next delimiter or the end of the stream
     *
     * @param delimiter
     * @return
     * @throws IOException
     */
    public long getNextPosFor(byte delimiter) throws IOException {
        long p;
        int i = index;
        ByteBuffer buffer;
        outer: for(p = page; (buffer = getBufferForPage(p)) != null; ++p) {
//			byte[] arr = buffer.array();
            int o = buffer.position();
            int r = buffer.remaining();
            for(i = index; i < r; ++i) {
                byte a = buffer.get(o + i);
                if(a == delimiter) {
                    break outer;
                }
            }
            index = 0;
        }

        long result = (pageBuffer == null ? p - 1 : p) * pageSize + (long)i;
        return result;
    }

    public long getPrevPosFor(byte delimiter) throws IOException {
        long p;
        int i = index;
        outer: for(p = page; p >= 0; --p) {
            ByteBuffer buffer = getBufferForPage(p);
            int o = buffer.position();
            for(i = index; i >= 0; --i) {
                byte c = buffer.get(o + i);
                if(c == delimiter) {
                    break outer;
                }
            }

            index = pageSize - 1;
        }

        p = Math.max(0, p);
        //i = Math.max(0, i);

        long result = p * pageSize + (long)i;
        return result;
    }

    @Override
    public int compareToPrefix(byte[] prefix) throws IOException {
        int x = 0;
        int n = prefix.length;

        int result = 0;
        ByteBuffer buffer;
        long p = page;
        int i = index;

        outer: for(; (buffer = getBufferForPage(p)) != null; ++p) {
            int absGetIndexInPage = displacement + i;

            for(; absGetIndexInPage < absMaxIndexInPage && x < n; ++absGetIndexInPage, ++x) {
                byte a = buffer.get(absGetIndexInPage);
                byte b = prefix[x];

                result = Byte.compare(a, b);
                if(result != 0) {
                    break outer;
                }
            }

            if(x == n) {
                break;
            }

            if(p == maxPage) {
                result = -1;
                break;
            }
            i = 0;
        }

        // Reset the page
        getBufferForPage(p);

        return result;
    }

    /**
     * Reads bytes at the current position; does not advance pos
     *
     * @param n
     * @return
     * @throws IOException
     */
    @Override
    public int peekNextBytes(byte[] dst, int offset, int len) throws IOException {
        //long end = findFollowingDelimiter(pos, delimiter);

        // TODO use this guava safe int feature
        //int n = (int)(end - pos);
        //byte[] dst = new byte[n];
        int x = 0;
        ByteBuffer buffer;
        int delta = index;
        for(long p = page; x < len && (buffer = getBufferForPage(p)) != null; ++p) {
            //int displacement = buffer.position();
            int absMaxIndexInPage = buffer.limit(); //buffer.remaining();
            //int absGetIndexInPage = displacement + delta;
            for(int i = displacement + delta; i < absMaxIndexInPage && x < len; ++i, ++x) {
                byte b = buffer.get(i);
                dst[offset + x] = b;
            }
            delta = 0;
        }

        return x;
    }


    // Convenience method
    // TODO Move to Seekables
    public String readLine() throws IOException {
        long start = getPos();
        posToNext((byte)'\n');
        long end = getPos();

        setPos(start);
        int len = (int)(end - start);
        byte[] arr = new byte[len];
        peekNextBytes(arr, 0, len);
        String result = new String(arr);
        return result;
    }


    // Convenience method
    public String readString(int n) throws IOException {
        //long end = findFollowingDelimiter(pos, delimiter);

        // TODO use this guava safe int feature
        //int n = (int)(end - pos);
        byte[] dst = new byte[n];

        int x = 0;
        ByteBuffer buffer;
        for(long p = page; x < n && (buffer = getBufferForPage(p)) != null; ++p) {
            int r = buffer.remaining();
            for(int i = index; i < r && x < n; ++i, ++x) {
                byte b = buffer.get(i);
                dst[x] = b;
            }
            index = 0;
        }

        String result = new String(dst);
        return result;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ByteBuffer buffer;
        int n = 0;
        if(isPosAfterEnd()) {
            n = -1;
        } else {
            while(dst.remaining() > 0 && (buffer = getBufferForPage(page)) != null) {
                ByteBuffer src = buffer.duplicate();
                ((Buffer)src).position(displacement + index);
                ((Buffer)src).limit(displacement + relMaxIndexInPage);
                int readContrib = readRemaining(dst, src);
                n += readContrib;

                if(!nextPos(readContrib)) {
                    posToEnd();
                    break;
                } else if(readContrib == 0) {
                    break;
                }
            }
        }

        return n;
    }

    // Copy as much as possible into dst
    public static int readRemaining(ByteBuffer dst, ByteBuffer src) {
        int n = Math.min(src.remaining(), dst.remaining());
        ((Buffer)src).limit(src.position() + n);
        dst.put(src);

        return n;
    }

    @Override
    public boolean isOpen() {
        return !isClosed;
        //return isOpen;
    }

    @Override
    public void closeActual() throws Exception {
        if(pageObj != null) {
            try {
                if(!pageObj.isClosed()) {
                    pageObj.close();
                }
            } catch(Exception e) {
                throw new IOException(e);
            }
            pageObj = null;
        }

        //isOpen = false;
    }



//    public long binarySearch(long min, long max, byte delimiter, byte[] prefix) throws IOException {
//
//        long middlePos = (min + max) / 2;
//        setPos(middlePos);
//
//        if(isPosAfterEnd()) {
//            return Long.MIN_VALUE;
//        }
//
//        posToPrev(delimiter);
//        long delimPos = getPos();
//
//        // If the delimPos has not progressed over min then there is no match
//        if(delimPos < min || min >= max) {
//            return Long.MIN_VALUE;
//        }
//
//        // long lineStart = getPos();
//        //long lineStart = delimPos + 1;
//        nextPos(1);
//        int cmp = compareToPrefix(prefix);
//
//        // System.out.println(min + " - " + max);
////        String l = readLine();
////        System.out.println("Comparison of line at range " + min + " - " + max);
////        System.out.println("    " + l);
////        System.out.println("  and");
////        System.out.println("    " + new String(prefix));
////        System.out.println("  resulted in " + cmp);
//
//
//        // if we have a byte comparison such as
//        // [3].compareToPrefix([5]) which yields -1, then we need to search in
//        // the higher segment
//        long result;
//        if(cmp == 0) {
//            result = delimPos;
//        } else if(cmp < 0) {
//            long nextDelimPos;
//            setPos(delimPos + 1);
//            posToNext(delimiter);
//            nextDelimPos = getPos();
//
//            result = binarySearch(nextDelimPos, max, delimiter, prefix);
//        } else { // if cmp > 0
//            result = binarySearch(min, delimPos - 1, delimiter, prefix);
//        }
//
//        return result;
//    }
}
