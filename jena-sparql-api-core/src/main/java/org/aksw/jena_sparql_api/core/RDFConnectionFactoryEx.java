package org.aksw.jena_sparql_api.core;

import java.lang.reflect.Field;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

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
	
	public static final Symbol symConnection = Symbol.create("http://jsa.aksw.org/connection");

	
	public static RDFConnection wrapWithQueryParser(RDFConnection rawConn, Function<String, SparqlStmt> parser) {
		RDFConnection result =
			new RDFConnectionModular(rawConn, rawConn, rawConn) {
				@Override
				public QueryExecution query(String queryString) {
					SparqlStmt stmt = parser.apply(queryString);
					Query query = stmt.getAsQueryStmt().getQuery();
					QueryExecution result = query(query);
					return result;
				}

			
				@Override
				public void update(String updateString) {
					SparqlStmt stmt = parser.apply(updateString);
					UpdateRequest updateRequest = stmt.getAsUpdateStmt().getUpdateRequest();
					update(updateRequest);
					//QueryExecution result = query(query);
				}
			};

//					new SparqlQueryConnectionJsa(
//				FluentQueryExecutionFactory
//					.from(new QueryExecutionFactorySparqlQueryConnection(rawConn))
//					.config()
//						.withParser(parser)
//						.end()
//					.create()
//					), rawConn, rawConn);

		
		return result;
	}
	
	public static SparqlUpdateConnection  getUpdateConnection(RDFConnectionModular conn) {
		SparqlUpdateConnection result;
		try {
			Field f = RDFConnectionModular.class.getDeclaredField("updateConnection");
			f.setAccessible(true);
			result = (SparqlUpdateConnection)f.get(conn);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}
	
	public static Dataset getDataset(RDFConnectionLocal conn) {
		Dataset result;
		try {
			Field f = RDFConnectionLocal.class.getDeclaredField("dataset");
			f.setAccessible(true);
			result = (Dataset)f.get(conn);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	public static SparqlUpdateConnection unwrapUpdateConnection(SparqlUpdateConnection conn) {
		SparqlUpdateConnection result;
		if(conn instanceof RDFConnectionModular) {
			SparqlUpdateConnection tmp = getUpdateConnection((RDFConnectionModular)conn);
			result = unwrapUpdateConnection(tmp);
		} else {
			result = conn;
		}
		
		return result;
	}
	
	public static RDFConnection wrapWithContext(RDFConnection rawConn) {
		RDFConnection[] result = {null}; 

		SparqlUpdateConnection tmp = unwrapUpdateConnection(rawConn);
		Dataset dataset = tmp instanceof RDFConnectionLocal
				? getDataset((RDFConnectionLocal)tmp)
				: null;

		result[0] =
			new RDFConnectionModular(rawConn, rawConn, rawConn) {
				public QueryExecution query(Query query) {
					return postProcess(rawConn.query(query));
				}
				
				@Override
				public QueryExecution query(String queryString) {
					return postProcess(rawConn.query(queryString));
				}
				
				
				@Override
				public void update(UpdateRequest update) {
//			        checkOpen();
//			        Txn.executeWrite(dataset, () -> { 
			        	UpdateProcessor tmp = UpdateExecutionFactory.create(update, dataset);
			        	UpdateProcessor up = postProcess(tmp);
			        	up.execute();
//			        });
				}
				
				
				public UpdateProcessor postProcess(UpdateProcessor qe) {
					Context cxt = qe.getContext();
					if(cxt != null) {
						cxt.set(symConnection, result[0]);
					}
					
					return qe;
				}

				public QueryExecution postProcess(QueryExecution qe) {
					Context cxt = qe.getContext();
					if(cxt != null) {
						cxt.set(symConnection, result[0]);
					}
					
					return qe;
				}
			};
		
		return result[0];
	}

}
