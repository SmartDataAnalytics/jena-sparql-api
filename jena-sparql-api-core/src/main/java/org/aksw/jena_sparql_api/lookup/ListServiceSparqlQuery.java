package org.aksw.jena_sparql_api.lookup;

import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.aksw.jena_sparql_api.utils.ResultSetUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;


public class ListServiceSparqlQuery
    implements ListService<Concept, Node, ResultSetPart>
{
    private QueryExecutionFactory qef;
    private Query attrQuery;
    private Var attrVar;
    private boolean isLeftJoin;
    private boolean forceSubQuery;

    public ListServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery, Var attrVar) {
        this(qef, attrQuery, attrVar, true, false);
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
        if(filterConcept != null) {
            filterConcept = ConceptUtils.createSubjectConcept();
        }

        Query query = ConceptUtils.createAttrQuery(this.attrQuery, attrVar, this.isLeftJoin, filterConcept, limit, offset, this.forceSubQuery);

        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        Map<Node, ResultSetPart> map = ResultSetUtils.partition(rs, attrVar);


    }
    @Override
    public CountInfo fetchCount(Concept filterConcept, Long itemLimit) {

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

        CountInfo result = ServiceUtils.fetchCountConcept(qef, countConcept, itemLimit); //rowLimit
        return result;
    }
}
