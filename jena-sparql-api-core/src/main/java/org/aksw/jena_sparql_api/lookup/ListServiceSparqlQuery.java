package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ListServiceSparqlQuery
    implements ListService<Concept, Node, ResultSetPart>
{
    private static final Logger logger = LoggerFactory.getLogger(ListServiceSparqlQuery.class);

    private QueryExecutionFactory qef;
    private Query attrQuery;
    private Var attrVar;
    private boolean isLeftJoin;
    private boolean forceSubQuery;

    public ListServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery, Var attrVar) {
        this(qef, attrQuery, attrVar, true, false);
    }

    public ListServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery, Var attrVar, boolean isLeftJoin) {
        this(qef, attrQuery, attrVar, isLeftJoin, false);
    }

    public ListServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery,
            Var attrVar, boolean isLeftJoin, boolean forceSubQuery) {
        super();
        this.qef = qef;
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.isLeftJoin = isLeftJoin;
        this.forceSubQuery = forceSubQuery;
    }


    @Override
    public Map<Node, ResultSetPart> fetchData(Concept filterConcept, Long limit, Long offset) {
        if(filterConcept == null) {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        // Make the filter concept make use of the attrVar
        if(!attrVar.equals(filterConcept.getVar())) {
            filterConcept = ConceptUtils.createRenamedConcept(filterConcept, attrVar);
        }

        //System.out.println(attrQuery);
        //if(true) { throw new RuntimeException("foo"); }

        Query query = ConceptUtils.createAttrQuery(this.attrQuery, attrVar, this.isLeftJoin, filterConcept, limit, offset, this.forceSubQuery);

        logger.debug("Query: " + query);
        //System.out.println(query);
        //if(true) {throw new RuntimeException(""); }

        QueryExecution qe = qef.createQueryExecution(query);
        //ResultSet rs = qe.execSelect();
        ResultSet rs = ServiceUtils.forceExecResultSet(qe, query);
        Map<Node, ResultSetPart> result = ResultSetUtils.partition(rs, attrVar);
        return result;
    }

    @Override
    public CountInfo fetchCount(Concept filterConcept, Long itemLimit, Long rowLimit) {

        if(filterConcept != null) {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        Concept countConcept;
        if(this.isLeftJoin) {
            Query query = ConceptUtils.createAttrQuery(this.attrQuery, this.attrVar, this.isLeftJoin, filterConcept, itemLimit, null, this.forceSubQuery);

            countConcept = new Concept(query.getQueryPattern(), this.attrVar);
        } else {
            Concept attrConcept = ( this.forceSubQuery
                ? new Concept(new ElementSubQuery(this.attrQuery), this.attrVar)
                : new Concept(this.attrQuery.getQueryPattern(), this.attrVar) )
                ;

            countConcept = ConceptUtils.createCombinedConcept(attrConcept, filterConcept, true, false, false);
//            console.log('FILTER ' + filterConcept);
//            console.log('ATTR ' + attrConcept);
//            console.log('COUNT ' + countConcept);
//            console.log('ROW ' + rowLimit);
        }

        CountInfo result = ServiceUtils.fetchCountConcept(qef, countConcept, itemLimit, null); //rowLimit
        return result;
    }
}
