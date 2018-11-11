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
public abstract class ConceptPathFinderFactorySummaryBase<T extends ConceptPathFinderFactorySummaryBase<T>>
	implements ConceptPathFinderFactory<T>
{
	protected Model dataSummary;
	protected SparqlQueryConnection dataConnection;

	// These flags are so general, it probably makes sense to add them here
	// We can add traits on the concept path finder system level, whether implementations actually make use of these flags
	protected Boolean shortestPathsOnly;
	
	/**
	 * Shortest paths are always simple paths - so if shortestPathsOnly is enabled, this attribute
	 * has no effect
	 */
	protected Boolean simplePathsOnly;

	@Override
	public T setDataSummary(Graph dataSummary) {
		this.dataSummary = ModelFactory.createModelForGraph(dataSummary);
		return (T)this;
	}

	@Override
	public T setDataSummary(Model dataSummary) {
		this.dataSummary = dataSummary;
		return (T)this;
	}

	@Override
	public T setDataConnection(SparqlQueryConnection dataConnection) {
		this.dataConnection = dataConnection;
		return (T)this;
	}
	
	public T setShortestPathsOnly(Boolean onOrOff) {
		this.shortestPathsOnly = onOrOff;
		return (T)this;
	}

	public T setSimplePathsOnly(Boolean onOrOff) {
		this.simplePathsOnly = onOrOff;
		return (T)this;
	}


	@Override
	public Model getDataSummary() {
		return dataSummary;
	}

	@Override
	public SparqlQueryConnection getDataConnection() {
		return dataConnection;
	}
	
	@Override
	public Boolean getShortestPathsOnly() {
		return shortestPathsOnly;
	}
	
	@Override
	public Boolean getSimplePathsOnly() {
		return simplePathsOnly;
	}

}
