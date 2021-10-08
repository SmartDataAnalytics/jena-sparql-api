package org.aksw.jena_sparql_api.rx.io.resultset;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.rx.DatasetGraphFactoryEx;
import org.aksw.jena_sparql_api.util.iri.PrefixUtils;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.model.PrefixMapAdapter;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Sink for deferring the sending of datasets to the delegate so that used prefixes
 * can be analyzed for a given number of datasets.
 * 
 * @author raven
 *
 */
public class SinkStreamingDataset
	extends SinkStreamingBase<Dataset>
{
	protected SinkStreamingStreamRDF delegate;
	protected List<DatasetGraph> deferredData;

	// If true any seen prefixes on datasets are added to the base prefixes
	protected boolean allowExtendBasePrefixes = true;
	protected PrefixMapping basePrefixes;
	
	protected PrefixMapping usedPrefixes = new PrefixMappingImpl();
	
	// Adapter to bridge between riot and rdf
	protected PrefixMap usedPrefixAdapter = new PrefixMapAdapter(usedPrefixes);
	
	// Wait for this number of events before used-prefix
	// analysis and prefix optimize-prefixes is performed and output starts
	protected long remainingDeferrals;

	/**
	 * 
	 * 
	 * @param delegate
	 * @param basePrefixes The initial set of prefixes. Interally a copy is made.
	 * @param remainingDeferrals
	 * @param allowExtendBasePrefixes
	 */
	public SinkStreamingDataset(
			SinkStreamingStreamRDF delegate,
			PrefixMapping basePrefixes,
			long remainingDeferrals,
			boolean allowExtendBasePrefixes) {
		super();
		this.delegate = delegate;
		this.deferredData = remainingDeferrals > 1 ? new ArrayList<>() : null;
		this.allowExtendBasePrefixes = allowExtendBasePrefixes;
		this.basePrefixes = new PrefixMappingImpl();
		this.basePrefixes.setNsPrefixes(basePrefixes);
		this.remainingDeferrals = remainingDeferrals;
	}

	@Override
	public void flush() {
		delegate.flush();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public final void finishActual() {
		delegate.finish();
	}

	@Override
	protected void sendActual(Dataset item) {
		
		DatasetGraph dg = item.asDatasetGraph();

		if (remainingDeferrals > 0) {
			if (allowExtendBasePrefixes) {
				basePrefixes.setNsPrefixes(item.getDefaultModel().getNsPrefixMap());
			}


			// Iterate through all nodes in the dataset's default graph and
			// named graphs and collect all used prefixes
			Streams
				.concat(
					Streams.stream(dg.getDefaultGraph().find()).flatMap(TripleUtils::streamNodes),
					Streams.stream(dg.find()).flatMap(QuadUtils::streamNodes))
				.forEach(node -> PrefixUtils.usedPrefixes(node, basePrefixes, usedPrefixes));

			--remainingDeferrals;

			if (remainingDeferrals == 0) {
				StreamRDFOps.sendPrefixesToStream(usedPrefixes, delegate);
				
				for (DatasetGraph d : deferredData) {
					StreamRDFOps.sendDatasetToStream(d, delegate, null, usedPrefixAdapter);
				}

				// Unset deferredData to Allow for garbage collection
				deferredData = null;				

				// Write out the current item
				StreamRDFOps.sendDatasetToStream(dg, delegate, null, usedPrefixAdapter);

			} else {
				// Defer the current item
				DatasetGraph copy = DatasetGraphFactoryEx.createInsertOrderPreservingDatasetGraph();
				DatasetGraphUtils.addAll(copy, dg);
				deferredData.add(copy);
			}
			
			
		} else {
			// Write out the current item
			StreamRDFOps.sendDatasetToStream(dg, delegate, null, usedPrefixAdapter);			
		}		
	}
	
//	abstract public void expandPrefixe(T item, PrefixMapping target);
//	abstract public void analyzePrefixes(T item);
}

