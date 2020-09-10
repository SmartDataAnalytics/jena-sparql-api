package org.aksw.jena_sparql_api.io.binsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.io.binseach.BlockSources;
import org.aksw.jena_sparql_api.rx.GraphOpsRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;


public class TestBinSearchBz2 {

    private static Logger logger = LoggerFactory.getLogger(TestBinSearchBz2.class);

    @Test
    public void testBinarySearchBz2Lookups() throws IOException {
        runTest();
    }

//    public static void main(String[] args) throws IOException {
//        runTest();
//    }

    @Test
    public void testFullRead() throws IOException {
        Path path = Paths.get("src/test/resources/2015-11-02-Amenity.node.5mb-uncompressed.sorted.nt.bz2");
//        Path path = Paths.get("/home/raven/tmp/sorttest/sorted.nt.bz2");

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            BufferedReader expected = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ), true)))) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel, false);

            BufferedReader actual = new BufferedReader(new InputStreamReader(bs.search((String)null)));

            String lineActual;
            String lineExpected;
            for(int i = 1; ; ++i) {
                lineActual = actual.readLine();
                lineExpected = expected.readLine();

//                System.out.println("Line: " + i);
                if(!Objects.equals(lineExpected, lineActual)) {
                    logger.warn("Mismatch in line " + i);
                    Assert.assertEquals(lineExpected, lineActual);
                }

                if(lineActual == null && lineExpected == null) {
                    break;
                }
            }

        }

    }

    /**
     * For future reference and quantifying improvements:
     * The first working version of this test took [11.6, 11.3, 11.5] seconds ~ 2020-05-08 Claus Stadler
     *
     *
     *
     *
     */
    public static void runTest() throws IOException {
        Path path = Paths.get("src/test/resources/2015-11-02-Amenity.node.5mb-uncompressed.sorted.nt.bz2");

        // Read file and map each key to the number of lines
        Stopwatch sw = Stopwatch.createStarted();
        Map<Node, Graph> map = RDFDataMgrRx.createFlowableTriples(
                    () -> new BZip2CompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ), true),
                    Lang.NTRIPLES, null)
                .compose(GraphOpsRx.graphsFromConsecutiveSubjectsRaw())
                .toMap(Entry::getKey, Entry::getValue)
                .blockingGet()
                ;

        // Note that the logged time is for cold state - repeated loads should
        // exhibit significant speedups
        logger.debug("Needed " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds to load " + path);

        sw.reset().start();
        int i = 0;
        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel, false);

            // This key overlaps on the block boundary (byte 2700000)
//            try(InputStream in = bs.search("<http://linkedgeodata.org/geometry/node1012767568>")) {
//                MainPlaygroundScanFile.printLines(in, 10);
//            }
//

            // Generic tests

            for(Entry<Node, Graph> e : map.entrySet()) {
                Node s = e.getKey();
                ++i;
//                System.out.println("Test #" + (++i) + ": " + s);
                Graph expected = e.getValue();

//                if(s.getURI().equals("http://linkedgeodata.org/geometry/node1012767568")) {
//                    System.err.println("DEBUG POINT");
//                }

                //String str = s.isURI() ? "<" + s.getURI() + ">" : s.getBlankNodeLabel()
                String str = NodeFmtLib.str(s);
                try(InputStream in = bs.search(str)) {
                    Graph actual = GraphFactory.createDefaultGraph();
                    RDFDataMgr.read(actual, in, Lang.NTRIPLES);


                    // Assert.assertEquals(expected, actual);
                    boolean isOk = expected.isIsomorphicWith(actual);
                    if(!isOk) {
                        System.err.println("Expected:");
                        RDFDataMgr.write(System.err, expected, RDFFormat.TURTLE_PRETTY);
                        System.err.println("Actual:");
                        RDFDataMgr.write(System.out, actual, RDFFormat.TURTLE_PRETTY);
                    }
                    Assert.assertTrue(isOk);
                }

            }

        }

        logger.debug("Needed " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds for " + i + " lookups on " + path);

    }


    @Test
    public void testLocalBinSearch() throws IOException, Exception {
        try(BinarySearcher bs = BlockSources.createBinarySearcherText(Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt"))) {
            try (InputStream in = bs.search("<https://d-nb.info/1017454930>")) {
                System.out.println("Output: " + IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        }
    }

}

//public static void doAssert(BinarySearcher searcher, String key, int expectedLines) throws IOException {
//    try(InputStream in = searcher.search(key)) {
//        List<String> lines = new BufferedReader(new InputStreamReader(in))
//                .lines().collect(Collectors.toList());
//        //MainPlaygroundScanFile.printLines(in, 5);
//        int actual = lines.size();
//        Assert.assertEquals(expectedLines, actual);
//    }
//}
//
            // A record in the middle of a block
//             String str = "<http://linkedgeodata.org/geometry/node1583470199>";

            // This one is the first record in a block:
//            String str = "<http://linkedgeodata.org/geometry/node1583253778>";

            // This one is overlapping before node1583253778
//            String str = "<http://linkedgeodata.org/geometry/node1583253655>";

            // This key is on the first page and the key itself overlaps with the page boundary
             //String str = "<http://linkedgeodata.org/geometry/node1003603551>";

            // First key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000005269>";


            // Second key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000006271>";

            // Mistyped key (missing '<')
            // doAssert(bs, "http://linkedgeodata.org/geometry/node1000005269", 0);

            // Empty string should match everything - needs special handling?
            // String str = "";
//            try(InputStream in = bs.search(str)) {
//                MainPlaygroundScanFile.printLines(in, 5);
//            }
