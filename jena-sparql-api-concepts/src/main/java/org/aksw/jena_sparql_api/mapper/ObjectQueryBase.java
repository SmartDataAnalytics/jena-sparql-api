package org.aksw.jena_sparql_api.mapper;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;

public abstract class ObjectQueryBase
    implements ObjectQuery
{
    protected Map<Node, ExprList> idMapping;

    public ObjectQueryBase(Map<Node, ExprList> idMapping) {
        super();
        this.idMapping = idMapping;
    }

    @Override
    public Map<Node, ExprList> getIdMapping() {
        return idMapping;
    }
}
