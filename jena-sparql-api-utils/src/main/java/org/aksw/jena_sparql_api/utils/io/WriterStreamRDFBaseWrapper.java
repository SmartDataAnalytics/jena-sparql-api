package org.aksw.jena_sparql_api.utils.io;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.shared.PrefixMapping;

/**
 * A wrapper for {@link WriterStreamRDFBase} to access its internal prefix map.
 * This enables writing out prefix'd RDF w.r.t. a fixed set of prefixes
 * without writing out the prefixes themselves.
 * This is particularly useful when serializing RDF in parallel such as with Apache Spark.
 * 
 * @author Claus Stadler
 *
 */
public class WriterStreamRDFBaseWrapper
	extends StreamRDFWrapper
{
	/** The delegate's prefix map obtained via reflection */
	protected PrefixMap pMap;
	
	public WriterStreamRDFBaseWrapper(WriterStreamRDFBase other) {
		super(other);
		
		try {
			Field pMapField = WriterStreamRDFBase.class.getDeclaredField("pMap");
			pMapField.setAccessible(true);
			pMap = (PrefixMap)pMapField.get(other);
			pMapField.setAccessible(false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public PrefixMap getPrefixMap() {
		return pMap;
	}
	
	
	/**
	 * Wrap the given {@link WriterStreamRDFBase} instance such that prifxes are
	 * no longer delegated to it
	 * 
	 * @param fixedPrefixes
	 * @param delegate
	 * @return
	 */
	public static WriterStreamRDFBaseWrapper wrapWithoutPrefixDelegation(WriterStreamRDFBase delegate) {
		WriterStreamRDFBaseWrapper result = new WriterStreamRDFBaseWrapper(delegate) {
			@Override
			public void prefix(String prefix, String iri) {
				// Do not delegate prefixes as they are fixed
			}
		};


		return result;
	}

	/**
	 * First wrap the given {@link WriterStreamRDFBase} instance such that prefixes are no longer delegated.
	 * Then <b>add</b> the given set of fixed prefixes to the delegate's internal prefix map.
	 * Typically, the internal prefix map should be empty.
	 * 
	 * @param fixedPrefixes
	 * @param delegate
	 * @return
	 */
	public static WriterStreamRDFBaseWrapper wrapWithFixedPrefixes(PrefixMapping fixedPrefixes, WriterStreamRDFBase delegate) {
		WriterStreamRDFBaseWrapper result = wrapWithoutPrefixDelegation(delegate);

		
		for (Entry<String, String> e : fixedPrefixes.getNsPrefixMap().entrySet()) {
			result.getPrefixMap().add(e.getKey(), e.getValue());
		}

		return result;
	}

}
