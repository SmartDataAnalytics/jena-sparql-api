package org.aksw.jena_sparql_api.mapper;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

public class ObjectQueryFromQuery
    extends ObjectQueryBase
{
    protected Query query;

    public ObjectQueryFromQuery(Query query) {
        this(query, new HashMap<>());
    }

    public ObjectQueryFromQuery(Query query, Map<Node, ExprList> idMapping) {
        super(idMapping);
        this.query = query;
    }

    @Override
    public Template getTemplate() {
        return query.getConstructTemplate();
    }

    @Override
    public Relation getRelation() {
        // return RelationUtils.fromQuery(query);
        Query asSelect = query.cloneQuery();
        asSelect.setQuerySelectType();
        return RelationUtils.fromQuery(asSelect);
    }
}
