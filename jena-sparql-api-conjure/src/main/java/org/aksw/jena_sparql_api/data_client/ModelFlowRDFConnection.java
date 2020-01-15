package org.aksw.jena_sparql_api.data_client;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.update.UpdateRequest;

public class ModelFlowRDFConnection
	implements ModelFlow
{
	protected RDFConnection conn;
	protected ModelFlowDriver driver;
	
	@Override
	public ModelFlow execUpdate(UpdateRequest updateRequest) {
		conn.update(updateRequest);
		return this;
	}

	@Override
	public ModelFlow execConstruct(Query query) {
		Model model = conn.queryConstruct(query);
		ModelFlow result = driver.connect(model);
		return result;
	}

	@Override
	public Model toModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Triple> toTriples() {
		// TODO Auto-generated method stub
		return null;
	}

}
