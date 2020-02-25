package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public class PageNavigator {
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
	protected ByteBuffer pageBuffer = null;
	protected long bufferForPage = -1;
	protected int absMaxIndexInPage;
	protected int absMinIndexInPage;
	
	
	protected long minPos;
	protected long minPage;
	protected int minIndex;
	
	protected long maxPos;
	protected long maxPage;
	protected int maxIndex;
	
	public PageNavigator(PageManager pageManager) {
		this(pageManager, 0, pageManager.getEndPos());
	}

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
			if(page < minPage || page > maxPage) {
				return null;
			}

			ByteBuffer buf = pageManager.requestBufferForPage(page);
			if(buf != null) {
				pageBuffer = buf;
				bufferForPage = page;
			
				int displacement = buf.position();
				absMaxIndexInPage = page == maxPage ? displacement + maxIndex : buf.limit();
				absMinIndexInPage = page == minPage ? displacement + minIndex : displacement;
			}
			
			return buf;
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
	
	public void posToStart() {
		page = minPage;
		index = minIndex;
//		page = minPage;
//		index = minIndex;
	}
	
	public void posToEnd() {
		page = maxPage;
		index = maxIndex;
	}
	
	public void setPos(long pos) {
		page = getPageForPos(pos);
		index = getIndexForPos(pos);
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
			int o = buf.position();
			int r = buf.limit(); //buf.remaining();
			if(o + index + 1 < r) {
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
	 * Cannot advance beyond the last byte.
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean nextPos() throws IOException {
		ByteBuffer buf = getBufferForPage(page);
		if(buf != null) {
			// int r = page == maxPage ? maxIndex : pageSize; //buf.remaining();
			if(index + 1 < absMaxIndexInPage) {
				++index;
				return true;
			} else {
				if(page >= maxPage) {
					return false;
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
		}
		return false;
	}
	
	public boolean prevPos() throws IOException {
		ByteBuffer buf = getBufferForPage(page);
		if(buf != null) {
			int r = absMinIndexInPage; // page == minPage ? minIndex : 0; //buf.position();
			if(index - 1 >= r) {
				--index;
				return true;
			} else {
				if(page <= minPage) {
					return false;
				} else {
					buf = getBufferForPage(page - 1);
					if(buf != null) {
						--page;
						index = pageSize - 1;
						return true;
					} else {
						return false;
					}
				}
//				} else {
//					return false;
//				}
			}
		}
		return false;
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
			// int absMaxIndexInPage = page == maxPage ? maxIndex : buffer.limit();
			int displacement = buffer.position();
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
		ByteBuffer buffer;
        outer: for(; (buffer = getBufferForPage(p)) != null; --p) {
//			int relMinIndexInPage = page == minPage ? minIndex : 0;
            int displacement = buffer.position();
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
	public int readBytes(byte[] dst, int offset, int len) throws IOException {
		//long end = findFollowingDelimiter(pos, delimiter);

		// TODO use this guava safe int feature
		//int n = (int)(end - pos);
		//byte[] dst = new byte[n];
		int x = 0;
		ByteBuffer buffer;
		int delta = index;
		for(long p = page; x < len && (buffer = getBufferForPage(p)) != null; ++p) {
			int displacement = buffer.position();
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
}
