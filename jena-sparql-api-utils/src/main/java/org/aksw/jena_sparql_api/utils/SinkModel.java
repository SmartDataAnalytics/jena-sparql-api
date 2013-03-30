package org.aksw.jena_sparql_api.utils;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.util.ModelUtils;
import org.openjena.atlas.lib.Sink;


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