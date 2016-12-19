package org.aksw.jena_sparql_api.rdf_stream.enhanced;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

import com.google.common.collect.MutableClassToInstanceMap;

public class ImplementationEnh
	extends Implementation
{
	// Data structure for non-RDF information attached to nodes
	// It could be considered moving this field to (a subclass of) Model
	protected Map<Node, MutableClassToInstanceMap<Object>> meta;

	public ImplementationEnh() {
		this(new HashMap<>());
	}

	public ImplementationEnh(Map<Node, MutableClassToInstanceMap<Object>> meta) {
		super();
		this.meta = meta;
	}

	@Override
	public EnhNode wrap(Node node, EnhGraph eg) {
		ResourceEnh result = new ResourceEnh(node, eg, meta);
		return result;
    }

	@Override
	public boolean canWrap(Node node, EnhGraph eg) {
		return true;
	}
}
