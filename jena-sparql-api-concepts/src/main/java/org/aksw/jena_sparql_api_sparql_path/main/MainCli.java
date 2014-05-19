package org.aksw.jena_sparql_api_sparql_path.main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Path;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Map1;

//class EdgeTransition
//	extends DefaultEdge
//{
//	public EdgeTransition() {
//	}
//}

/**
 * Just some idea:
 * The property matrix query might not run on DBpedia (we have to try out), BUT:
 * 
 * First, we can partition by graph.
 * Then, we can fetch all properties
 *     We could even use a partitioned approach for this step.
 *
 * Afterwards, we could take each individual property, and try to find all
 * successor properties of it, e.g.
 * 
 * Select Distinct ?y {
 *         { Select * { -- See Note [1]
 *     ?a ?x ?b .
 *     Filter(?x = <foobar>)
 *         } Limit 100000 Offset 10000 }
 *
 *     ?b ?y ?c .
 * }
 * 
 * [1] Note: we could do Distinct ?b, but it might not improve performance (much), as ?b will have few duplicates
 * 
 *
 *
 * @author raven
 *
 */
class PropertySummaryCreator {
	
}


public class MainCli {
	
	
	
	public static void main2(String[] args) throws IOException, SQLException {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://localhost:8810/sparql", "http://fp7-pp.publicdata.eu/");
				

		Concept sourceConcept = Concept.create("?s a <http://fp7-pp.publicdata.eu/ontology/Project>", "s");
		System.out.println(sourceConcept);

		Concept tmpTargetConcept = Concept.create("?s <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lon ; <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat", "s");
	
		ConceptPathFinder.findPaths(qef, sourceConcept, tmpTargetConcept, 10, 10);
	}

	public static void main(String[] args) throws IOException, SQLException {
	    String sparqlServiceIri = "http://fp7-pp.publicdata.eu/sparql";
	    //String sparqlServiceIri = "http://localhost:8810/sparql";
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(sparqlServiceIri);
				

		Concept sourceConcept = Concept.create("?s ?_p_ ?_o_", "s");
		System.out.println(sourceConcept);

		Concept tmpTargetConcept = Concept.create("?s <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lon ; <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat", "s");
	
		List<Path> paths = ConceptPathFinder.findPaths(qef, sourceConcept, tmpTargetConcept, 10, 10);
		System.out.println(paths);
	}

}


class Map1StatementToSubject
	implements Map1<Statement, Resource>
{
	@Override
	public Resource map1(Statement stmt) {
		return stmt.getSubject().asResource();
	}	
}

/*
 * Note: Dijkstra would only keep the shortest path to a node - but here we want all paths...
 * 
 * 
 * Schema:
 * context: The id of the path segement
 * in_inverse: whether this node was reached by forward or backward traversal
 * path_length: accumulated path length
 * 
 * cost: some meta data..., for example, how many forward / backwards traks were used
 * 
 * Path "Fact" table:
 * TODO I guess the segment_id is globally unique...
 * 
 * process_id | ant_id | segment_id | context_id | to_node_id | is_inverse | path_length | backward_step_count |
 *          X |      1 |          1 |       null |        foo |      false |           0 |                   0 |
 *                                2 |          1 |        bar |        baz |           1 |                   0 |
 *
 * Solution Cache:
 * We can track which paths were found between two nodes, but we also need a completeness level,
 * i.e. whether all paths of e.g. length 1, 2, 3, ... have been found.
 * If yes, then we can make full use of the cache, otherwise, we need to scan whether there
 * are paths which have not been checked.
 * This on the other hand means, that we could have to keep track of
 * - which paths have not been seen yet (because the iteration was not far enough yet)
 * - which paths have been skipped, e.g. because their cost estimate was too high.
 * 
 * 
 * 
 * Maintaining this minimum path matrix would require N^2 space...
 * With ~50000 properties on DBpedia, there is no point to pre-compute this, but only cache results
 * of on-demand computations.
 * 
 * 
 * A shortest path cache:
 * This could help estimating, whether 2 nodes are connected at all, and what would be their shortest route.
 * 
 * Somehow this begs the question of what pg_routing actually does...
 * Anyway, a pure Java solution is preferred...
 * 
 * Path "Cache" table:
 * start_node_id | end_node_id | 
 * 
 * 
 * Questions:
 * 
 * We could start caching paths between nodes after a certain length!
 * 
 * 
 * So, when we search for paths between nodes, we ask an "Edge-Provider" for all outgoing edges.
 * We could then keep track of whether the edge provider was done or not with the provisioning.
 *    -> state + done flag needed
 *    
 * 
 * 
 * 
 * How to figure out that two paths meet:
 * If one ant either reaches a goal node (to_node_id is a goal) or
 * to_node_id was reached by another ant
 * 
 * Problem with this approach:
 * We do not keep track of sub-solutions -> bad!
 * 
 *
 * Property adjacency retrieval:
 * This is in the general case some kind of partitioned query execution....
 * 
 */



