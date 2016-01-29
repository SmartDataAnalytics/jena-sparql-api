package org.aksw.jena_sparql_api.utils;

import org.apache.jena.atlas.lib.Sink;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.ModelUtils;


/**
 * @author Claus Stadler
 *         Date: 5/16/12
 *         Time: 9:41 PM
 */
public class SinkModel
	implements Sink<Triple>
{
	private Model model;

	public SinkModel() {
		this.model = ModelFactory.createDefaultModel();
	}

	public SinkModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void send(Triple triple) {
		Statement stmt = ModelUtils.tripleToStatement(model, triple);
		model.add(stmt);
	}

}