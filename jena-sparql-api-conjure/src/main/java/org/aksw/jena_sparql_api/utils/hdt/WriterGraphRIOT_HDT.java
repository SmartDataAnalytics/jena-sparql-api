package org.aksw.jena_sparql_api.utils.hdt;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.graph.Graph;
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

	@Override
	public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI,
			Context context) {
		ExtendedIterator<TripleString> it = graph.find()
				.mapWith(x -> new TripleString(
						NodeFmtLib.str(x.getSubject(), baseURI, prefixMap),
						NodeFmtLib.str(x.getPredicate(), baseURI, prefixMap),
						NodeFmtLib.str(x.getObject(), baseURI, prefixMap)));

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
