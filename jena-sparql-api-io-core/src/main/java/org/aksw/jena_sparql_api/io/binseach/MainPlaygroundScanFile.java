package org.aksw.jena_sparql_api.io.binseach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.BlockSources;
import org.aksw.commons.io.block.impl.PageManagerForByteBuffer;
import org.aksw.commons.io.block.impl.PageManagerForFileChannel;
import org.aksw.commons.io.block.impl.PageNavigator;
import org.aksw.commons.io.seekable.api.SeekableSource;
import org.aksw.commons.io.seekable.impl.SeekableFromBlock;
import org.aksw.commons.io.seekable.impl.SeekableSourceFromPageManager;
import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.io.binseach.bz2.BlockSourceBzip2;
import org.aksw.jena_sparql_api.io.deprecated.BoyerMooreMatcherFactory;
import org.aksw.jena_sparql_api.io.deprecated.SeekableMatcher;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.QueryExecutionFactory;

import com.github.jsonldjava.shaded.com.google.common.primitives.Ints;


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
            boolean posChanged = pageNavigator.nextPos(1);
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
        pageNavigator.prevPos(1);

        //boolean startsOnDelim = !pageNavigator.isPosBeforeStart() && pageNavigator.get() == delim;
        boolean endsOnDelim = false;

        for(;;) {
            // Pos to prev can move one character before the stream if the char is not found
            pageNavigator.posToPrev(delim);
            //byte bAt = pageNavigator.get();
            //System.out.println("Got byte: " + bAt + " at " + pageNavigator.getPos());
            boolean posChanged = pageNavigator.prevPos(1);
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

    public static void main(String[] args) throws Exception {
        mainBz2DecodeNice(args);
//		mainBoyerMooreTest(args);
    }


    public static void mainBz2DecodeNice(String[] args) throws Exception {
        Path path = Paths.get("/home/raven/Downloads/2015-11-02-Amenity.node.sorted.fixed.nt.bz2");

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel, PageManagerForFileChannel.DEFAULT_PAGE_SIZE, false);

            System.out.println("Matches:");
            // A record in the middle of a block
//             String str = "<http://linkedgeodata.org/geometry/node1583470199>";

            // This one is the first record in a block:
//            String str = "<http://linkedgeodata.org/geometry/node1583253778>";

            // This one is overlapping before node1583253778
//            String str = "<http://linkedgeodata.org/geometry/node1583253655>";

            // This key is on the first page and the key itself overlaps with the page boundary
             String str = "<http://linkedgeodata.org/geometry/node1003603551>";

            // First key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000005269>";


            // Second key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000006271>";

            // Mistyped key (missing '<')
            // String str = "http://linkedgeodata.org/geometry/node1000005269";

            // Empty string should match everything - needs special handling?
            // String str = "";
            try(InputStream in = bs.search(str)) {
                printLines(in, 5);
            }
        }

    }

    public static void printLines(InputStream in, int count) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        int i = 0;
        while((line = br.readLine()) != null) {
            ++i;
            System.out.println(line);
            if(i > count) { break; }
        }
        System.out.println(i + " lines");
    }

    public static void mainBz2Decode(String[] args) throws Exception {

        Path path = Paths.get("/home/raven/Downloads/2015-11-02-Amenity.node.sorted.fixed.nt.bz2");

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            PageManager pageManager = PageManagerForFileChannel.create(fileChannel);
            long maxBlockOffset = pageManager.size();

            SeekableSource pagedSource = new SeekableSourceFromPageManager(pageManager);
            BlockSource blockSource = BlockSourceBzip2.create(pagedSource);

            byte[] prefix = "<http://linkedgeodata.org/geometry/node1583470199>".getBytes();

            Ref<? extends Block> blockRef = BlockSources.binarySearch(blockSource, 0, maxBlockOffset, (byte)'\n', prefix);
            if(blockRef == null) {
                System.out.println("No match found");
                return;
            }
            Block block = blockRef.get();

            System.out.println("Block offset: " + block.getOffset());

            // Load the block full + extra bytes up to the start of the first record in the
            // next block


            int extraBytes = 0;
            BlockIterState it = BlockIterState.fwd(true, blockRef, true);
            while(it.hasNext()) {
                it.advance();
                SeekableFromBlock seekable = new SeekableFromBlock(it.blockRef, 0, 0);
                boolean found = seekable.posToNext((byte)'\n');
                if(found) {
                    extraBytes = Ints.checkedCast(seekable.getPos());
                    it.closeCurrent();
                    break;
                }
            }

            // extraBytes = 0;
            System.out.println("Extra bytes: " + extraBytes);

            long blockSize = block.length();
            System.out.println("Block size: " + blockSize);
            long maxPos = blockSize + extraBytes;

            SeekableFromBlock decodedView = new SeekableFromBlock(it.blockRef, 0, 0, Long.MIN_VALUE, maxPos);



//             Seekable decodedView = block.newChannel();



            //SeekableSource decodedViewSource = new SeekableSourceFromBufferSource(blockSource);
//			Seekable decodedView = decodedViewSource.get(511604800);
            // Seekable decodedView = decodedViewSource.get(11604800);

            // preload the whole block
            // TODO With bz2 we can know the size of the decoded data block in advance - i.e. 900KB
            // So we don't have to load it fully - but instead can load half of it first!
            // TODO It may be even better to first do binary search on the loaded region, and if the entry was not found
            // then load twice the known region - but it may well be that loading the first half of the region has the better
            // avg complexity if we assume lookups for keys to be distributed evenly across the block
            // TODO Make this less hacky
//            Seekable decodedView = block.newChannel(); //new SeekableFromChannelFactory(block);
//            decodedView.setPos(Long.MAX_VALUE);
//            decodedView.get();
//            decodedView.setPos(0);
//            long max = decodedView.size();

//            byte[] prefix = "<http://linkedgeodata.org/geometry/node1583470200>".getBytes();





            long findPos = decodedView.binarySearch(0, maxPos, (byte)'\n', prefix);

            if(findPos == Long.MIN_VALUE) {
                System.out.println("No pos found in block");
                return;
            }
            System.out.println(findPos);


            // Seekable continuousView = new SeekableFromBlock(blockRef, (int)findPos, findPos);


            long start = BinarySearchOnSortedFile.getPosOfFirstMatch(decodedView, (byte)'\n', prefix);
            decodedView.setPos(start + 1);

            BinSearchScanState state = new BinSearchScanState();
            state.firstDelimPos = start;
            state.matchDelimPos = findPos;
            state.prefixBytes = prefix;
            state.size = Long.MAX_VALUE;


            InputStream in = BinarySearchOnSortedFile.newInputStream(decodedView, state);




            System.out.println("Start: " + start);

//            BinarySearchOnSortedFile searcher = new BinarySearchOnSortedFile(continuousView, Long.MAX_VALUE, (byte)'\n');
//
//            searcher.sea



//            System.out.println(findPos);

            // TODO We now need to scan backwards to the beginning of a block
            // If we reach the beginning of the current block, we need to decode the whole prior block
            // and continue scanning backwards.
            // This can repeat over multiple blocks
            // If we reach the end of the current block, we have to continue scanning in the next one
            // But the main point as this stage is, that we can now abstract contiguous blocks as a virtual
            // continuous one



            // So to generalize this:
            // Find candidate record start
            // Verify if it is an actual record start
            // Extract information

            // The more fine grained approach from the trig reader
            // - find candidate record start
            //  invoke constituent parser (in nquads, a constituent would be a quad with a certain graph)
            //    and extract component from constituent info (nquads -> graph)
            //  find further candidate record starts ; this step can now include a pattern that skips over constituents with the same component
            // - skip to first constituent that is certainly a new record (i.e. the first quad with a different graph)
            //

//            decodedView.prevPos(1);
//            decodedView.posToPrev((byte)'\n');
//            decodedView.nextPos(1);
            // InputStream in = Channels.newInputStream(decodedView);


//			Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, Lang.NTRIPLES, null);
//			while(it.hasNext()) {
//				Triple t = it.next();
//				System.out.println(t);
//			}
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            int i = 0;
            while((line = br.readLine()) != null) {
                ++i;
                System.out.println(line);
                if(i > 100) { break; }
            }
            System.out.println(i + " lines");

            //bufferSource.contentBefore(10000000);

        }
    }

    public static void mainBoyerMooreTest(String[] args) throws IOException {

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
//		Path path = Paths.get("/home/raven/Downloads/2015-11-02-Amenity.node.sorted.nt.bz2");

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            PageManager pageManager = PageManagerForFileChannel.create(fileChannel);


            byte[] text = "ABAAAABAACDABA".getBytes();
//			byte[] text = "BAACDABA".getBytes();
            pageManager = new PageManagerForByteBuffer(ByteBuffer.wrap(text));
//			pageManager = new PageManagerWrapper(pageManager, 0, pageManager.getPageSize() / 4);
//			pageManager = new PageManagerWrapper(pageManager, 0, pageManager.getPageSize());

            PageNavigator nav = new PageNavigator(pageManager);
            nav.posToStart();
            //nav.nextPos();

//			String pattern = "t%2FP625%3E++%3Fvar4+.%0";
//			String pattern = "SELECT";
//			String pattern = "AACD";
//			String pattern = "BAA";
            String pattern = "ABA";
            //String pattern = "cddd".getBytes();

            byte[] patternBytes = pattern.getBytes();

            //.compressed_magic:48 0x314159265359 (BCD (pi))
            //
//			patternBytes = new BigInteger("425a6839", 16).toByteArray();
//			patternBytes = new BigInteger("314159265359", 16).toByteArray();
            SeekableMatcher matcher = BoyerMooreMatcherFactory.createFwd(patternBytes).newMatcher();
            // nav.setPos(patternBytes.length);

            for(int xx = 0; xx < 10; ++xx) {
            Stopwatch sw = Stopwatch.createStarted();
            matcher.resetState();
            nav.posToStart();
            int matchCnt = 0;
            while(matcher.find(nav)) {
                ++matchCnt;
//				System.out.println("Got match at pos " + nav.getPos());
//				String line = nav.readLine();
//				System.out.println("Pos after match: " + nav.getPos() + " " + line);
            }
            System.out.println("Nav at pos: " + nav.getPos());
            System.out.println("Got " + matchCnt + " matches in " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
            }
            if(true) {
                return;
            }


            for(int i = 0; i < str.length(); ++i) {
                System.out.println(i);
                nav.posToStart();
                nav.nextPos(1);
                nav.nextPos(i);

                System.out.println("got: " + (char)nav.get());
            }

    //		for(int i = str.length(); i >= 0; --i) {
            for(int i = 4; i >= 0; --i) {
                System.out.println(i);
                nav.posToEnd();
                nav.prevPos(1);
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
            PageManager pageManager = PageManagerForFileChannel.create(fileChannel);
//			PageManager pageManager = PageManagerForFileChannel.create(fileChannel,  128 * 1024 * 1024);
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
