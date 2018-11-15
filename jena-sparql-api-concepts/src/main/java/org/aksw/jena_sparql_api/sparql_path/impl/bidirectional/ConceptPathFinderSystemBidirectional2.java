package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactorySummaryBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchBase;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class ConceptPathFinderSystemBidirectional2
	//extends ConceptPathFinderFactorySummaryBase
	implements ConceptPathFinderSystem
{
	

	@Override
	public Single<Model> computeDataSummary(SparqlQueryConnection dataConnection) {
		return ConceptPathFinderBidirectionalUtils.createDefaultDataSummary(dataConnection);
	}

	@Override
	public ConceptPathFinderFactoryBidirectional<?> newPathFinderBuilder() {
		return new ConceptPathFinderFactoryBidirectional<>();
	}
	
	
	
	public static class ConceptPathFinderFactoryBidirectional<T extends ConceptPathFinderFactoryBidirectional<T>>
		extends ConceptPathFinderFactorySummaryBase<T>
	{
		
		// NOTE We could add more specific attributes here if we wanted

		@Override
		public ConceptPathFinder build() {
			return new ConceptPathFinderBase(dataSummary.getGraph(), dataConnection) {

				@Override
				public PathSearch<SimplePath> createSearch(UnaryRelation sourceConcept, UnaryRelation targetConcept) {
					return new PathSearchBase<SimplePath>() {
						@Override
						public Flowable<SimplePath> exec() {
							return ConceptPathFinderBidirectionalUtils
								.findPaths(dataConnection, sourceConcept, targetConcept, maxResults, maxLength, dataSummary, shortestPathsOnly, simplePathsOnly);
						}
					};
				}					
			};
		}
	}
}
