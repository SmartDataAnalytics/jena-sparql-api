package org.aksw.jena_sparql_api.update;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.TableFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class QuadContainmentUtils {

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterable<Quad> iterable) {
        Set<Quad> result = checkContainment(qef, iterable.iterator());
        return result;
    }

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterator<Quad> it) {
        Map<Node, Graph> nodeToGraph = QuadPatternUtils.indexAsGraphs(it);
        Set<Quad> result = checkContainment(qef, nodeToGraph);
        return result;
    }

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Map<Node, Graph> nodeToGraph) {
        Set<Quad> result = new HashSet<Quad>();

        Query query = QuadContainmentUtils.createQueryCheckExistenceValues(nodeToGraph);
        System.out.println("Containment Check Query: " + query);
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node g = binding.get(Vars.g);
            Node s = binding.get(Vars.s);
            Node p = binding.get(Vars.p);
            Node o = binding.get(Vars.o);

            Quad quad = new Quad(g, s, p, o);
            result.add(quad);
        }

        return result;
    }

    public static ElementData tableToElement(Table table) {
        ElementData result = new ElementData();
        result.add(Vars.g);
        result.add(Vars.s);
        result.add(Vars.p);
        result.add(Vars.o);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding binding = it.next();
            result.add(binding);
        }

        return result;
    }

    public static Query createQueryCheckExistenceValues(Map<Node, Graph> nodeToGraph) {
        Table table = createTableForQuads(nodeToGraph);
        Element elData = tableToElement(table);

        ElementTriplesBlock elTriples = new ElementTriplesBlock();
        elTriples.addTriple(new Triple(Vars.s, Vars.p, Vars.o));

        ElementGroup elGroup = new ElementGroup();
        elGroup.addElement(new ElementNamedGraph(Vars.g, elTriples));
        elGroup.addElement(elData);

        Query result = new Query();
        result.setQuerySelectType();

        result.getProject().add(Vars.g);
        result.getProject().add(Vars.s);
        result.getProject().add(Vars.p);
        result.getProject().add(Vars.o);

        result.setQueryPattern(elGroup);

        return result;
    }

    public static Table createTableForQuads(Map<Node, Graph> nodeToGraph) {
        Iterator<Quad> it = IteratorQuadsFromNodeToGraph.create(nodeToGraph);
        Table result = createTableForQuads(it);
        return result;
    }

    public static Table createTableForQuads(Iterator<Quad> it) {
        List<Var> vars = Arrays.asList(Vars.g, Vars.s, Vars.p, Vars.o);
        Table result = TableFactory.create(vars);

        while(it.hasNext()) {
            Quad quad = it.next();
            BindingMap binding = new BindingHashMap();

            Node g = quad.getGraph();

            if(g != null && !g.equals(Quad.defaultGraphIRI)) {
                binding.add(Vars.g, g);
            }

            binding.add(Vars.s, quad.getSubject());
            binding.add(Vars.p, quad.getPredicate());
            binding.add(Vars.o, quad.getObject());

            result.addBinding(binding);
        }

        return result;
    }


    /*
    public static Map<Node, Graph> checkExistence(QuadContainmentChecker gec, QueryExecutionFactory qef, Iterable<Quad> quads) {
        Map<Node, Graph> nodeToGraph = QuadPatternUtils.indexAsGraphs(quads);
        Map<Node, Graph> result = gec.contains(nodeToGraph);
        return result;
    }
    */
}
