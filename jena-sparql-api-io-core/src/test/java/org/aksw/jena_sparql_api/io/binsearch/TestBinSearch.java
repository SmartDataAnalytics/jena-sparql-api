package org.aksw.jena_sparql_api.io.binsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.io.binseach.BinarySearchOnSortedFile;
import org.aksw.jena_sparql_api.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.io.binseach.Block;
import org.aksw.jena_sparql_api.io.binseach.BlockIterState;
import org.aksw.jena_sparql_api.io.binseach.BlockSource;
import org.aksw.jena_sparql_api.io.binseach.BlockSources;
import org.aksw.jena_sparql_api.io.binseach.PageManager;
import org.aksw.jena_sparql_api.io.binseach.PageManagerForFileChannel;
import org.aksw.jena_sparql_api.io.binseach.SeekableSource;
import org.aksw.jena_sparql_api.io.binseach.SeekableSourceFromPageManager;
import org.aksw.jena_sparql_api.io.binseach.bz2.BlockSourceBzip2;
import org.aksw.jena_sparql_api.io.common.Reference;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class TestBinSearch {

    /**
     * Should give the roughly similar output as
     * grep -a -b 'BZh91AY&SY' file.bz2 | cut -d: -f1 | head
     * (the grep invocation gets byte position wrong which indicates that -a causes it to use characters instead of bytes...)
     * @throws IOException
     */
    public static void listBz2Blocks() throws IOException {
        Path path = Paths.get("/home/raven/tmp/sorttest/sorted.nt.bz2");

        try(FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            PageManager pageManager = PageManagerForFileChannel.create(channel);
            SeekableSource pagedSource = new SeekableSourceFromPageManager(pageManager);
            BlockSource blockSource = BlockSourceBzip2.create(pagedSource);

            if(true)
            {
                Reference<? extends Block> block = blockSource.contentAtOrAfter(21133549, true);
                BlockIterState state = new BlockIterState(true, block, null, true);
                while(state.hasNext()) {
                    state.advance();
                    System.out.println(state.block.getOffset());
                }
            }

//            0
//            39008
//            81062
//            123387
//            162881
//            243917
//            ...
//            940240
//            992484
//            1043168
            if(true)
            {
                Reference<? extends Block> block = blockSource.contentAtOrBefore(162881, true);
                BlockIterState state = new BlockIterState(true, block, null, false);
                while(state.hasNext()) {
                    state.advance();
                    System.out.println(state.block.getOffset());
                }
            }

        }
    }


//    @Test
//    void testBinSearch() throws IOException {
    public static void main(String[] args) throws IOException {
        // listBz2Blocks();
        testLookup();
    }

    public static void testLookup() throws IOException {
        Path path = Paths.get("/home/raven/tmp/sorttest/sorted.nt.bz2");
        BinarySearcher searcher = BlockSources.createBinarySearcherBz2(path);
        InputStream in = searcher.search("<https://d-nb.info/gnd/10072935-6");
        System.out.println("Got: " + StreamUtils.toString(in));
    }

    public void test() throws IOException {
        Path path = Paths.get("/home/raven/tmp/sorttest/sorted.nt");
        BinarySearchOnSortedFile searcher = BinarySearchOnSortedFile.create(path);

//        System.err.println("HIT START");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("<https://d-nb.info/gnd/10000027-7/about>")));
//
//        System.err.println("HIT END");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("_:node1e1jhg0s7x16683363")));
//
//        System.err.println("HIT MIDDLE");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("<https://d-nb.info/gnd/2102991-X>")));
//
//
//        System.err.println("MISS START");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("<https://d-nb.info/gnd/2000000-X>")));
//
//        System.err.println("MISS END");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("_:xnode1e1jhg0s7x16683363")));
//
//        System.err.println("MISS MIDDLE");
//        System.err.println(StreamUtils.toString(searcher.searchCore2("<https://d-nb.info/gnd/2102991-Y>")));
//
//
//        System.err.println("MISS START TOTAL"); // ';' < '<'
//        System.err.println(StreamUtils.toString(searcher.searchCore2(";https://d-nb.info/gnd/2102991-Y>")));
//
//        System.err.println("MISS END TOTAL"); // 'h' > '_'
//        System.err.println(StreamUtils.toString(searcher.searchCore2("https://d-nb.info/gnd/2102991-Y>")));


//      System.err.println("EVERYTHING");
//      InputStream is = searcher.searchCore2("<https://d-nb.info/gnd/10000069-1>");
//      InputStream is = searcher.searchCore2("");
      InputStream is = searcher.searchCore("<https://d-nb.info/gnd/7862131-8/about>");

      Iterator<Triple> it = RDFDataMgr.createIteratorTriples(is, Lang.TURTLE, "http://foo.bar/baz");
      while(it.hasNext()) {
          System.out.println(it.next());
      }

//      BufferedReader br = new BufferedReader(new InputStreamReader(is));// .lines();
//      for(int i = 0; i < 100; ++i) {
//          String str = br.readLine();
//          System.err.println(str);
//      }
      //lines.forEach(System.err::println);

      // System.out.println("Lines: " + lines.count());
    }
}
