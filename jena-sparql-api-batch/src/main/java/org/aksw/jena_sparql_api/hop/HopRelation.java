package org.aksw.jena_sparql_api.hop;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

public class HopRelation
    extends HopBase
{
    protected Relation relation;
    protected List<Hop> hops;

    public HopRelation(QueryExecutionFactory qef) {
        super(qef);
    }

    public HopRelation(QueryExecutionFactory qef, Relation relation, List<Hop> hops) {
        super(qef);
        this.relation = relation;
        this.hops = hops;
    }

    public Relation getRelation() {
        return relation;
    }

    public List<Hop> getHops() {
        return hops;
    }
}
