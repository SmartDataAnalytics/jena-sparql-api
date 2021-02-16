package org.aksw.jena_sparql_api.io.hdt;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.writer.WriterGraphRIOTBase;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;


public class WriterGraphRIOT_HDT extends
	WriterGraphRIOTBase
{
	@Override
	public Lang getLang() {
		return JenaPluginHdt.LANG_HDT;
	}

	@Override
	public void write(Writer writer, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
		OutputStream out = new WriterOutputStream(writer, StandardCharsets.UTF_8);
		write(out, graph, prefixMap, baseURI, context);
	}

	/** Encode for HDT; IRIs do not make use of angular brackets otherwise it is turtle syntax */
	public static String encode(Node node, String baseURI, PrefixMap prefixMap) {
		String result = node.isURI()
				? node.getURI()
				: NodeFmtLib.str(node); // Not used: baseURI, prefixMap);
		return result;
	}
	
	public static TripleString encode(Triple triple, String baseURI, PrefixMap prefixMap) {
		TripleString result = new TripleString(
				encode(triple.getSubject(), baseURI, prefixMap),
				encode(triple.getPredicate(), baseURI, prefixMap),
				encode(triple.getObject(), baseURI, prefixMap));

		return result;
	}
	
	@Override
	public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String rawBaseURI,
			Context context) {
		
		String baseURI = rawBaseURI == null ? "http://www.example.org/" : rawBaseURI;
		
		ExtendedIterator<TripleString> it = graph.find()
				.mapWith(t -> encode(t, baseURI, prefixMap));

		try {
			HDT hdt = HDTManager.generateHDT(it, baseURI, new HDTSpecification(), null);
			hdt.saveToHDT(out, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			it.close();
		}						
	}
};
