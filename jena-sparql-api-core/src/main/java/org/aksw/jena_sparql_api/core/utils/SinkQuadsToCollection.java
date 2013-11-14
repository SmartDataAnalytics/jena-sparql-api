package org.aksw.jena_sparql_api.core.utils;

import java.util.Collection;
import java.util.HashSet;

import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.sparql.core.Quad;


public class SinkQuadsToCollection<C extends Collection<? super Quad>>
	implements Sink<Quad>
{
	private C quads;
	
	public SinkQuadsToCollection(C quads) {
		this.quads = quads;
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public void send(Quad item) {
		quads.add(item);
	}
	
	@Override
	public void flush() {
	}
	
	public C getQuads() {
		return quads;
	}
	
	public static SinkQuadsToCollection<HashSet<Quad>> createSinkHashSet() {
		return createSink(new HashSet<Quad>());
	}

	public static <C extends Collection<? super Quad>> SinkQuadsToCollection<C> createSink(C quads) {
		SinkQuadsToCollection<C> result = new SinkQuadsToCollection<C>(quads);
		return result;
	}
}