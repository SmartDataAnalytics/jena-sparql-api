package org.aksw.jena_sparql_api.io.hdt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

public class ReaderRIOT_HDT
    implements ReaderRIOT
{
    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        InputStream in = new ReaderInputStream(reader, StandardCharsets.UTF_8);
        read(in, baseURI, ct, output, context);
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        HDT hdt = null;
        try {
            hdt = HDTManager.loadHDT(new BufferedInputStream(in));
            Graph graph = null;

            try {
                graph = new HDTGraph(hdt);
                ExtendedIterator<Triple> it = graph.find();
                try {
                    while(it.hasNext()) {
                        Triple t = it.next();
                        output.triple(t);
                    }
                } finally {
                    it.close();
                    output.finish();
                }
            } finally {
                if (graph != null) {
                    graph.close();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (hdt != null) {
                    hdt.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
