package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.utils.ResultSetPart;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;


public class MapServiceSparqlQuery
    implements MapService<Concept, Node, ResultSetPart>
{
    protected QueryExecutionFactory qef;
    protected boolean isLeftJoin;

    protected Query attrQuery;
    protected Var attrVar;
    protected boolean forceSubQuery;

    public MapServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery, Var attrVar) {
        this(qef, attrQuery, attrVar, true, false);
    }

    public MapServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery, Var attrVar, boolean isLeftJoin) {
        this(qef, attrQuery, attrVar, isLeftJoin, false);
    }

    public MapServiceSparqlQuery(QueryExecutionFactory qef, Query attrQuery,
            Var attrVar, boolean isLeftJoin, boolean forceSubQuery) {
        super();
        this.qef = qef;
        this.attrQuery = attrQuery;
        this.attrVar = attrVar;
        this.isLeftJoin = isLeftJoin;
        this.forceSubQuery = forceSubQuery;
    }

    @Override
    public MapPaginator<Node, ResultSetPart> createPaginator(Concept filterConcept) {
        MapPaginatorSparqlQuery result = new MapPaginatorSparqlQuery(qef, filterConcept, isLeftJoin, attrQuery, attrVar, forceSubQuery);
        return result;
    }

}
