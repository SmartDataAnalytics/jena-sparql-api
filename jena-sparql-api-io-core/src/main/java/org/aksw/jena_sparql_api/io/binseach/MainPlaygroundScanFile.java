package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.collect.Maps;


public class MainPlaygroundScanFile {
	/**
	 * Count the number of occurrences of '\n' together with a flag
	 * whether it ocurred at the end
	 * 
	 * @param pageNavigator
	 * @return
	 * @throws IOException
	 */
	public static Entry<Long, Boolean> wcFwd(PageNavigator pageNavigator) throws IOException {
//		System.out.println("Thread: " + Thread.currentThread());
		byte delim = (byte)'\n';
		
		boolean endsOnDelim = false;
		long count = 0;
		pageNavigator.posToStart();
		
		for(;;) {
			pageNavigator.posToNext(delim);
			boolean posChanged = pageNavigator.nextPos();
			if(!posChanged) {
				// we may be on the last byte or one beyond
				endsOnDelim = !pageNavigator.isPosAfterEnd();				
				if(endsOnDelim) {
					++count;
				}
				break;
			}
			++count;
		}

		return Maps.immutableEntry(count, endsOnDelim);
	}
	
	/**
	 * Return the line count and whether the first position was a newline
	 * 
	 * @param pageNavigator
	 * @return
	 * @throws IOException
	 */
	public static Entry<Long, Boolean> wcBwd(PageNavigator pageNavigator) throws IOException {
//		System.out.println("Thread: " + Thread.currentThread());
		byte delim = (byte)'\n';

		long count = 0;

		pageNavigator.posToEnd();
		pageNavigator.prevPos();
		
		//boolean startsOnDelim = !pageNavigator.isPosBeforeStart() && pageNavigator.get() == delim;
		boolean endsOnDelim = false;
	
		for(;;) {
			// Pos to prev can move one character before the stream if the char is not found
			pageNavigator.posToPrev(delim);
			//byte bAt = pageNavigator.get();
			//System.out.println("Got byte: " + bAt + " at " + pageNavigator.getPos());
			boolean posChanged = pageNavigator.prevPos();
			if(!posChanged) {
				endsOnDelim = !pageNavigator.isPosBeforeStart();
				if(endsOnDelim) {
					++count;
				}
				break;
			}

			++count;
		}
		
		return Maps.immutableEntry(count, endsOnDelim);
	}
	

	public static void main(String[] args) throws IOException {
		
//		if(true) {
//			byte x = -5;
//			System.out.println(x & 0xff);
//			
//			return;
//		}
		
		String str = "aaaabbbbccccdddd";
		
		// Simulate access to the string using multiple pages
		// FIXME Disposition is broken and therefore 0 for now
		Path path = Paths.get("/home/raven/Projects/Eclipse/sparql-integrate-parent/ngs/test2.nq");

		try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
			PageManager pageManager = PageManagerForFileChannel.create(fileChannel);

			byte[] text = "ABAAAABAACDABA".getBytes(); 
//			byte[] text = "BAACDABA".getBytes(); 
//			pageManager = new PageManagerForByteBuffer(ByteBuffer.wrap(text));
//			pageManager = new PageManagerWrapper(pageManager, 0, pageManager.getPageSize() / 4);
//			pageManager = new PageManagerWrapper(pageManager, 0, pageManager.getPageSize());
			
			PageNavigator nav = new PageNavigator(pageManager);
			nav.posToStart();
			//nav.nextPos();
			
//			String pattern = "t%2FP625%3E++%3Fvar4+.%0";
			String pattern = "SELECT";
//			String pattern = "AACD";
//			String pattern = "BAA";
			//String pattern = "cddd".getBytes();
			
			byte[] patternBytes = pattern.getBytes();
			BoyerMooreMatcher matcher = BoyerMooreMatcher.create(patternBytes);
			// nav.setPos(patternBytes.length);
			
			Stopwatch sw = Stopwatch.createStarted();
			int matchCnt = 0;
			while(matcher.searchFwd(nav)) {
				++matchCnt;
//				System.out.println("Got match");
//				String line = nav.readLine();
//				System.out.println("Pos after match: " + nav.getPos() + " " + line);				
			}
			System.out.println("Got " + matchCnt + " matches in " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
			
			if(true) {
				return;
			}
			
			
			for(int i = 0; i < str.length(); ++i) {
				System.out.println(i);
				nav.posToStart();
				nav.nextPos();
				nav.nextPos(i);
				
				System.out.println("got: " + (char)nav.get());
			}
			
	//		for(int i = str.length(); i >= 0; --i) {
			for(int i = 4; i >= 0; --i) {
				System.out.println(i);
				nav.posToEnd();
				nav.prevPos();
				nav.prevPos(i);
				
				System.out.println("got: " + (char)nav.get());
			}
		}
	}
	
	
	public static void mainWc(String[] args) throws IOException {
		Path path = Paths.get("/home/raven/Projects/Eclipse/sparql-integrate-parent/ngs/test2.nq");
//		Path path = Paths.get("/tmp/test.txt");

		for(int i = 0; i < 0; ++i) {
			Stopwatch sw = Stopwatch.createStarted();
			System.out.println("Lines: " + Files.lines(path).count()); //, StandardCharsets.UTF_8));
			System.out.println(sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
		}
		
		String str1 = "\n   "
				   + "   \n"
				   + "  \n "
				   + "   \n"
				   ;

		String str = " \n  "
				   + "   \n"
				   + "  \n "
				   + "   \n"
				   ;
		
		// The first newline in a chunk does not count, unless the preceeding chunk ended on a newline
		// Conversely: If the previous chunk does not end in a newline, subtract one from the chunk's count 
		
//		String str = "  \n "
//				   + "  \n "
//				   + "  \n "
//				   + "  \n "
//				   ;

		try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
			PageManager pageManager = PageManagerForFileChannel.create(fileChannel,  128 * 1024 * 1024);
//			PageManager pageManager = new PageManagerForByteBuffer(ByteBuffer.wrap(str.getBytes()));
			
			long size = pageManager.getEndPos();
			//long size = fileChannel.size();			
			int numChunks = 4; //32;
			boolean fwd = true;
			int numRuns = 10;

			long chunkSize = size / numChunks;
			int remainder = (int)size % numChunks;
			
			Map<Integer, PageNavigator> navs = new HashMap<>();
			long start = 0;
			for(int i = 0; i < numChunks; ++i) {
				long extra = i < remainder ? 1 : 0;
				long end = start + chunkSize + extra;
//				System.out.println("Chunk: " + start + " -> " + end);
				navs.put(i, new PageNavigator(pageManager, start, end));
				start = end;
			}

				
			for(int i = 0; i < numRuns; ++i) {
				Stopwatch sw = Stopwatch.createStarted();

				List<Entry<Integer, PageNavigator>> ready = navs.entrySet().stream()
						.sorted((a, b) -> (a.getKey() - b.getKey()) * (fwd ? 1 : -1))
						.collect(Collectors.toList());
								
				// TODO Counts are not yet exact because of lack of boundary handling
				List<Entry<Integer, Entry<Long, Boolean>>> contribs = ready.parallelStream()
					.map(e -> {
						try {
							Entry<Long, Boolean> f = fwd
									? wcFwd(e.getValue())
									: wcBwd(e.getValue());
							return Maps.immutableEntry(e.getKey(), f);
						} catch (IOException x) {
							throw new RuntimeException(x);
						}
					})
					.sorted((a, b) -> (a.getKey() - b.getKey()) * (fwd ? 1 : -1))
					.collect(Collectors.toList());

// If the last chunk does not end on a newline, increase the count by 1
				
				
				long count = contribs.stream().mapToLong(e -> e.getValue().getKey()).sum();
				
				boolean endsOnDelim = contribs.get(contribs.size() - 1).getValue().getValue();
				if(!endsOnDelim) {
					// TODO Only if the chunk is not empty
					if(fwd) {
						++count;
					}
				}
				
				System.out.println("Contribs: " + contribs + " raw sum: " + contribs.stream().mapToLong(e -> e.getValue().getKey()).sum());
					//.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

//				long count = 0;
//				//Entry<Long, Boolean> before = Maps.immutableEntry(0l, true);
//				boolean prevEndsOnDelim = true;
//				for(int x = 0; x < contribs.size(); ++x) {
//					Entry<Long, Boolean> contrib = contribs.get(x).getValue();
//					long contribCount = contrib.getKey();					
//					boolean endsOnDelim = contrib.getValue();
//					
//					boolean isLastChunk = x + 1 == contribs.size();
//					if(!prevEndsOnDelim) {
//						--contribCount;
//					}
////					if(!prevEndsOnDelim) {
////						--contribCount;
////					}
////
//					if(!fwd && isLastChunk) {
//						if(!endsOnDelim) {
//							--contribCount;
//						}
//					}
//
//					count += contribCount;
//
//
//					prevEndsOnDelim = endsOnDelim;
//				}

				
//				System.out.println("Contribs: " + contribs);
//				long count = contribs.stream()
//						.map(Entry::getValue)
//						.reduce(Maps.immutableEntry(0l, true), (a, b) -> {
//							long tmpCount = a.getKey() + b.getKey();
//							// If fwd and prior chunk not ended on newline or
//							// bwd and prior chunk started with newline
//							if(fwd && !a.getValue() || (!fwd && a.getValue())) {
//							//if(!a.getValue()) {
//								--tmpCount;
//							}
//							
//							return Maps.immutableEntry(tmpCount, b.getValue());
//						})
//						.getKey();
//						;//.co
					
				//long count = rawCount - n + 1;
					//.collect(Collectors.toList());
				
				System.out.println("count, fwd=" + fwd + ": "  + count + " "+ sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
			}
//
	
//			
//			PageNavigator pageNavigator = new PageNavigator(
//				PageManagerForFileChannel.create(fileChannel)
////				,8, 9);
//				);
////				8, 13);
//			
//			
//			pageNavigator.posToStart();
////			pageNavigator.nextPos();
//
//			if(false) {
//				byte[] arr = new byte[100];
//				int read = pageNavigator.readBytes(arr, 0, 100);
//				
//				System.out.println("Read " + read + ": " + new String(arr, 0, read));
//				System.out.println("Done");
//			}
//
//			for(int x = 0; x < 5; ++x) {
//
//				if(true) {
//					Stopwatch sw = Stopwatch.createStarted();
//					
//					long i = 0;
//					//long i = wcFwd();
//					
//					System.out.println("fwd: " + i + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001 + " " + pageNavigator.getPos());
//				}
//	
//				if(true) {
//					Stopwatch sw = Stopwatch.createStarted();
//					long i = 0;
//
//					System.out.println("bwd: " + i + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001 + " " + pageNavigator.getPos());
//				}
//
//			}
		}
	}
}
