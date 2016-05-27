package org.aksw.jena_sparql_api.views;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatesetDescription;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;


public class LDQ16Demonstrator implements Closeable {
	    
    /** query execution factory instance */
    QueryExecutionFactory qef;
    
    /** currently active sparql view */
    QueryExecutionFactorySparqlView currentSparqlView;
    
    /** map of all known SPARQL views in their order of registration */
    Map<String, CandidateViewSelector<SparqlView>> sparqlViews = new LinkedHashMap<>();
    
    Map<String, QueryExecutionFactorySparqlView> sparqlQueryExectionFactoryMap = new LinkedHashMap<>();

	
	public LDQ16Demonstrator(final String sparqlEndpoint, final String sourceGraphName) {
		this.qef = FluentQueryExecutionFactory
                .http(sparqlEndpoint)
                .config()
                    .withParser(SparqlQueryParserImpl.create())
                    .withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph(sourceGraphName))
                    .withQueryTransform(F_QueryTransformDatesetDescription.fn)
                    .withPagination(1000)
                .end()
                .create();
	}
	
	/**
	 * Add view to candidate view selector
	 * 
	 * @param viewName		 - viewName
	 * @param constructQuery - query
	 */
	public void addView(final String layerName, final String constructQuery) {
		if (null == layerName || null == constructQuery || false == constructQuery.toLowerCase().contains("construct")) {
			return;
		}
		
		CandidateViewSelector<SparqlView> candidateViewSelector = this.sparqlViews.get(layerName);
		if (null == candidateViewSelector) {
			candidateViewSelector = new CandidateViewSelectorSparqlView();
			this.sparqlViews.put(layerName, candidateViewSelector);
		}
		
		SparqlView sparqlViewGridId = SparqlView.create(layerName + candidateViewSelector.getViews().size(), constructQuery);
		candidateViewSelector.addView(sparqlViewGridId);
		
		QueryExecutionFactorySparqlView sparqlViewQueryExectionFactory = this.sparqlQueryExectionFactoryMap.get(layerName);
		if (null == sparqlViewQueryExectionFactory) {
			if (null == currentSparqlView) {
				sparqlViewQueryExectionFactory = new QueryExecutionFactorySparqlView(this.qef, candidateViewSelector, Dialect.VIRTUOSO);
			} else {
		        sparqlViewQueryExectionFactory = new QueryExecutionFactorySparqlView(this.currentSparqlView, candidateViewSelector, Dialect.VIRTUOSO);
			}
			
	        this.currentSparqlView = sparqlViewQueryExectionFactory;
	        this.sparqlQueryExectionFactoryMap.put(layerName, sparqlViewQueryExectionFactory);
		}
	}
	
	/**
	 * Executes the search query on the last query layer
	 * 
	 * @param searchQuery
	 * @return
	 */
	public QueryExecution executeQuery(final String searchQuery) {
		if (null == searchQuery) {
			return null;
		}
		
		return this.currentSparqlView.createQueryExecution(searchQuery);
	}
	
	/**
	 * Executes search query for given search query and passed in layer
	 * 
	 * @param layerName
	 * @param searchQuery
	 * @return
	 */
	public QueryExecution executeQuery(final String layerName, final String searchQuery) {
		if (null == layerName || null == searchQuery) {
			return null;
		}
		
		QueryExecutionFactorySparqlView sparqlViewQueryExectionFactory = this.sparqlQueryExectionFactoryMap.get(layerName);
		if (null == sparqlViewQueryExectionFactory) {
			return null;
		}
		
		return sparqlViewQueryExectionFactory.createQueryExecution(searchQuery);
	}
	

	@Override
	public void close() throws IOException {
		if (null != this.sparqlQueryExectionFactoryMap) {
			for (String layerName : this.sparqlQueryExectionFactoryMap.keySet()) {
				QueryExecutionFactorySparqlView queryExecutionFactory =
						this.sparqlQueryExectionFactoryMap.get(layerName);
				
				queryExecutionFactory.close();
			}
			
			this.sparqlQueryExectionFactoryMap.clear();
			this.sparqlQueryExectionFactoryMap = null;
		}
		
		if (null != this.sparqlViews) {
			this.sparqlViews.clear();
			this.sparqlViews = null;
		}
		
		if (null != this.qef) {
			this.qef.close();
			this.qef = null;
		}
	}

	
    
	static final String purlCreator = "http://purl.org/dc/terms/creator";	    	
	static final String admsIdentifierClass = "http://www.w3.org/ns/adms#Identifier";
	static final String admsIdentifierProperty = "http://www.w3.org/ns/adms#identifier";
	

    public static void main(String[] args) throws IOException {
    	
    	LDQ16Demonstrator demo = new LDQ16Demonstrator("http://localhost:9890/sparql", "http://dfki.gcd.source.de");
    
    	try {
	    	String queryLabelChange = "Construct { ?s <" + RDFS.label.getURI() + "> ?o } WHERE { ?s <http://grid.source.ac/name> ?o }";
	    	demo.addView("SourceMappingLayer", queryLabelChange);
	    	
	    	 String gridIdQueryChange = "prefix fn:<http://www.w3.org/2005/xpath-functions#> " +
					  "Construct {?s <" + admsIdentifierProperty + "> ?idInstance . " +
					  		"?idInstance a <" + admsIdentifierClass + "> . " +
					  		"?idInstance <" + purlCreator + "> \"https://grid.ac\" . " +
					  		"?idInstance <" + SKOS.notation.getURI() + "> ?o . } " +
					  		"WHERE { ?s <http://grid.source.ac/id> ?o " +
					  		"BIND( IRI(CONCAT(STR(?s),\"_gridId\")) AS ?idInstance ) }";
	     	demo.addView("SourceMappingLayer", gridIdQueryChange);
	     	
	     	String normalizeLabels = "Construct { ?s ?p ?o } WHERE { ?s ?p ?x . Bind(replace(?x, '[2-4]', 'I') As ?o) }";
	     	demo.addView("NormalizationLayer", normalizeLabels);
	     	
	     	QueryExecution queryExecution = demo.executeQuery("SELECT ?s ?o WHERE { ?s <" + SKOS.notation.getURI() + "> ?o . } LIMIT 10");
	     	ResultSet resultSet = queryExecution.execSelect();
	        if(true) {
	        	ResultSetFormatter.out(System.out, resultSet);
	        }
	        
	        QueryExecution queryExecution2 = demo.executeQuery("SourceMappingLayer", "SELECT ?s ?o WHERE { ?s <" + SKOS.notation.getURI() + "> ?o . } LIMIT 10");
	     	ResultSet resultSet2 = queryExecution2.execSelect();
	        if(true) {
	        	ResultSetFormatter.out(System.out, resultSet2);
	        }
    	} finally {
    		demo.close();
    	}
    }
}
