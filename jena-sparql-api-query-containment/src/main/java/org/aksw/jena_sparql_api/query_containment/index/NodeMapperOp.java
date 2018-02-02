package org.aksw.jena_sparql_api.query_containment.index;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

public interface NodeMapperOp
	extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, ResidualMatching>
	//extends TriFunction<Op, Op, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching>, Entry<BiMap<Node, Node>, ResidualMatching>>
{

}
