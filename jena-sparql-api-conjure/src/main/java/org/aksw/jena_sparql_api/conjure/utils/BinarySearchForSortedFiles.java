package org.aksw.jena_sparql_api.conjure.utils;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

/**
 * Binary search over sorted files with memory mapped IO
 * 
 * @author raven
 *
 */
public class BinarySearchForSortedFiles
	implements AutoCloseable
{
	int pageSize = 4096;
	byte delimiter = (byte)'\n';
	
	// Note sure whether we need a cache or whether the OS does it
	// In any case, the cache should be cleared when done:
	// https://stackoverflow.com/questions/25238110/how-to-properly-close-mappedbytebuffer?lq=1
	protected Cache<Long, MappedByteBuffer> pageCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build();
	
	protected FileChannel channel;
	
	
	public BinarySearchForSortedFiles(FileChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public void close() throws Exception {
		pageCache.invalidateAll();
	}
	
	public Stream<String> search(String prefix) {
		Stream<String> result;
		try {
			result = searchCore(prefix);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public Stream<String> searchCore(String prefix) throws IOException, ExecutionException {
		long size = channel.size();

		byte[] prefixBytes = prefix.getBytes();
		
		// -1 is the position of the delimiter before the file start
		// jump to the beginning of the file if the prefix is empty
		long matchDelimPos = prefixBytes.length == 0
			? -1
			: binarySearch(-1, size, prefixBytes);
		
		Stream<String> result;
		
		if(matchDelimPos != Long.MIN_VALUE) {
			long tmp[] = { getPosOfFirstMatch(matchDelimPos, prefixBytes) };

			Supplier<String> nextLineSupp = () -> {
				try {
					long currentDelimPos = tmp[0];
					
					boolean exceededEnd = currentDelimPos >= size;
					boolean isValidLine = exceededEnd
							? false
							// Lines not at the end must start with the prefix
							: currentDelimPos <= matchDelimPos // Do not recheck lines before the binary search location
								? true
								: compareToPrefix(currentDelimPos + 1, prefixBytes) == 0;
	
					String r;
					if(isValidLine) {
						long endPos = findFollowingDelimiter(currentDelimPos + 1, delimiter);
						int len = Ints.checkedCast(endPos - currentDelimPos);

						// Cut off the delimiter if we are not at the end
						if(endPos != size) {
							--len; 
						}
//						System.err.println("Endpos: " + endPos + " size: " + size + " len: " + len + " sum: " + (currentDelimPos + 1 + len));
						r = readString(currentDelimPos + 1, len);
						tmp[0] = endPos;
					} else {
						r = null;
					}
					
					return r;
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			};

			Iterator<String> it = new AbstractIterator<String>() {
				@Override
				protected String computeNext() {
					String tmp = nextLineSupp.get();
					String r = tmp == null ? endOfData() : tmp;
					return r;
				}
			};
			
			result = Streams.stream(it);
		}  else {
			result = Stream.empty();
		}
		
		return result;
	}
		
		//System.out.println(matchPos);
		
		//if(matchPos >= size)

		
		//String line = readLine(matchPos + 1, delimiter);
//System.out.println(line);
//		long nlp = findPrecedingDelimiter(channel, pos, delimiter);
//		// nlp = new line position
//		System.out.println("Found newline at: " + nlp);
//		
//		String line = readLine(nlp + 1, delimiter);
//		
//		System.out.println(line);
//		
//		
//		System.out.println(comparePrefix(nlp + 1, "<http://lsq.aksw.org/res/q-4a68281e>".getBytes()));

	public long binarySearch(long min, long max, byte[] prefix) throws IOException, ExecutionException {
		
		long pos = (min + max) / 2;
		long delimPos = findPrecedingDelimiter(pos, delimiter);
		
		// If the delimPos has no progressed over min then there is no match
		if(delimPos < min || min >= max) {
			return Long.MIN_VALUE;
		}

		long lineStart = delimPos + 1;
		int cmp = compareToPrefix(lineStart, prefix);

//		System.out.println(min + " - " + max);
//		String l = readLine(lineStart, delimiter);
//		System.out.println("Compared");
//		System.out.println("  " + l);
//		System.out.println("  " + new String(prefix));
//		System.out.println("  with result " + cmp);
		

		// if we have a byte comparison such as
		// [3].compareToPrefix([5]) which yields -1, then we need to search in 
		// the higher segment
		long result;
		if(cmp == 0) {
			result = delimPos;
		} else if(cmp < 0) {
			long nextDelimPos = findFollowingDelimiter(delimPos + 1, delimiter);
			
			result = binarySearch(nextDelimPos, max, prefix);
		} else { // if cmp > 0
			result = binarySearch(min, delimPos - 1, prefix);
		}
		
		return result;
	}
	
	// Pos should point to the delimiter
	// the result will point to pos or a preceding delimiter
	public long getPosOfFirstMatch(long pos, byte[] prefix) throws IOException, ExecutionException {
		long result = pos;
		
		while(result != -1) {
			long tmp = findPrecedingDelimiter(result - 1, delimiter);
			int cmp = compareToPrefix(tmp + 1, prefix);
			if(cmp != 0) {
				break;
			} else {
				result = tmp;
			}
		}
		
		return result;
	}
	
	public MappedByteBuffer getBufferForPage(long page) throws IOException, ExecutionException {
		long eof = channel.size();

        long start = page * pageSize;
        long end = Math.min(eof, start + pageSize);
        long length = end - start;
        
        MappedByteBuffer result = length <= 0
        		? null
        		: pageCache.get(page, () -> channel.map(MapMode.READ_ONLY, start, length));
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
	
	public MappedByteBuffer getBufferForPos(long pos) throws IOException, ExecutionException {
        long page = getPageForPos(pos);
        MappedByteBuffer result = getBufferForPage(page);
		return result;
	}
	
	// Returns whether the bytes at pos are lower or higher than prefix
	// with the respective results -1 and +1
	public int compareToPrefix(long pos, byte[] prefix) throws IOException, ExecutionException {
		long page = getPageForPos(pos);
		int index = getIndexForPos(pos);

		int x = 0;
		int n = prefix.length;
		
		int result = 0;
		MappedByteBuffer buffer;
		outer: for(long p = page; (buffer = getBufferForPage(p)) != null && x < n; ++p) {
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

	public String readString(long pos, int n) throws IOException, ExecutionException {
		//long end = findFollowingDelimiter(pos, delimiter);

		// TODO use this guava safe int feature
		//int n = (int)(end - pos);
		byte[] dst = new byte[n];
		
		long page = getPageForPos(pos);
		int index = getIndexForPos(pos);
		
		int x = 0;
		MappedByteBuffer buffer;
		for(long p = page; (buffer = getBufferForPage(p)) != null && x < n; ++p) {
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

	public String readLine(long pos, byte delimiter) throws IOException, ExecutionException {
		long page = getPageForPos(pos);
		int index = getIndexForPos(pos);

		//StringBuilder sb = new StringBuilder();
		
		// I'd assume a solution with low level byte array and System.arrayCopy to be
		// alot more efficient - so this should be replaced
		List<Byte> list = new ArrayList<Byte>();
		
		MappedByteBuffer buffer;
		outer: for(long p = page; (buffer = getBufferForPage(p)) != null; ++p) {
			int r = buffer.remaining();
			for(int i = index; i < r; ++i) {
				byte a = buffer.get(i);
				if(a == delimiter) {
					break outer;
				}
				
				list.add(a);
				//sb.append(a);
			}
			index = 0;
		}

		byte[] arr = Bytes.toArray(list);
		String result = new String(arr);
		
		return result;
	}


	
	public long findFollowingDelimiter(long pos, byte delimiter) throws IOException, ExecutionException {
		long page = getPageForPos(pos);
		int index = getIndexForPos(pos);
		
		MappedByteBuffer buffer;
		long p;
		int i = index;
		outer: for(p = page; (buffer = getBufferForPage(p)) != null; ++p) {
			int r = buffer.remaining();
			for(i = index; i < r; ++i) {
				byte a = buffer.get(i);
				if(a == delimiter) {
					break outer;
				}
			}
			index = 0;
		}

		long result = (buffer == null ? p - 1 : p) * pageSize + (long)i;
		return result;
	}
	// returns pos if byteOf[pos] == delimiter
	// returns -1 if there is none
	public long findPrecedingDelimiter(long pos, byte delimiter) throws IOException, ExecutionException {

        long page = getPageForPos(pos);
		int index = getIndexForPos(pos);
        
		long p;
        int i = index;
        outer: for(p = page; p >=0; --p) {
            MappedByteBuffer buffer = getBufferForPage(p);

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

}