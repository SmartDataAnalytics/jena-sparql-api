package org.aksw.jena_sparql_api.concepts;

import org.apache.jena.graph.Node;

import com.github.andrewoma.dexx.collection.Map;

// Idea for semantic expression fragments -
// instead of variables, placeholders are denoted by arbitary nodes
// Then again, do we really need node-based and variable-based versions?
// Probably yes, as these are 2 layers - variables are low level, whereas 'semnatic' nodes
// are high level and objects making use of semantic nodes can be resolved to the lower level objects.
public interface SemExprFragment {
	ExprFragment resolve(Map<Node, Node> binding);
}
