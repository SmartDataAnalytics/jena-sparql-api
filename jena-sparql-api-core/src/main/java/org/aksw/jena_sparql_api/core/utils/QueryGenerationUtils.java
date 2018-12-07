package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;

public class QueryGenerationUtils {

    public static Query createQueryQuad(Quad quad) {
        Query query = new Query();
        query.setQuerySelectType();

        Node g = quad.getGraph();
        Node s = quad.getSubject();
        Node p = quad.getPredicate();
        Node o = quad.getObject();

        s = g == null || g.equals(Node.ANY) ? Vars.g : g;
        s = s == null || s.equals(Node.ANY) ? Vars.s : s;
        p = p == null || p.equals(Node.ANY) ? Vars.p : p;
        o = o == null || o.equals(Node.ANY) ? Vars.o : o;

        Triple triple = new Triple(s, p, o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        Element element = new ElementTriplesBlock(bgp);

        element = new ElementNamedGraph(g, element);

        query.setQueryPattern(element);
        return query;
    }

    public static Query createQueryTriple(Triple m) {
        Query query = new Query();
        query.setQueryConstructType();

        /*
        Node s = m.getMatchSubject();
        Node p = m.getMatchPredicate();
        Node o = m.getMatchObject();
        */
        Node s = m.getSubject();
        Node p = m.getPredicate();
        Node o = m.getObject();

        s = s == null || s.equals(Node.ANY) ? Vars.s : s;
        p = p == null || p.equals(Node.ANY) ? Vars.p : p;
        o = o == null || o.equals(Node.ANY) ? Vars.o : o;

        Triple triple = new Triple(s, p, o);

        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);

        Template template = new Template(bgp);
        Element element = new ElementTriplesBlock(bgp);

        query.setConstructTemplate(template);
        query.setQueryPattern(element);
        return query;
    }

    // Util for cerateQueryCount
    public static Query wrapAsSubQuery(Query query, Var v) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.getProject().add(v);
        result.setQueryPattern(esq);

        return result;
    }

    public static Query wrapAsSubQuery(Query query) {
        Element esq = new ElementSubQuery(query);

        Query result = new Query();
        result.setQuerySelectType();
        result.setQueryResultStar(true);
        result.setQueryPattern(esq);

        return result;
    }


    public static Query createQueryCount(Query query, Var outputVar, Long itemLimit, Long rowLimit) {
        Query subQuery = query.cloneQuery();

        if(rowLimit != null) {
            subQuery.setDistinct(false);
            subQuery.setLimit(rowLimit);

            subQuery = QueryGenerationUtils.wrapAsSubQuery(subQuery);
            subQuery.setDistinct(true);
        }

        if(itemLimit != null) {
            subQuery.setLimit(itemLimit);
        }

        Element esq = new ElementSubQuery(subQuery);

        Query result = new Query();
        Expr aggCount = result.allocAggregate(new AggCount());
        result.setQuerySelectType();
        result.getProject().add(outputVar, aggCount);
        result.setQueryPattern(esq);

        return result;
    }



    /**
     * Takes a concept and adds
     *
     * @return
     */
    public static Concept createPredicateQuery(UnaryRelation concept) {
        Collection<Var> vars = PatternVars.vars(concept.getElement());
        List<String> varNames = VarUtils.getVarNames(vars);

        Var s = concept.getVar();

        Generator gen = GeneratorBlacklist.create("v", varNames);
        Var p = Var.alloc(gen.next());
        Var o = Var.alloc(gen.next());


        Triple triple = new Triple(s, p, o);

        BasicPattern bp = new BasicPattern();
        bp.add(triple);

        List<Element> elements;
        if(concept.isSubjectConcept()) {
            elements = new ArrayList<Element>();
        } else {
            elements = concept.getElements();
        }
        elements.add(new ElementTriplesBlock(bp));

        Concept result = new Concept(elements, p);

        return result;
    }
}
