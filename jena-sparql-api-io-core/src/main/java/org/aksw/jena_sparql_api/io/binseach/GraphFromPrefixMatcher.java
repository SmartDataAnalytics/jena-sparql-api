package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.lang.RiotParsers;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Streams;

/**
 *
 * TODO Check whether Graph is the appropriate abstraction
 *
 * @author raven
 *
 */
public class GraphFromPrefixMatcher extends GraphBase {
    private static final Logger logger = LoggerFactory.getLogger(GraphFromPrefixMatcher.class);

    protected Path path;
    protected FileChannel channel = null;
    protected BinarySearchOnSortedFile searcher = null;

    public GraphFromPrefixMatcher(Path path) {
        super();
        this.path = path;
    }

    protected ExtendedIterator<Triple> graphBaseFindCore(Triple triplePattern) throws Exception {
        // Init channel on first request

        // TODO Improve resource management ; we should close the seekable and the pageManager
        if(channel == null) {
            channel = FileChannel.open(path, StandardOpenOption.READ);
            PageManager pageManager = PageManagerForFileChannel.create(channel);
            Seekable seekable = new PageNavigator(pageManager);
            long channelSize = channel.size();
            searcher = new BinarySearchOnSortedFile(seekable, channelSize, (byte)'\n');
        }

        ExtendedIterator<Triple> result;

        // Construct the prefix from the subject
        // Because whitespaces between subject and predicate may differ, do not include
        // further components
        String prefix;
        Node s = triplePattern.getSubject();
        if(s.equals(Node.ANY) || s.isVariable()) {
            prefix = "";
        } else if(s.isBlank()) {
            prefix = "_:" + s.getBlankNodeLabel();
        } else if(s.isURI() ){
            prefix = "<" + s.getURI() + ">";
        } else {
            // Literal in subject position - skip
            prefix = null;
        }

//        System.out.println("PREFIX: " + prefix);

        InputStream in = searcher.search(prefix);

        Stream<Triple> baseStream = Streams.stream(
                //RDFDataMgrRx.createIteratorTriples(in, Lang.NTRIPLES, "http://www.example.org/"));
                RiotParsers.createIteratorNTriples(in, null, RDFDataMgrRx.dftProfile()));

        if(prefix.length() > 0) {
            List<Triple> t = baseStream.collect(Collectors.toList());
            System.out.println("For prefix " + prefix + " got " + t.size() + " triples");
            baseStream = t.stream();
        } else {
            System.out.println("Got pattern: " + triplePattern);
        }

        Iterator<Triple> itTriples = baseStream
            .filter(triplePattern::matches)
            .iterator();

        result = WrappedIterator.create(itTriples);
        result = ExtendedIteratorClosable.create(itTriples, () -> {
            in.close();
        });

        return result;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
        ExtendedIterator<Triple> result;
        try {
            result = graphBaseFindCore(triplePattern);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Graph createGraphFromSortedNtriples(Path path) {
        Graph result = new GraphFromPrefixMatcher(path);
        return result;
    }

//	public static Graph createGraphFromSortedNtriples(Path path, int maxCacheSize, int bufferSize) {
//		// TODO Properly pass these parameters to the search component
//		Graph result = new GraphFromFileSystem(path);
//		return result;
//	}

    @Override
    public void close() {
        if(channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                logger.warn("Exception on close", e);
            }
        }

        super.close();
    }

    @Override
    protected GraphStatisticsHandler createStatisticsHandler() {
        return new GraphStatisticsHandler() {
            @Override
            public long getStatistic(Node S, Node P, Node O) {
                if(S.isConcrete()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
    }

    public static void main(String[] args) throws IOException {

        JenaSystem.init();
        Function<String, SparqlStmt> parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, DefaultPrefixes.prefixes, false);

//		Path path = Paths.get("/home/raven/Projects/Data/LSQ/wtf100k.nt");
        Path path = Paths.get("/home/raven/Projects/Data/LSQ/deleteme.sorted.nt");

//		Path path = Paths.get("/home/raven/Projects/Data/LSQ/wtf.sorted.nt");
        Graph graph = GraphFromPrefixMatcher.createGraphFromSortedNtriples(path);

        Model m = ModelFactory.createModelForGraph(graph);

        String queryStr;

        Iterator<String> itSubject = Files.lines(Paths.get("/home/raven/Projects/Data/LSQ/subjects.shuffled.txt")).iterator();

        Stopwatch stopwatch = Stopwatch.createStarted();
        int i = 0;
        while(itSubject.hasNext()) {
            if(i % 100 == 0) {
                System.out.println(i);
            }
            ++i;

            String s = itSubject.next();
            queryStr = "SELECT * { " + s + " ?p ?o }";
            //System.out.println(queryStr);
            try(QueryExecution qe = QueryExecutionFactory.create(queryStr, m)) {
                long numResults = ResultSetFormatter.consume(qe.execSelect());
                if(numResults > 0) {
                    int xxx = 5;
                }
                //System.out.println("Num results: " + numResults);
                //System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        }
        System.out.println("Processed items in " + (stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds");

        queryStr = parser.apply("SELECT *\n" +
                "           {\n" +
                "             # TODO Allocate some URI based on the dataset id\n" +
                "             BIND(BNODE() AS ?report)\n" +
                "             { SELECT ?p (COUNT(*) AS ?numTriples) (COUNT(DISTINCT ?s) AS ?numUniqS) (COUNT(DISTINCT ?o) AS ?numUniqO) {\n" +
                "               ?s ?p ?o\n" +
                "             } GROUP BY ?p }\n" +
                "           }").toString();

//		queryStr = "SELECT * { ?s ?p ?o . ?o ?x ?y . ?y ?a ?b } LIMIT 100 OFFSET 100000";
//		queryStr = "SELECT * { <http://lsq.aksw.org/res/swdf> ?p ?o } LIMIT 10";

//		Stopwatch stopwatch = Stopwatch.createStarted();
//		try(QueryExecution qe = QueryExecutionFactory.create(queryStr, m)) {
//			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//		}
//	    System.out.println("Processed items in " + (stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds");



        //Set<Resource> res = Streams.stream(m.listSubjects()).limit(1000).collect(Collectors.toSet());
//
//		Collection<Resource> res = Arrays.asList(
//				m.createResource("http://lsq.aksw.org/res/re-swdf-q-6ffab076-8ab0c230a859bbe3-02014-52-27_03:52:43")
//		);
//
//		for(Resource r: res) {
//			System.out.println(r);
//			for(Statement stmt : r.listProperties().toList()) {
//				System.out.println("  " + stmt);
//			}
////			System.out.println(r + ": " + r.listProperties().toSet().size());
//		}

    }

    /*
     * String prefix;
     *
     * // Everything // prefix = "";
     *
     * // First line prefix =
     * "<http://lsq.aksw.org/res/re-swdf-q-6ffab076-8ab0c230a859bbe3-02014-52-27_03:52:43>";
     *
     * // In the middle // prefix = "<http://lsq.aksw.org/res/q-4a68281e>"; //
     * prefix = "<http://lsq.aksw.org/res/q-4a6838ea>"; // prefix =
     * "<http://lsq.aksw.org/res/re-swdf-q-ffcf2ff2-8ab0c230a859bbe3-02014-06-26_07:06:31>";
     *
     * // Last line // prefix = "<http://lsq.aksw.org/res/swdf>";
     *
     * // Random stuff // prefix = "<http://lsq.ak>"; // prefix = "<zzzzzz>";
     *
     * // Files.lines(Paths.get("/home/raven/Projects/Data/LSQ/sorted.nt")) //
     * .map(str -> str.substring(0, 3)) // .forEach(str ->
     * searcher.search(str).forEach(System.err::println));
     *
     *
     * searcher.search(prefix) .forEach(System.err::println);
     *
     */
}
