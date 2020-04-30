package org.aksw.jena_sparql_api.io.binsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.aksw.jena_sparql_api.io.binseach.BinarySearchOnSortedFile;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class TestBinSearch {

//    @Test
//    void testBinSearch() throws IOException {
    public static void main(String[] args) throws IOException {

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


      System.err.println("EVERYTHING");
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
