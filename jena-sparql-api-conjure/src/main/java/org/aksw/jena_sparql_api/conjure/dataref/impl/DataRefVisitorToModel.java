package org.aksw.jena_sparql_api.conjure.dataref.impl;

import org.aksw.jena_sparql_api.conjure.dataref.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromEntity;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromUrl;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromRDFConnection;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefFromRemoteSparqlDataset;
import org.aksw.jena_sparql_api.conjure.dataref.api.DataRefVisitor;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntity;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * Simple DataRef visitor implementation that attempts to yield a {@link Model} for
 * any kind of reference
 * 
 * @author raven
 *
 */
public class DataRefVisitorToModel
	implements DataRefVisitor<Model>
{
	@Override
	public Model visit(DataRefFromUrl dataRef) {
		String url = dataRef.getUrl();
		Model result = RDFDataMgr.loadModel(url);
		return result;
	}

	@Override
	public Model visit(DataRefFromEntity dataRef) {
		RdfHttpEntity entity = dataRef.getEntity();
		
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public Model visit(DataRefFromRemoteSparqlDataset dataRef) {
		String url = dataRef.getUrl();
		DatasetDescription dd = dataRef.getDatsetDescription();
		
		Model result;
		try(RDFConnection conn = RDFConnectionRemote.create()
			.destination(url)
			.build()) {
			
			// TODO Take dataset description into accounut
			
			DataRef tmp = DataRefFromRDFConnectionImpl.create(conn);
			result = tmp.accept(this);
		}
		
		return result;
	}

	@Override
	public Model visit(DataRefFromRDFConnection dataRef) {
		RDFConnection conn = dataRef.getConnection();
		Model result = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
		return result;
	}

	@Override
	public Model visit(DataRefExt dataRef) {
		throw new RuntimeException("Extension method not overridden");
	}
}
