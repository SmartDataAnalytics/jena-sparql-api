package org.aksw.jena_sparql_api.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggDatasetGraph;
import org.aksw.jena_sparql_api.mapper.AggGraph;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.springframework.util.Assert;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * Takes a CONSTRUCT WHERE query (i.e. query pattern matches the template)
 * and a grouping variable and
 * transforms it into a mapped concept that yields a graph for each resource
 *
 * @author raven
 *
 */
public class QueryTransformConstructGroupedGraph {
	public static MappedConcept<DatasetGraph> query2(Query query, Var partitionVar) {
		Assert.isTrue(query.isConstructType());

		Template template = query.getConstructTemplate();
		BasicPattern bgp = template.getBGP();

		BasicPattern newBgp = allocVarsForBlankNodes(bgp);

		//org.aksw.jena_sparql_api.utils.VarUtils.
		// Allocate a fresh var for the graph
		Var g = Var.alloc("_g_");

		//Element e = new Element
		Element tmp = new ElementTriplesBlock(newBgp);
		Element newElement = new ElementNamedGraph(g, tmp);

		OpQuadPattern tmpOp = new OpQuadPattern(g, newBgp);
		QuadPattern quadPattern = tmpOp.getPattern();


		Concept concept = new Concept(newElement, partitionVar);
		Agg<DatasetGraph> agg = AggDatasetGraph.create(quadPattern);

		MappedConcept<DatasetGraph> result = MappedConcept.create(concept, agg);
		return result;

	}


	public static BasicPattern allocVarsForBlankNodes(BasicPattern bgp) {

		//NodeTransformBNodesToVariables nodeTransform = new NodeTransformBNodesToVariables();
		//BasicPattern newBgp = NodeTransformLib.transform(nodeTransform, bgp);
		NodeTransformCollectNodes collector = new NodeTransformCollectNodes();
		NodeTransformLib.transform(collector, bgp);

		Set<Node> nodes = collector.getNodes();
		Set<Var> vars = NodeUtils.getVarsMentioned(nodes);
		Set<Node> bnodes = NodeUtils.getBnodesMentioned(nodes);

		Generator<Var> gen = VarGeneratorBlacklist.create("v", vars);

		Map<Node, Var> map = new HashMap<Node, Var>();
		for(Node node : bnodes) {
			map.put(node, gen.next());
		}
		for(Var var : vars) {
			if(var.getName().startsWith("?")) {
				map.put(var, gen.next());
			}
			//System.out.println(var);
		}

		NodeTransformRenameMap nodeTransform = new NodeTransformRenameMap(map);

		BasicPattern result = NodeTransformLib.transform(nodeTransform, bgp);
		return result;
	}

	public static MappedConcept<Graph> query(Query query, Var partitionVar) {
		Assert.isTrue(query.isConstructType());

		Template template = query.getConstructTemplate();
		BasicPattern bgp = template.getBGP();

		BasicPattern newBgp = allocVarsForBlankNodes(bgp);

		//Element e = new Element
		Element newElement = new ElementTriplesBlock(newBgp);
		Template newTemplate = new Template(newBgp);


		Concept concept = new Concept(newElement, partitionVar);
		Agg<Graph> agg = AggGraph.create(newTemplate);

		MappedConcept<Graph> result = MappedConcept.create(concept, agg);
		return result;
	}
}