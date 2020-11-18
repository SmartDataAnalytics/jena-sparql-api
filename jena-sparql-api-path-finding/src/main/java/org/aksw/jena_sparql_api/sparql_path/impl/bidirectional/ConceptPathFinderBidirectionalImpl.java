package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

/**
 * A concept path finder capable of finding paths in both directions.
 *
 *
 *
 * @author Claus Stadler, Nov 11, 2018
 *
 */
//public class ConceptPathFinderBidirectionalImpl
//	implements ConceptPathFinder
//{
//	protected Graph dataSummary;
//	protected SparqlQueryConnection dataConnection;
//
//	public ConceptPathFinderBidirectionalImpl(Graph dataSummary, SparqlQueryConnection dataConnection) {
//		super();
//		this.dataSummary = dataSummary;
//		this.dataConnection = dataConnection;
//	}
//
//	@Override
//	public PathSearch<Path> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept) {
//		return new PathSearchBase<Path>() {
//			@Override
//			public Flowable<Path> exec() {
//				return ConceptPathFinderBidirectionalUtils
//					.findPaths(dataConnection, sourceConcept, targetConcept, maxResults, maxLength, dataSummary);
//			}
//		};
//	}
//}
