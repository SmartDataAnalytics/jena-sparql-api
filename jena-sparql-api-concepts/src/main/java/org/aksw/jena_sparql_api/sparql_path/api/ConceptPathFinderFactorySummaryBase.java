package org.aksw.jena_sparql_api.sparql_path.api;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

/**
 * Abstract base class for concept path finders that use
 * an data summary graph (typically in-memory) and a sparql connection to the actual data.
 * 
 * @author Claus Stadler, Nov 11, 2018
 *
 */
public abstract class ConceptPathFinderFactorySummaryBase
	implements ConceptPathFinderFactory
{
	protected Model dataSummary;
	protected SparqlQueryConnection dataConnection;
	
	@Override
	public ConceptPathFinderFactory setDataSummary(Graph dataSummary) {
		this.dataSummary = ModelFactory.createModelForGraph(dataSummary);
		return this;
	}

	@Override
	public ConceptPathFinderFactory setDataSummary(Model dataSummary) {
		this.dataSummary = dataSummary;
		return this;
	}

	@Override
	public ConceptPathFinderFactory setDataConnection(SparqlQueryConnection dataConnection) {
		this.dataConnection = dataConnection;
		return this;
	}

	@Override
	public Model getDataSummary() {
		return dataSummary;
	}

	@Override
	public SparqlQueryConnection getDataConnection() {
		return dataConnection;
	}

}
