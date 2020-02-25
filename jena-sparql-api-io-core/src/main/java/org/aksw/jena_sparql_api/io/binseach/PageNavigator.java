package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PageNavigator {
	protected PageManager pageManager;

	// Copy of pageManager.getPageSize()
	protected int pageSize;

	protected long page = 0;
	protected int index = 0;
	
	protected ByteBuffer pageBuffer = null;
	protected long bufferForPage = -1;
	
	public PageNavigator(PageManager pageManager) {
		super();
		this.pageManager = pageManager;
		this.pageSize = pageManager.getPageSize();
		if(pageSize == 0) {
			throw new RuntimeException("Page size must never be 0");
		}
	}
	
	public long getPos() {
		long result = page * pageSize + index;
		return result;
	}
	
	public ByteBuffer getBufferForPage(long page) throws IOException {
		if(page == bufferForPage) {
			return pageBuffer;
		} else {
			pageBuffer = pageManager.requestBufferForPage(page);
			bufferForPage = page;
			return pageBuffer;
		}
	}

	public ByteBuffer getBufferForPos(long pos) throws IOException {
        long page = getPageForPos(pos);
        ByteBuffer result = getBufferForPage(page);
		return result;
	}

	public long getPageForPos(long pos) {
        long result = pos / pageSize;
        return result;
	}

	public int getIndexForPos(long pos) {
        int result = (int)(pos % pageSize);
        return result;
	}
	
	public void posToEnd() {
		long endPos = pageManager.getEndPos();
		setPos(endPos);
	}
	
	public void setPos(long pos) {
		page = getPageForPos(pos);
		index = getIndexForPos(pos);
		// pageBuffer = getBufferForPage(page);
	}
	
	/**
	 * Get the byte at the current position
	 * Throws an exception if beyond end of data
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte get() throws IOException {
		ByteBuffer buf = getBufferForPage(page);
		byte result = buf.get(index);
		return result;
	}

//	public byte get(int delta) throws IOException {
//		
//		
//		ByteBuffer buf = getBufferForPage(page);
//		byte result = buf.get(index);
//		return result;
//	}

	
	public boolean hasMoreData() throws IOException {
		ByteBuffer buf = getBufferForPage(page);
		if(buf != null) {
			int r = buf.remaining();
			if(index + 1 < r) {
				return true;
			} else { // index >= r
				buf = getBufferForPage(page + 1);
				if(buf != null) {
					int r2 = buf.remaining();
					return r2 > 0;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Attempts to advance the position by 1 byte
	 * and returns true if this succeeded.
	 * Can advance one byte beyond the end of data
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean nextPos() throws IOException {
		ByteBuffer buf = getBufferForPage(page);
		if(buf != null) {
			int r = buf.remaining();
			if(index + 1 < r) {
				++index;
				return true;
			} else {
				buf = getBufferForPage(page + 1);
				if(buf != null) {
					++page;
					index = 0;
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	
	public boolean prevPos() throws IOException {
		if(page >= 0) {
			if(index > 0) {
				--index;
			} else {
				--page;
//				ByteBuffer buf = getBufferForPage(page);
//				index = buf.remaining() - 1;
				index = pageSize - 1;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Advances the position to the next delimiter or the end of the stream.
	 * 
	 * Returns true if and only if the position was changed.
	 * Note, true does NOT mean that the byte at the new position matches delim
	 * 
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
	public boolean posToNext(byte delimiter) throws IOException {
		long p = page;
		int i = index;
		ByteBuffer buffer;
		outer: for(; (buffer = getBufferForPage(p)) != null; ++p) {
			int r = buffer.remaining();
			for(; i < r; ++i) {
				byte a = buffer.get(i);
				if(a == delimiter) {
					break outer;
				}
			}
			i = 0;
		}
		
		if(i != index || p != page) {
			page = p;
			index = i;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean posToPrev(byte delimiter) throws IOException {
		long p = page;
		int i = index;
        outer: for(; p >= 0; --p) {
            ByteBuffer buffer = getBufferForPage(p);

	        for(; i >= 0; --i) {
	            byte c = buffer.get(i);
	            if(c == delimiter) {
	            	break outer;
	            }
	        }
	        
	        i = pageSize - 1;
        }
		
		if(i != index || p != page) {
			page = p;
			index = i;
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
			int r = buffer.remaining();
			for(i = index; i < r; ++i) {
				byte a = buffer.get(i);
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

	        for(i = index; i >= 0; --i) {
	            byte c = buffer.get(i);
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
	
	public int compareToPrefix(byte[] prefix) throws IOException {
//		long page = getPageForPos(pos);
//		int index = getIndexForPos(pos);

		int x = 0;
		int n = prefix.length;
		
		int result = 0;
		ByteBuffer buffer;
		outer: for(long p = page; x < n && (buffer = getBufferForPage(p)) != null; ++p) {
			int r = buffer.remaining();
			for(int i = index; i < r && x < n; ++i, ++x) {
				byte a = buffer.get(i);
				byte b = prefix[x];
				
				result = Byte.compare(a, b);
				if(result != 0) {
					break outer;
				}
				
			}
			index = 0;
		}

		return result;
	}
	
	/**
	 * 
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes(int n) throws IOException {
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
		
		return dst;
	}
}
