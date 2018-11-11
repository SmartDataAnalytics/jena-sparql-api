package org.aksw.jena_sparql_api.sparql_path.api;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public interface ConceptPathFinderFactory {
	ConceptPathFinderFactory setDataSummary(Graph dataSummary);
	ConceptPathFinderFactory setDataSummary(Model dataSummary);

	/**
	 * Returns the previously set data summary.
	 * In case a {@link Graph} has been set, use the returned model's .getGraph() method to obtain it
	 * 
	 * @return
	 */
	Model getDataSummary();
	
	ConceptPathFinderFactory setDataConnection(SparqlQueryConnection conn);

	SparqlQueryConnection getDataConnection();
	
	ConceptPathFinder create();
}
