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
	extends QueryContainmentIndexImpl<K, OpContext, OpGraph, org.jgrapht.Graph<Node, Triple>, Node, Var, Op, R, SparqlTreeMapping<R>>
	implements SparqlQueryContainmentIndex<K, R>
{

	public SparqlQueryContainmentIndexImpl(
			SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index,
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory) {

		super(
			OpContext::create,
			OpContext::getNormalizedOp,
			OpContext::getLeafOpGraphs,
	        OpContext::getNormalizedOpTree,
	        
	        OpGraph::getJGraphTGraph,
	
	        index,
	
	        QueryContainmentIndexImpl::retainVarMappingsOnlyAsVars,
	        QueryContainmentIndexImpl::toNodes,
	        
	        nodeMapperFactory,
	        (TreeMappingFactory<Op, Op, BiMap<Var, Var>, R, SparqlTreeMapping<R>>)SparqlTreeMapping<R>::new
			);

//        TreeMappingFactory<Op, Op, BiMap<Var, Var>, R, TreeMapping<Op, Op, BiMap<Var, Var>, R>> sparqlTreeMappingFactory = SparqlTreeMapping<R>::new;

	}

	public static <K> SparqlQueryContainmentIndex<K, ResidualMatching> create() {
		SparqlQueryContainmentIndex<K, ResidualMatching> result =  create(NodeMapperOpContainment::new);
		return result;
	}

	public static <K, R> SparqlQueryContainmentIndex<K, R> create(
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory
	) {
        SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index = ExpressionMapper.createIndex(false);

        SparqlQueryContainmentIndex<K, R> result = create(index, nodeMapperFactory);
        return result;
	}

	public static <K, R> SparqlQueryContainmentIndex<K, R> create(
			SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index,
			TriFunction<? super OpContext, ? super OpContext, ? super Table<Op, Op, BiMap<Node, Node>>, ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory
	) {
		SparqlQueryContainmentIndex<K, R> result =  new SparqlQueryContainmentIndexImpl<>(index, nodeMapperFactory);
		return result;
	}
}

