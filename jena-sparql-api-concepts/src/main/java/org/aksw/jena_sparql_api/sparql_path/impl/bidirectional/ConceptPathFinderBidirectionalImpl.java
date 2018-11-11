package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchBase;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.Flowable;

/**
 * A concept path finder capable of finding paths in both directions.
 * 
 * 
 * 
 * @author Claus Stadler, Nov 11, 2018
 *
 */
public class ConceptPathFinderBidirectionalImpl
	implements ConceptPathFinder
{
	protected Graph dataSummary;
	protected SparqlQueryConnection dataConnection;

	public ConceptPathFinderBidirectionalImpl(Graph dataSummary, SparqlQueryConnection dataConnection) {
		super();
		this.dataSummary = dataSummary;
		this.dataConnection = dataConnection;
	}

	@Override
	public PathSearch<Path> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept) {
		return new PathSearchBase<Path>() {
			@Override
			public Flowable<Path> exec() {
				return ConceptPathFinderBidirectionalUtils
					.findPaths(dataConnection, sourceConcept, targetConcept, maxResults, maxLength, dataSummary);
			}
		};
	}
}
