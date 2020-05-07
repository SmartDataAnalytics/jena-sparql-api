package org.aksw.jena_sparql_api.io.binsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.io.binseach.BinarySearcher;
import org.aksw.jena_sparql_api.io.binseach.BlockSources;
import org.aksw.jena_sparql_api.io.binseach.MainPlaygroundScanFile;
import org.aksw.jena_sparql_api.rx.GraphOpsRx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestBinSearchBz2 {

    @Test
    public void testBinarySearchBz2Lookups() throws IOException {
        runTest();
    }

//    public static void main(String[] args) throws IOException {
//        runTest();
//    }

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
        Map<Node, Graph> map = RDFDataMgrRx.createFlowableTriples(
                    () -> new BZip2CompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ), true),
                    Lang.NTRIPLES, null)
                .compose(GraphOpsRx.graphsFromConsecutiveSubjectsRaw())
                .toMap(Entry::getKey, Entry::getValue)
                .blockingGet()
                ;

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel);

            // This key overlaps on the block boundary (byte 2700000)
//            try(InputStream in = bs.search("<http://linkedgeodata.org/geometry/node1012767568>")) {
//                MainPlaygroundScanFile.printLines(in, 10);
//            }
//

            // Generic tests

            int i = 0;
            for(Entry<Node, Graph> e : map.entrySet()) {
                Node s = e.getKey();
//                System.out.println("Test #" + (++i) + ": " + s);
                Graph expected = e.getValue();

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
