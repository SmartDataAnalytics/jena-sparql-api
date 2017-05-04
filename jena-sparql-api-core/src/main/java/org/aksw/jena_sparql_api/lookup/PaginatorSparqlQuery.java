package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
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

import com.google.common.collect.Range;

public class PaginatorSparqlQuery
    extends PaginatorSparqlQueryBase<Node, ResultSetPart>
{
    private static final Logger logger = LoggerFactory.getLogger(PaginatorSparqlQuery.class);


    protected Query attrQuery;
    protected Var attrVar;
    protected boolean forceSubQuery;


    public PaginatorSparqlQuery(QueryExecutionFactory qef, Concept filterConcept, boolean isLeftJoin, Query attrQuery, Var attrVar, boolean forceSubQuery) {
        super(qef, filterConcept, isLeftJoin);
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.forceSubQuery = forceSubQuery;
    }

    @Override
    public Map<Node, ResultSetPart> fetchData(Range<Long> range) {
        if(filterConcept == null) {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        // Make the filter concept make use of the attrVar
        if(!attrVar.equals(filterConcept.getVar())) {
            filterConcept = ConceptUtils.createRenamedConcept(filterConcept, attrVar);
        }

        //System.out.println(attrQuery);
        //if(true) { throw new RuntimeException("foo"); }

        Long limit = QueryUtils.rangeToLimit(range);
        Long offset = QueryUtils.rangeToOffset(range);

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
    public CountInfo fetchCount(Long itemLimit, Long rowLimit) {

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

    @Override
    public Stream<Entry<Node, ResultSetPart>> apply(Range<Long> range) {
        Map<Node, ResultSetPart> map = fetchData(range);
        return map.entrySet().stream();
    }
}
