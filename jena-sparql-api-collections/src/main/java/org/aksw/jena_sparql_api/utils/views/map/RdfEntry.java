package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class RdfEntry
	extends ResourceImpl
	implements Entry<RDFNode, RDFNode>
{
	// Note: These properties could be read from a map resource
	protected Property keyProperty;
	protected Property valueProperty;

	public RdfEntry(Node n, EnhGraph g, Property keyProperty, Property valueProperty) {
		super(n, g);
		this.keyProperty = keyProperty;
		this.valueProperty = valueProperty;
	}

	@Override
	public RDFNode getKey() {
		RDFNode result = ResourceUtils.getPropertyValue(this, keyProperty);
		return result;
	}

	@Override
	public RDFNode getValue() {
		RDFNode result = ResourceUtils.getPropertyValue(this, valueProperty);
		return result;
	}

	@Override
	public RDFNode setValue(RDFNode value) {
		RDFNode result = getValue();
		ResourceUtils.setProperty(this, valueProperty, value);
		return result;
	}

	public void clear() {
		this.removeAll(valueProperty);
		this.removeAll(keyProperty);
	}
}
