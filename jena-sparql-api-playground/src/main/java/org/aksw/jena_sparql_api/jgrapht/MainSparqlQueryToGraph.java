package org.aksw.jena_sparql_api.jgrapht;

import java.util.Map;

import javax.swing.JFrame;

import org.aksw.jena_sparql_api.jgrapht.transform.QueryToGraphVisitor;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.jgraph.JGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class MainSparqlQueryToGraph {

    public static void main(String[] args) {
        Op op = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("Select * { ?s a ?o , ?x }")));
        System.out.println(op);


        QueryToGraphVisitor visitor = new QueryToGraphVisitor();
        op.visit(visitor);
        Graph<?, ?> graph = visitor.getGraph();

        System.out.println(graph);

        visualizeGraph(graph);
    }

    public static void visualizeGraph(Graph<?, ?> graph) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        JGraph jgraph = new JGraph(new JGraphModelAdapter(graph));
        jgraph.setScale(2);
        final JGraphLayout hir = new JGraphHierarchicalLayout();
        // final JGraphLayout hir = new JGraphSelfOrganizingOrganicLayout();

        final JGraphFacade graphFacade = new JGraphFacade(jgraph);
        hir.run(graphFacade);
        final Map nestedMap = graphFacade.createNestedMap(true, true);
        jgraph.getGraphLayoutCache().edit(nestedMap);

        frame.getContentPane().add(jgraph);
        frame.setVisible(true);
    }

}
