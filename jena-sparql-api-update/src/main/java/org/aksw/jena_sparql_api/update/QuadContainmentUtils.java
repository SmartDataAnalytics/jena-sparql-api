package org.aksw.jena_sparql_api.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.Vars;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

public class QuadContainmentUtils {

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterable<Quad> iterable) {
        Set<Quad> result = checkContainment(qef, iterable.iterator());
        return result;
    }

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterator<Quad> it) {

        Set<Quad> result = new HashSet<Quad>();

        Query query = createQueryCheckExistenceValues(it);
        System.out.println("Containment Check Query: " + query);
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node g = binding.get(Vars.g);
            if(g == null) {
                g = Quad.defaultGraphNodeGenerated;
            }

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
        for(Var v : table.getVars()) {
            result.add(v);
        }
//        result.add(Vars.g);
//        result.add(Vars.s);
//        result.add(Vars.p);
//        result.add(Vars.o);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding binding = it.next();
            result.add(binding);
        }

        return result;
    }

    public static Query createQueryCheckExistenceValues(Iterator<Quad> it) {
        Tables tables = createTablesForQuads(it);

        boolean useG = !tables.getNamedGraphTable().isEmpty();


        Element element = createElement(tables);

        Query result = new Query();
        result.setQuerySelectType();

        if(useG) {
            result.getProject().add(Vars.g);
        }

        result.getProject().add(Vars.s);
        result.getProject().add(Vars.p);
        result.getProject().add(Vars.o);

        result.setQueryPattern(element);

        return result;
    }

    public static Element createElement(Tables tables) {
        Table defaultGraphTable = tables.getDefaultGraphTable();
        Table namedGraphTable = tables.getNamedGraphTable();

        boolean isDefaultGraph = !defaultGraphTable.isEmpty();
        boolean isNamedGraph = !namedGraphTable.isEmpty();
        //boolean useUnion = ! && !defaultGraphTable.isEmpty();

        Element e1 = isDefaultGraph ? createElementDefaultGraph(defaultGraphTable) : null;
        Element e2 = isNamedGraph ? createElementNamedGraph(namedGraphTable) : null;

        Element result;

        if(e1 != null && e2 != null) {
            ElementUnion tmp = new ElementUnion();
            tmp.addElement(e1);
            tmp.addElement(e2);
            result = tmp;
        } else if(e1 != null) {
            result = e1;
        } else if(e2 != null) {
            result = e2;
        } else {
            result = null;
        }

        return result;
    }

    public static Element createElementDefaultGraph(Table table) {
        Element elData = tableToElement(table);

        ElementTriplesBlock elTriples = new ElementTriplesBlock();
        elTriples.addTriple(new Triple(Vars.s, Vars.p, Vars.o));

        ElementGroup result = new ElementGroup();
        result.addElement(elData);
        result.addElement(elTriples);

        return result;
    }

    public static Element createElementNamedGraph(Table table) {
        Element elData = tableToElement(table);

        ElementTriplesBlock elTriples = new ElementTriplesBlock();
        elTriples.addTriple(new Triple(Vars.s, Vars.p, Vars.o));

        ElementGroup result = new ElementGroup();
        result.addElement(elData);
        result.addElement(new ElementNamedGraph(Vars.g, elTriples));

        return result;
    }


    public static void addToTables(Tables tables, Quad quad) {
        Node g = quad.getGraph();

        Table defaultGraphTable = tables.getDefaultGraphTable();
        Table namedGraphTable = tables.getNamedGraphTable();

        if(Quad.defaultGraphNodeGenerated.equals(g)) {
            Triple triple = quad.asTriple();
            Binding binding = createBinding(triple);
            defaultGraphTable.addBinding(binding);
        } else {
            Binding binding = createBinding(quad);
            namedGraphTable.addBinding(binding);
        }

    }

    public static void addToTables(Tables tables, Iterator<Quad> it) {
        while(it.hasNext()) {
            Quad quad = it.next();

            addToTables(tables, quad);
        }
    }

    public static Tables createTablesForQuads(Iterator<Quad> it)
    {
        Tables result = new Tables();
        addToTables(result, it);
        return result;
    }

    public static BindingHashMap createBinding(Quad q) {
        BindingHashMap result = new BindingHashMap();

        result.add(Vars.g, q.getGraph());
        tripleToBinding(q.asTriple(), result);

        return result;
    }

    public static BindingHashMap createBinding(Triple t) {
        BindingHashMap result = new BindingHashMap();
        tripleToBinding(t, result);
        return result;
    }

    public static void tripleToBinding(Triple t, BindingHashMap result) {
        result.add(Vars.s, t.getSubject());
        result.add(Vars.p, t.getPredicate());
        result.add(Vars.o, t.getObject());
    }

//    public static Table createTableForQuads(Map<Node, Graph> nodeToGraph) {
//        Iterator<Quad> it = IteratorQuadsFromNodeToGraph.create(nodeToGraph);
//        Table result = createTableForQuads(it);
//        return result;
//    }
//
//    public static Table createTableForTriples(Iterator<Triple> it) {
//        Table result = TableFactory.create(Vars.spo);
//
//        while(it.hasNext()) {
//            Triple triple = it.next();
//
//            Binding binding = createBinding(triple);
//            result.addBinding(binding);
//        }
//
//        return result;
//    }
//
//    public static Table createTableForQuads(Iterator<Quad> it) {
//        Table result = TableFactory.create(Vars.gspo);
//
//        while(it.hasNext()) {
//            Quad quad = it.next();
//
//            Node g = quad.getGraph();
//
//            Binding binding = g != null && !g.equals(Quad.defaultGraphIRI)
//                ? createBinding(quad)
//                : createBinding(quad.asTriple());
//
//            result.addBinding(binding);
//        }
//
//        return result;
//    }

    //
//  public static Query createQueryCheckExistenceValues(Map<Node, Graph> nodeToGraph) {
//      Table table = createTableForQuads(nodeToGraph);
//      Element elData = tableToElement(table);
//
//      ElementTriplesBlock elTriples = new ElementTriplesBlock();
//      elTriples.addTriple(new Triple(Vars.s, Vars.p, Vars.o));
//
//      ElementGroup elGroup = new ElementGroup();
//      elGroup.addElement(new ElementNamedGraph(Vars.g, elTriples));
//      elGroup.addElement(elData);
//
//      Query result = new Query();
//      result.setQuerySelectType();
//
//      result.getProject().add(Vars.g);
//      result.getProject().add(Vars.s);
//      result.getProject().add(Vars.p);
//      result.getProject().add(Vars.o);
//
//      result.setQueryPattern(elGroup);
//
//      return result;
//  }

//public static Set<Quad> checkContainment(QueryExecutionFactory qef, Map<Node, Graph> nodeToGraph) {
//Set<Quad> result = new HashSet<Quad>();
//
//Query query = QuadContainmentUtils.createQueryCheckExistenceValues(nodeToGraph);
//System.out.println("Containment Check Query: " + query);
//QueryExecution qe = qef.createQueryExecution(query);
//ResultSet rs = qe.execSelect();
//while(rs.hasNext()) {
//    Binding binding = rs.nextBinding();
//
//    Node g = binding.get(Vars.g);
//    Node s = binding.get(Vars.s);
//    Node p = binding.get(Vars.p);
//    Node o = binding.get(Vars.o);
//
//    Quad quad = new Quad(g, s, p, o);
//    result.add(quad);
//}
//
//return result;
//}

    /*
    public static Map<Node, Graph> checkExistence(QuadContainmentChecker gec, QueryExecutionFactory qef, Iterable<Quad> quads) {
        Map<Node, Graph> nodeToGraph = QuadPatternUtils.indexAsGraphs(quads);
        Map<Node, Graph> result = gec.contains(nodeToGraph);
        return result;
    }
    */
}
