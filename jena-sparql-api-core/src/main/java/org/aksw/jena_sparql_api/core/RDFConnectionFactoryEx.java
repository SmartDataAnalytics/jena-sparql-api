package org.aksw.jena_sparql_api.core;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class RDFConnectionFactoryEx {

    // TODO Consider move to a better place - e.g. RDFConnectionFactoryEx
	public static RDFConnectionEx connect(String serviceUrl, DatasetDescription datasetDescription) {
		SparqlServiceReference ssr = new SparqlServiceReference(serviceUrl, datasetDescription);
		RDFConnectionEx result = connect(ssr);
		return result;
	}
	
    public static RDFConnectionEx connect(SparqlServiceReference ssr) {
    	String serviceUrl = ssr.getServiceURL();
		RDFConnection rawConn = RDFConnectionFactory.connect(serviceUrl);
		DatasetDescription datasetDescription = ssr.getDatasetDescription();
		RDFConnection core = wrapWithDatasetAndXmlContentType(rawConn, datasetDescription);

		RDFConnectionMetaData metadata = ModelFactory.createDefaultModel()
				.createResource().as(RDFConnectionMetaData.class);
		
		metadata.setServiceURL(ssr.getServiceURL());
		metadata.getDefaultGraphs().addAll(ssr.getDefaultGraphURIs());
		metadata.getNamedGraphs().addAll(ssr.getNamedGraphURIs());
		
		RDFConnectionEx result = new RDFConnectionExImpl(core, metadata);

		return result;
	}

    /**
     * Wrap a connection with one that provides metadata.
     * If the given metadata is null, an empty blank node will be created.
     * 
     * @param rawConn
     * @param metadata
     * @return
     */
	public static RDFConnectionEx wrap(RDFConnection rawConn, Resource metadata) {
		if(metadata == null) {
			metadata = ModelFactory.createDefaultModel().createResource();
		}

		RDFConnectionMetaData md = metadata.as(RDFConnectionMetaData.class);
		
		RDFConnectionEx result = new RDFConnectionExImpl(rawConn, md);
		return result;
	}

	
	public static RDFConnection wrapWithQueryTransform(RDFConnection conn, Function<? super Query, ? extends Query> fn) {
		RDFConnection result =
				new RDFConnectionModular(new SparqlQueryConnectionJsa(
						FluentQueryExecutionFactory
							.from(new QueryExecutionFactorySparqlQueryConnection(conn))
							.config()
								.withQueryTransform(fn)
								.end()
							.create()
							), conn, conn);
	
		return result;
	}

	public static RDFConnection wrapWithDatasetAndXmlContentType(RDFConnection rawConn, DatasetDescription datasetDescription) {
		RDFConnection result =
				new RDFConnectionModular(new SparqlQueryConnectionJsa(
						FluentQueryExecutionFactory
							.from(new QueryExecutionFactorySparqlQueryConnection(rawConn))
							.config()
								.withClientSideConstruct()
								.withDatasetDescription(datasetDescription)
								.withPostProcessor(qe -> {
									if(qe instanceof QueryEngineHTTP) {
										QueryEngineHTTP qeh = (QueryEngineHTTP)qe;
										qeh.setSelectContentType(WebContent.contentTypeResultsXML);
										qeh.setModelContentType(WebContent.contentTypeNTriples);
										qeh.setDatasetContentType(WebContent.contentTypeNQuads);
									}
								})
								.end()
							.create()
							), rawConn, rawConn);

		
		return result;
	}
}
