/*
 * Copyright (C) INRIA, 2012-2013
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.tyrexmo.queryanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.Subgraph;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.core.Var;

public class CycleAnalysis {

    DirectedGraph<String, DefaultEdge> queryGraph;
    //UndirectedGraph<String, DefaultEdge> g1;
    CycleDetector<String,DefaultEdge> detector;
    Set<String> tripleNodeNames;
    Set<String> constantsAndDvars;

    CycleAnalysis () {
	queryGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	//g1 = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
	tripleNodeNames = new HashSet<String>();
	constantsAndDvars = new HashSet<String>();
    };

    public CycleAnalysis ( List<Triple> triples ) {
	this();
    	createGraph(triples);
	detector = new CycleDetector<String,DefaultEdge>( queryGraph );
    }

    private void createGraph( List<Triple> triples ) {
    	for( Triple t : triples ) reification(t);
    }

    /**
     * Creates a directed graph based on URL objects that represents link
     * structure.
     *
     * @return a graph based on String objects (URIs, Vars, Literals).
     */

    private void reification( Triple t ) {
    	String tripleNode = getUniqueTripleNodeName();
    	tripleNodeNames.add(tripleNode);
        String subject = t.getSubject().toString(); //t.getSubject().getName();
        String predicate = t.getPredicate().toString(); //t.getPredicate().getLocalName();
        String object = t.getObject().toString();//t.getObject().getName();

        if (t.getSubject().isURI() ||  t.getSubject().isBlank())
	    constantsAndDvars.add(subject);
        if (t.getPredicate().isURI())
	    constantsAndDvars.add(predicate);
        if (t.getObject().isURI() || t.getObject().isLiteral() || t.getObject().isBlank())
	    constantsAndDvars.add(object);

        // add the vertices
        queryGraph.addVertex(tripleNode);
        queryGraph.addVertex(subject);
        queryGraph.addVertex(predicate);
        queryGraph.addVertex(object);

        // add edges to create linking structure
        queryGraph.addEdge(subject, tripleNode);
        queryGraph.addEdge(tripleNode, predicate);
        queryGraph.addEdge(tripleNode, object);

	//System.out.println("["+subject+" "+ predicate + " " + object+"]");
    }

    /* Tests if a DAG is a tree */
    public boolean isDAGATree() {
    	for (DefaultEdge edges1 : queryGraph.edgeSet()) {
	    for (DefaultEdge edges2 : queryGraph.edgeSet()) {
		if (!edges1.equals(edges2)) {
		    if ( queryGraph.getEdgeTarget(edges1).equals(queryGraph.getEdgeTarget(edges2)))
			return false;
		}
	    }
    	}
    	return true;
    }

    /**
     * Generate random names for triple nodes
     * @param varName
     * @return
     */
    private static int newVarRank = 0;

    private String getUniqueTripleNodeName() {
    	return "xxx"+(newVarRank++);
    }

    private boolean detectCycle( String varName ) {
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(queryGraph);
    	if( !queryGraph.containsVertex(varName) ) {
	    System.err.println("Vertex does not exist in the graph");
	    return false;
    	}
    	return detector.detectCyclesContainingVertex(varName);
    }

    /**
     * Detect cycles among non-distinguished variables
     * @param ndvars : all the non-distinugished variables in the query
     * @return true or false
     */
    public boolean isCycle( Collection<Var> ndvars ) {
    	boolean cycle = false;
    	if ( !ndvars.isEmpty() ) {
	    for ( Var v : ndvars ) {
		if ( detectCycle( v.toString() ) ) {
		    cycle = true;
		    break;
		}
	    }
    	} else cycle = false;
    	return cycle;
    }

    public boolean isCyclic() {
    	boolean cycle = false;
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(queryGraph);
    	cycle = detector.detectCycles();
    	return cycle;
    }

    public Set<String> convertFromVarToString (Collection<Var> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (Var var : ndvars) {
	    vars.add(var.toString());
    	}
    	return vars;
    }

    public Set<String> convertFromVarToString (List<Var> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (Var var : ndvars) {
	    vars.add(var.toString());
    	}
    	return vars;
    }

    public Set<String> convertListToString (List<String> ndvars) {
    	Set<String> vars = new HashSet<String> ();
    	for (String var : ndvars) {
	    vars.add(var);
    	}
    	return vars;
    }

    public boolean isThereAcycleAmongNDvars(Collection<Var> ndvars) {
    	boolean cycle = false;
    	CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>( queryGraph );
    	if (detectCyclesContainingNDvars(ndvars) && !ndvars.isEmpty()) {
	    if ( detectCyclesContainingConstantsAndDvars() )
		cycle = false;
	    else {
        	//cycle = detector.detectCycles();
    		Set<String> varsAppearingInAcycle = detector.findCycles();

		//    		Subgraph<String, ?, DirectedGraph<String, DefaultEdge>> sg = new Subgraph (queryGraph,detector.findCycles(), queryGraph.edgeSet());
		//    		System.out.println( sg.edgeSet()); //queryGraph.edgeSet());
    		Set<DefaultEdge> edges = new HashSet<DefaultEdge> ();
    		for (String vertex : varsAppearingInAcycle) {
		    edges.addAll(queryGraph.edgesOf(vertex));
    		}
    		//System.out.println(edges);
    		// edges is a the cyclic component
    		// vertexes involved in the cyclic component
    		Set<String> vertex = new HashSet<String> ();
    		for (DefaultEdge e : edges) {
		    vertex.add(queryGraph.getEdgeSource(e));
		    vertex.add(queryGraph.getEdgeTarget(e));
    		}
    		//System.out.println(vertex);
    		cycle = !cycleContainsAConstantOrADvar(vertex);

	    }
	} else cycle = false;
    	return cycle;
    }

    private boolean detectCyclesContainingNDvars(Collection<Var> ndvars) {
    	boolean cycle = false;
    	if (ndvars.isEmpty())
    		cycle = false;
    	else {
    		for (Var var: ndvars) {
    			CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(queryGraph);
    			if (detector.detectCyclesContainingVertex(var.toString())) {
    				cycle = true;
    				break;
    			}
    		}
    	}
    	return cycle;
    }

    // check if all the cycles involve constants
    private boolean detectCyclesContainingConstantsAndDvars() {
    	boolean cycle = false;
    	if (constantsAndDvars.isEmpty())
    		cycle = false;
    	else {
    		for (String var: constantsAndDvars) {
    			CycleDetector<String,DefaultEdge> detector = new CycleDetector<String,DefaultEdge>(queryGraph);
    			if (detector.detectCyclesContainingVertex(var.toString())) {
    				cycle = true;
    				break;
    			}
    		}
    	}
    	return cycle;
    }
    //cyclic component contains a constant or a dvar
    private boolean cycleContainsAConstantOrADvar(Set<String> vertex) {
    	boolean contains = false;
    	if (constantsAndDvars.isEmpty())
    		contains = false;
    	else {
    		for (String var: constantsAndDvars) {
    			if (vertex.contains(var)) {
    				contains = true;
    				break;
    			}
    		}
    	}
    	return contains;
    }

    public boolean projectionCount ( String query ) {
    	Query q = QueryFactory.create( query );
    	return q.isQueryResultStar();
    }

    /*
    public boolean usesProjection ( TransformAlgebra ta ) {
	return ( ta.getNonDistVars().size() != 0 );
    }

    // getNonDistVars()
    // ta.getResultVars() ++ ta.getProjectVars()
    //    isSelectType() && (isQueryResultStar || getProjectVars().len == )
    // || isAskType && public
    // There is also getProjectVars() returning variables
    // List<String> getResultVars().

    // JE: This is useless, this is only dependent on itself!
    private static void projNoProjTest(String dir) throws IOException {
    	String missingPrefixes = "PREFIX geo: <http://www.example.com/>   PREFIX foaf: <http://xmlns.com/foaf/0.1/>";
    	int cProj = 0, cNoProj = 0, syntaxerror = 0;
    	int dagProj = 0, dagNoProj = 0;
    	int treeProj = 0, treeNoProj = 0;
    	int acyclic = 0, cyclic = 0;
    	int dag = 0, tree = 0;

    	File [] queries = getAllFiles (dir);
    	for (int i = 0; i < queries.length; i++) {
	    String fname = queries[i].getAbsolutePath();
	    String query = new ReadQuery().read(fname);
	    query = missingPrefixes + query;
	    try {
		TransformAlgebra tf = new TransformAlgebra(query);
		AcyclicnessTester cq = new AcyclicnessTester(tf.getTriples());
		if (cq.isCyclic()) {
		    cyclic++;
		    if (cq.projectionCount(query))
                	cNoProj++;
		    else {
                	cProj++;
		    }
		}
		else {
		    acyclic++;
		    if (cq.checkIfaDAGisTree()){
            		dag++;
            		if (cq.projectionCount(query))
			    dagNoProj++;
			else {
			    dagProj++;
			}
		    }
		    else {
            		tree++;
            		if (cq.projectionCount(query))
			    treeNoProj++;
			else {
			    treeProj++;
			}
		    }
		}
	    }  catch (Exception e) {
		syntaxerror++;continue;
	    }
    	}
    	System.out.println("Acyclics = "+ acyclic + "    Cyclics=" + cyclic);
    	System.out.println("DAG = "+ dag + "    Tree=" + tree);
    	System.out.println("Cyclic stars = "+ cNoProj + "    Cyclic proj = " + cProj + "  Syntax error = " + syntaxerror);
    	System.out.println("DAG stars = "+ dagNoProj + "     DAG proj = " + dagProj + "  Syntax error = " + syntaxerror);
    	System.out.println("Tree stars = "+ treeNoProj + "    Tree proj = " + treeProj + "  Syntax error = " + syntaxerror);
    }
    */

}