package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.function.BiPredicate;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.path.P_Path0;

public interface ConceptPathFinderFactory<T extends ConceptPathFinderFactory<T>> {
	T setDataSummary(Graph dataSummary);
	T setDataSummary(Model dataSummary);
	T setDataConnection(SparqlQueryConnection conn);

	// If null, use the system's default
	T setShortestPathsOnly(Boolean onOrOff);
	T setSimplePathsOnly(Boolean onOrOff);

	
	/**
	 * Returns the previously set data summary.
	 * In case a {@link Graph} has been set, use the returned model's .getGraph() method to obtain it
	 * 
	 * @return
	 */
	Model getDataSummary();
	SparqlQueryConnection getDataConnection();	
	ConceptPathFinder build();
	Boolean getShortestPathsOnly();
	Boolean getSimplePathsOnly();
	
	T addPathValidator(BiPredicate<? super SimplePath, ? super P_Path0> validator);
}
