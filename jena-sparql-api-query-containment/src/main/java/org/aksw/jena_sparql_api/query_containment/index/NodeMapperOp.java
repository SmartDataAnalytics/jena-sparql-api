package org.aksw.jena_sparql_api.query_containment.index;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;

import com.google.common.collect.BiMap;

public interface NodeMapperOp
	extends NodeMapper<Op, Op, BiMap<Node, Node>, BiMap<Node, Node>, ResidualMatching>
	//extends TriFunction<Op, Op, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching>, Entry<BiMap<Node, Node>, ResidualMatching>>
{

}
