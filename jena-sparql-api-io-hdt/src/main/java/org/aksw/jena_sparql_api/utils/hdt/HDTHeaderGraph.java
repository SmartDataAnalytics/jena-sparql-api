package org.aksw.jena_sparql_api.utils.hdt;

import java.io.IOException;

import org.aksw.jena_sparql_api.utils.io.NTripleUtils;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.triples.IteratorTripleString;
import org.rdfhdt.hdtjena.HDTCapabilities;
import org.rdfhdt.hdtjena.HDTStatistics;
import org.rdfhdt.hdtjena.NodeDictionary;
import org.rdfhdt.hdtjena.solver.HDTQueryEngine;
import org.rdfhdt.hdtjena.solver.OpExecutorHDT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaption of HDTGraph for headers ~ Claus
 * 
 * @author mario.arias
 *
 */
public class HDTHeaderGraph extends GraphBase {
	private static final Logger log = LoggerFactory.getLogger(HDTHeaderGraph.class);

	private static final HDTCapabilities capabilities = new HDTCapabilities();

	private HDT hdt;
	//private NodeDictionary nodeDictionary;
	private ReorderTransformation reorderTransform;
	private HDTStatistics hdtStatistics;
	private long numSearches;
	private boolean closeAfter;
	
	static {
		// Register OpExecutor
		QC.setFactory(ARQ.getContext(), OpExecutorHDT.opExecFactoryHDT);
		HDTQueryEngine.register();
	}
	
	public HDTHeaderGraph(HDT hdt) {
		this(hdt, false);
	}
	
	public HDTHeaderGraph(HDT hdt, boolean close) {
		this.hdt = hdt;
		///this.nodeDictionary = new NodeDictionary(hdt.getDictionary());
		//this.hdtStatistics = new HDTStatistics(this);	// Must go after NodeDictionary created.
		//this.reorderTransform= new ReorderTransformationHDT(this);  // Must go after Dict and Stats
		this.closeAfter = close;
	}
	
	public HDT getHDT() {
		return hdt;
	}
	
//	public NodeDictionary getNodeDictionary() {
//		return nodeDictionary;
//	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#graphBaseFind(com.hp.hpl.jena.graph.TripleMatch)
	 */
	@Override
	protected ExtendedIterator<Triple> graphBaseFind(Triple jenaTriple) {
		String s = NodeDictionary.nodeToStr(jenaTriple.getMatchSubject());
		String p = NodeDictionary.nodeToStr(jenaTriple.getMatchPredicate());
		String o = NodeDictionary.nodeToStr(jenaTriple.getMatchObject());
		
		IteratorTripleString hdtIterator;
		try {
			hdtIterator = hdt.getHeader().search(s, p, o);
		} catch (NotFoundException e1) {
			throw new RuntimeException(e1);
		}
		ExtendedIterator<Triple> result = WrappedIterator.create(hdtIterator)
			.mapWith(t -> {
				String r;
				try {
					r = t.asNtriple().toString();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return r;
			})
			.mapWith(NTripleUtils::parseNTriplesString);
		
		return result;
	}
	
	public long getNumSearches() {
		return numSearches;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#getStatisticsHandler()
	 */
	@Override
	public GraphStatisticsHandler getStatisticsHandler() {
		return hdtStatistics;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graph.impl.GraphBase#getCapabilities()
	 */
//	@Override
//	public Capabilities getCapabilities() {
//		return HDTGraph.capabilities;
//	}

	public ReorderTransformation getReorderTransform() {
		return reorderTransform;
	}
	
	@Override
	protected int graphBaseSize() {
		return (int)hdt.getHeader().getNumberOfElements();
	}
	
	@Override
	public void close() {
		super.close();
		
		if(closeAfter) {
			try {
				hdt.close();
			} catch (IOException e) {
				log.error("Unexpected exception.", e);
			}
		}
	}
}
