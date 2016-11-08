package org.aksw.jena_sparql_api.example.views;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.server.utils.SparqlServerUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatasetDescription;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.jena_sparql_api.views.CandidateViewSelectorSparqlView;
import org.aksw.jena_sparql_api.views.Dialect;
import org.aksw.jena_sparql_api.views.QueryExecutionFactorySparqlView;
import org.aksw.jena_sparql_api.views.SparqlView;
import org.apache.http.client.HttpClient;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetDescription;
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
                    .withQueryTransform(F_QueryTransformDatasetDescription.fn)
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

		System.out.println("Add view for layer '" + layerName + "': " + constructQuery);

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

	public void addMixedLayer(final String mixedLayerName, List<String> otherLayers, final String constructQuery) {
		if (null == mixedLayerName || null == constructQuery) {
			return;
		}

		System.out.println("Added mixed view for '" + otherLayers + "' with query: " + constructQuery);

		CandidateViewSelector<SparqlView> candidateViewSelector = this.sparqlViews.get(mixedLayerName);
		if (null == candidateViewSelector) {
			candidateViewSelector = new CandidateViewSelectorSparqlView();
			this.sparqlViews.put(mixedLayerName, candidateViewSelector);
		}

		for (String otherLayer : otherLayers) {
			CandidateViewSelector<SparqlView> viewSelector = this.sparqlViews.get(otherLayer);
			if (null == viewSelector) {
				continue;
			}

			for (SparqlView view : viewSelector.getViews()) {
				candidateViewSelector.addView(view);
			}
		}

		SparqlView sparqlViewBase = SparqlView.create(mixedLayerName + "0", constructQuery);
		candidateViewSelector.addView(sparqlViewBase);

		QueryExecutionFactorySparqlView sparqlViewQueryExectionFactory = this.sparqlQueryExectionFactoryMap.get(mixedLayerName);
		if (null == sparqlViewQueryExectionFactory) {
			sparqlViewQueryExectionFactory = new QueryExecutionFactorySparqlView(this.qef, candidateViewSelector, Dialect.VIRTUOSO);
	        this.sparqlQueryExectionFactoryMap.put(mixedLayerName, sparqlViewQueryExectionFactory);
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

		System.out.println("Execute query: \n" + searchQuery);

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

		System.out.println("Execute query in layer '" + layerName + "': \n" + searchQuery);

		QueryExecutionFactorySparqlView sparqlViewQueryExectionFactory = this.sparqlQueryExectionFactoryMap.get(layerName);
		if (null == sparqlViewQueryExectionFactory) {
			return null;
		}

		return sparqlViewQueryExectionFactory.createQueryExecution(searchQuery);
	}

	/**
	 * Start the server based on the top layer
	 *
	 * @param port port number of the server
	 */
	public void startServer(final short port) {
		if (0 > port) {
			return;
		}

        SparqlStmtParser sparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription,
                    HttpClient httpClient) {

                return new SparqlServiceImpl(currentSparqlView, null);
            }
        };

        System.out.println("Start Server for top layer on port: " + port);

        SparqlServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, port);
	}

	/**
	 * Start the server based on the passed in layer
	 *
	 * @param port port number of the server
	 */
	public void startServer(final String layerName, final short port) {
        if (0 > port || null == layerName || layerName.isEmpty()) {
        	return;
        }

        final QueryExecutionFactorySparqlView sparqlView = this.sparqlQueryExectionFactoryMap.get(layerName);
        if (null == sparqlView) {
        	return;
        }

		SparqlStmtParser sparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);

        SparqlServiceFactory ssf = new SparqlServiceFactory() {
            @Override
            public SparqlService createSparqlService(String serviceUri,
                    DatasetDescription datasetDescription,
                    HttpClient httpClient) {

                return new SparqlServiceImpl(sparqlView, null);
            }
        };

        System.out.println("Start Server for '" + layerName + "' on port: " + port);

        SparqlServerUtils.startSparqlEndpoint(ssf, sparqlStmtParser, port);
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


    public static void main(String[] args) throws IOException, InterruptedException {

    	LDQ16Demonstrator demo = new LDQ16Demonstrator("http://localhost:9890/sparql", "http://dfki.gcd.source.de");

    	try {
	    	String queryLabelChange = "Construct { ?s <" + SKOS.prefLabel.getURI() + "> ?o } WHERE { ?s <http://grid.source.ac/name> ?o }";
	    	demo.addView("SourceMappingLayer", queryLabelChange);

	    	 String gridIdQueryChange =
					  "Construct {?s <" + admsIdentifierProperty + "> ?idInstance . " +
					  		"?idInstance a <" + admsIdentifierClass + "> . " +
					  		"?idInstance <" + purlCreator + "> \"https://grid.ac\" . " +
					  		"?idInstance <" + SKOS.notation.getURI() + "> ?o . } " +
					  		"WHERE { ?s <http://grid.source.ac/id> ?o " +
					  		"BIND( IRI(CONCAT(STR(?s),\"_gridId\")) AS ?idInstance ) }";
	     	demo.addView("SourceMappingLayer", gridIdQueryChange);

	     	String normalizeLabels = "Construct { ?s ?p ?o } WHERE { ?s ?p ?x . Bind(replace(?x, '[aA]', '0') As ?o) }";
	     	demo.addView("NormalizationLayer", normalizeLabels);

	     	String mixedLayerQuery = "Construct { ?s ?p ?o . } WHERE { ?s ?p ?o. Filter(?p != <" + SKOS.prefLabel.getURI() + ">)}";

	     	demo.addMixedLayer("MixedLayer", Arrays.asList("SourceMappingLayer"), mixedLayerQuery);
	     	//demo.addView("MixedLayer", queryLabelChange);
	     	//demo.addView("MixedLayer", gridIdQueryChange);
	     	//demo.addView("MixedLayer", mixedLayerQuery);


	     	boolean useServer = true;
	     	if (useServer) {
		     	demo.startServer("SourceMappingLayer", (short) 7540);
		     	demo.startServer((short) 7550);
		     	demo.startServer("MixedLayer", (short) 7560);
	     	}

	     	//QueryExecution queryExecution = demo.executeQuery("SELECT DISTINCT * WHERE { ?s <" + SKOS.prefLabel.getURI() + "> ?o . } LIMIT 10");
	     	QueryExecution queryExecution = demo.executeQuery("SELECT DISTINCT * WHERE { ?s <" + SKOS.prefLabel.getURI() + "> ?o . } LIMIT 10");
	     	ResultSet resultSet = queryExecution.execSelect();
	        if(false == useServer) {
	        	ResultSetFormatter.out(System.out, resultSet);
	        }

	        QueryExecution queryExecution2 = demo.executeQuery("SourceMappingLayer", "SELECT ?s ?o WHERE { ?s <" + SKOS.prefLabel.getURI() + "> ?o . } LIMIT 10");
	        ResultSet resultSet2 = queryExecution2.execSelect();
	        if(false == useServer) {
	        	ResultSetFormatter.out(System.out, resultSet2);
	        }

	        if (useServer) {
		        System.out.println("Press SPACE to exit!");

		        char character;
		        do {
		        	character = (char) System.in.read();
		        } while (' ' != character);
		        System.out.print("That is all folks");
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		demo.close();
    		System.exit(0);
    	}
    }
}
