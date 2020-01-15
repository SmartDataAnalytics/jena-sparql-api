package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.GeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggCountDistinct;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
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

    public static Entry<Var, Query> createQueryCount(Query query) {
    	return createQueryCount(query, null, null);
    }

    public static Entry<Var, Query> createQueryCountPartition(Query query, Collection<Var> partitionVars, Long itemLimit, Long rowLimit) {
    	query = query.cloneQuery();

    	if(query.isConstructType()) {
        	Template template = query.getConstructTemplate();
        	Set<Var> vars = partitionVars == null
        			? QuadPatternUtils.getVarsMentioned(template.getQuads())
        			: new LinkedHashSet<>(partitionVars);

            query.setQuerySelectType();

        	// TODO Vars may be empty, in case we deal with a partitioned query
        	if(vars.isEmpty()) {
        		//query.setQueryResultStar(true);
        		throw new RuntimeException("Variables required for counting");
        	} else {
        		query.setQueryResultStar(false);
	        	query.addProjectVars(vars);
	        	query.setDistinct(true);
        	}
        } else {
        	throw new RuntimeException("Not implemented for select queries");
        	// TODO We need to check whether the partition variables are mapped to expressions in the projection
        }

    	Entry<Var, Query> result = createQueryCountCore(query, itemLimit, rowLimit);
    	return result;
    }

    public static Entry<Var, Query> createQueryCount(Query query, Long itemLimit, Long rowLimit) {
    	Entry<Var, Query> result = createQueryCountPartition(query, null, itemLimit, rowLimit);
    	return result;
    }

//    public static Entry<Var, Query> createQueryCount(Query query, Long itemLimit, Long rowLimit) {    	
//    	query = query.cloneQuery();
//
//    	if(query.isConstructType()) {
//        	Template template = query.getConstructTemplate();
//        	Set<Var> vars = QuadPatternUtils.getVarsMentioned(template.getQuads());
//        	// TODO Vars may be empty, in case we deal with a partitioned query
//        	
//        	query.setQuerySelectType();
//        	if(vars.isEmpty()) {
//        		query.setQueryResultStar(true);
//        	} else {
//	        	query.addProjectVars(vars);
//        	}
//        }
//    	
//    	Entry<Var, Query> result = createQueryCountCore(query, itemLimit, rowLimit);
//    	return result;
//    }

    
    public static Entry<Var, Query> createQueryCountCore(Query query, Long itemLimit, Long rowLimit) {
        Var resultVar = Vars.c;
    	
    	boolean needsWrapping = !query.getGroupBy().isEmpty() || !query.getAggregators().isEmpty();
	
	      if(rowLimit != null) {
		      query.setDistinct(false);
		      query.setLimit(rowLimit);
		
		      query = QueryGenerationUtils.wrapAsSubQuery(query);
		      query.setDistinct(true);
		      needsWrapping = true;
	      }
	
		  if(itemLimit != null) {
		      query.setLimit(itemLimit);
		      needsWrapping = true;
		  }

	  //Element esq = new ElementSubQuery(subQuery);

        Var singleResultVar = null;
        VarExprList project = query.getProject();
        if(project.size() == 1) {
        	Var v = project.getVars().iterator().next();
        	Expr e = project.getExpr(v);
//        	Entry<Var, Expr> tmp = project.getExprs().entrySet().iterator().next();
//        	Var v = tmp.getKey();
//        	Expr e = tmp.getValue();
        	if(e == null || (e.isVariable() && e.asVar().equals(v))) {
        		singleResultVar = v;
        	}
        }
        
        boolean useCountDistinct = !needsWrapping && query.isDistinct() && (query.isQueryResultStar() || singleResultVar != null);
        // TODO If there is only a single result variable (without mapping to an expr)
        // we can also use count distinct
        


        Aggregator agg = useCountDistinct
                ? singleResultVar == null
            		? new AggCountDistinct()
            		: new AggCountVarDistinct(new ExprVar(singleResultVar))
                : new AggCount();

        Query result = new Query();
        result.setQuerySelectType();
        result.setPrefixMapping(query.getPrefixMapping());
        //cQuery.getProject().add(Vars.c, new ExprAggregator(Vars.x, agg));
        Expr aggCount = result.allocAggregate(agg);
        result.getProject().add(resultVar, aggCount);

        Element queryPattern;
        if(needsWrapping) {
            Query q = query.cloneQuery();            
            q.setPrefixMapping(new PrefixMappingImpl());
            queryPattern = new ElementSubQuery(q);
        } else {
            queryPattern = query.getQueryPattern();
        }


        result.setQueryPattern(queryPattern);
        
        return Maps.immutableEntry(resultVar, result);
    }

//    public static Query createQueryCount(Query query, Var outputVar, Long itemLimit, Long rowLimit) {
//        Query subQuery = query.cloneQuery();
//        subQuery.setQuerySelectType();
//        subQuery.setQueryResultStar(true);
//
//        if(rowLimit != null) {
//            subQuery.setDistinct(false);
//            subQuery.setLimit(rowLimit);
//
//            subQuery = QueryGenerationUtils.wrapAsSubQuery(subQuery);
//            subQuery.setDistinct(true);
//        }
//
//        if(itemLimit != null) {
//            subQuery.setLimit(itemLimit);
//        }
//
//        Element esq = new ElementSubQuery(subQuery);
//
//        Query result = new Query();
//        result.setQuerySelectType();
//        Expr aggCount = result.allocAggregate(new AggCount());
//        result.getProject().add(outputVar, aggCount);
//        result.setQueryPattern(esq);
//
//        return result;
//    }



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
