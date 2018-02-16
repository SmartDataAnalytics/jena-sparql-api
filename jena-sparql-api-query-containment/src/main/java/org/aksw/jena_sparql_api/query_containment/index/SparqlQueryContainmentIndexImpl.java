package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map.Entry;

import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.jgrapht.Graph;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.Table;


public class SparqlQueryContainmentIndexImpl<K, R>
	extends QueryContainmentIndexImpl<K, OpContext, OpGraph, org.jgrapht.Graph<Node, Triple>, Node, Var, Op, R>
	implements SparqlQueryContainmentIndex<K, R>
{

	public SparqlQueryContainmentIndexImpl(
			SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index,
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory) {

		super(
			OpContext::create,
			OpContext::getLeafOpGraphs,
	        OpContext::getNormalizedOpTree,
	        
	        OpGraph::getJGraphTGraph,
	
	        index,
	
	        QueryContainmentIndexImpl::retainVarMappingsOnlyAsVars,
	        QueryContainmentIndexImpl::toNodes,
	        
	        nodeMapperFactory);
	}

	public static <K> SparqlQueryContainmentIndex<K, ResidualMatching> create() {
		return create(NodeMapperOpContainment::new);
	}


	public static <K, R> SparqlQueryContainmentIndex<K, R> create(
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory
	) {
        SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index = ExpressionMapper.createIndex(false);

		return create(index, nodeMapperFactory);
	}

	public static <K, R> SparqlQueryContainmentIndex<K, R> create(
			SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index,
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory
	) {
		return new SparqlQueryContainmentIndexImpl<>(index, nodeMapperFactory);
	}
}

