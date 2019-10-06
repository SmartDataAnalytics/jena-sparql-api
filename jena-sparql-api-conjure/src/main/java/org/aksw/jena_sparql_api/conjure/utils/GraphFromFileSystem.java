package org.aksw.jena_sparql_api.conjure.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

public class GraphFromFileSystem extends GraphBase {
	protected Path path;

	public GraphFromFileSystem(Path path) {
		super();
		this.path = path;
	}

	public static Triple parseNtripleString(String str)  {
		Triple result;
		try(InputStream is = new ByteArrayInputStream(str.getBytes())) {			
			ParserProfile profile = RiotLib.dftProfile();
			Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(is);
			LangNTriples parser = new LangNTriples(tokenizer, profile, null);
			result = parser.next();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}

	protected ExtendedIterator<Triple> graphBaseFindCore(Triple triplePattern) throws Exception {
		ExtendedIterator<Triple> result;
		FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
		BinarySearchForSortedFiles searcher = new BinarySearchForSortedFiles(channel);

		// Construct the prefix from the subject
		// Because whitespaces between subject and predicate may differ, do not include
		// further components
		String prefix;
		Node s = triplePattern.getSubject();
		if(s.equals(Node.ANY) || s.isVariable()) {
			prefix = "";
		} else if(s.isBlank()) {
			prefix = "_:";
		} else {
			prefix = "<" + s.getURI() + ">";
		}
//		System.out.println("Prefix: " + prefix);

		Iterator<Triple> itTriples = searcher.search(prefix)
//			.peek(System.out::println)
			.map(GraphFromFileSystem::parseNtripleString)
			.filter(triplePattern::matches)
			.iterator();
		
		result = ExtendedIteratorClosable.create(itTriples, () -> {
			searcher.close();
			channel.close();
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
		Graph result = new GraphFromFileSystem(path);
		return result;
	}
	
	public static void main(String[] args) {
		
		Path path = Paths.get("/home/raven/Projects/Data/LSQ/deleteme.sorted.nt");
		Graph graph = GraphFromFileSystem.createGraphFromSortedNtriples(path);
		
		Model m = ModelFactory.createModelForGraph(graph);

		//Set<Resource> res = Streams.stream(m.listSubjects()).limit(1000).collect(Collectors.toSet());
		
		Collection<Resource> res = Arrays.asList(
				m.createResource("http://lsq.aksw.org/res/re-swdf-q-6ffab076-8ab0c230a859bbe3-02014-52-27_03:52:43")
		);

		for(Resource r: res) {
			System.out.println(r);
			for(Statement stmt : r.listProperties().toList()) {
				System.out.println("  " + stmt);
			}
//			System.out.println(r + ": " + r.listProperties().toSet().size());
		}
		
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
