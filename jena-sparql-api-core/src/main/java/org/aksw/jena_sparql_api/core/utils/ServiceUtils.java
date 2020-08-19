package org.aksw.jena_sparql_api.core.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.OrderedConcept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.syntax.QueryGenerationUtils;
import org.aksw.jena_sparql_api.utils.CloseableQueryExecution;
import org.aksw.jena_sparql_api.utils.CountInfo;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Range;

public class ServiceUtils {
//    public static fetchList(QueryExecutionFactory, QueryExecutionFactory)


    public static List<Resource> fetchListResources(SparqlQueryConnection qef, Concept concept) {
        List<Node> tmp = fetchList(qef, concept);
        List<Resource> result = new ArrayList<Resource>(tmp.size());
        for(Node node : tmp) {
            Resource resource = ResourceFactory.createResource(node.getURI());
            result.add(resource);
        }

        return result;
    }

    public static List<Node> fetchList(SparqlQueryConnection qef, OrderedConcept orderedConcept, Long limit, Long offset) {
        Query query = ConceptUtils.createQueryList(orderedConcept, limit, offset);
        //System.out.println("Query: " + query);
        List<Node> result = fetchList(qef, query, orderedConcept.getConcept().getVar());
        return result;
    }

    public static List<Node> fetchList(SparqlQueryConnection qef, UnaryRelation concept, Long limit, Long offset) {
        Query query = ConceptUtils.createQueryList(concept, limit, offset);
        List<Node> result = fetchList(qef, query, concept.getVar());
        return result;
    }

    public static List<Node> fetchList(SparqlQueryConnection qef, UnaryRelation concept) {
        Query query = ConceptUtils.createQueryList(concept);
        List<Node> result = fetchList(qef, query, concept.getVar());
        return result;
    }

    public static List<Node> fetchList(SparqlQueryConnection qef, Query query, Var v) {
        QueryExecution qe = qef.query(query);
        List<Node> result = fetchList(qe, v);
        return result;
    }

    public static List<Node> fetchList(QueryExecution qe, Var v) {
        try {
            ResultSet rs = qe.execSelect();
            List<Node> result = ResultSetUtils.resultSetToList(rs, v);
            return result;
        } finally {
            qe.close();
        }
    }


    public static Integer fetchInteger(QueryExecutionFactory qef, Query query, Var v) {
        //System.out.println(query);
        QueryExecution qe = qef.createQueryExecution(query);
        Integer result = fetchInteger(qe, v);
        return result;
    }

    /**
     * Fetches the first column of the first row of a result set and parses it as int.
     *
     */
    public static Integer fetchInteger(QueryExecution qe, Var v) {
        ResultSet rs = qe.execSelect();
        //System.out.println(ResultSetFormatter.asText(rs));
        Integer result = ResultSetUtils.resultSetToInt(rs, v);

        return result;
    }

    /**
     * Attempt to get a Number from the first row of a result set for a given variable.
     * Returns null if there is no row or the value is unbound.
     * Raises an exception if the obtained RDFNode cannot be converted to a number or if there is more than 1 result row.
     *
     * @param qe
     * @param v
     * @return
     */
    public static Number fetchNumber(QueryExecution qe, Var v) {
        Number result;
        try(QueryExecution myQe = qe) {
            ResultSet rs = myQe.execSelect();

            RDFNode tmp = ResultSetUtils.getNextRDFNode(rs, v);
            if(tmp != null && tmp.isLiteral()) {
                Object val = tmp.asLiteral().getValue();
                if(val == null) {
                    result = null;
                } else {
                    if (!(val instanceof Number)) {
                        throw new RuntimeException("Value " + val + " is not a Number");
                    }

                    result = (Number)val;
                }
            } else {
                throw new RuntimeException("RDFNode " + tmp + " is not a literal");
            }

            if (rs.hasNext()) {
                throw new RuntimeException("At most one result set row expected");
            }
        }

        return result;
    }

//    public static Long fetchLong(QueryExecutionFactory qef, Concept c) {
//
//    }


    // NOTE: If there is a rowLimit, we can't determine whether there are more items or not
    public static CountInfo fetchCountConcept(QueryExecutionFactory sparqlService, Concept concept, Long itemLimit, Long rowLimit) {

        Var outputVar = ConceptUtils.freshVar(concept);

        long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Query countQuery = ConceptUtils.createQueryCount(concept, outputVar, xitemLimit, xrowLimit);

        //var qe = sparqlService.createQueryExecution(countQuery);

        Integer count = ServiceUtils.fetchInteger(sparqlService, countQuery, outputVar);
        boolean hasMoreItems = rowLimit != null
            ? null
            : (itemLimit != null ? count > itemLimit : false)
            ;

        Long c = hasMoreItems ? itemLimit : count;
        CountInfo result = new CountInfo(c, hasMoreItems, itemLimit);
        return result;
    }

    public static Range<Long> fetchCountQuery(QueryExecutionFactory sparqlService, Query query, Long itemLimit, Long rowLimit) {

        //Var outputVar = Var.alloc("_count_"); //ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Entry<Var, Query> e = QueryGenerationUtils.createQueryCount(query, xitemLimit, xrowLimit);
        Var outputVar = e.getKey();
        Query countQuery = e.getValue();

        //var qe = sparqlService.createQueryExecution(countQuery);

        Integer count = ServiceUtils.fetchInteger(sparqlService, countQuery, outputVar);
        boolean hasMoreItems = rowLimit != null
            ? null
            : (itemLimit != null ? count > itemLimit : false)
            ;

        //Long c = hasMoreItems ? itemLimit : count;
        Range<Long> result = hasMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count.longValue());
        //CountInfo result = new CountInfo(c, hasMoreItems, itemLimit);
        return result;
    }


    /**
     * CONSTRUCT queries are mapped to result sets with the variables ?s ?p ?o
     *
     */
    public static ResultSet forceExecResultSet(QueryExecution qe, Query query) {
        ResultSet result;
        if(query.isSelectType()) {
            result = qe.execSelect();

        } else if(query.isConstructType()) {
            Iterator<Triple> it = qe.execConstructTriples();
            result = org.aksw.jena_sparql_api.core.utils.ResultSetUtils.tripleIteratorToResultSet(it, new CloseableQueryExecution(qe));
        } else {
            throw new RuntimeException("Query type is not supported: " + query);
        }

        return result;
    }

}

