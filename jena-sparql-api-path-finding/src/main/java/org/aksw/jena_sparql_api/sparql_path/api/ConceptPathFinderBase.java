package org.aksw.jena_sparql_api.sparql_path.api;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public abstract class ConceptPathFinderBase
	implements ConceptPathFinder
{
	protected Graph dataSummary;
	protected SparqlQueryConnection dataConnection;

	public ConceptPathFinderBase(Graph dataSummary, SparqlQueryConnection dataConnection) {
		super();
		this.dataSummary = dataSummary;
		this.dataConnection = dataConnection;
	}
}
