package org.aksw.jena_sparql_api.hop;

import java.util.List;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class HopRelation
    extends HopBase
{
    protected BinaryRelation relation;
    protected List<Hop> hops;

    public HopRelation(SparqlQueryConnection qef) {
        super(qef);
    }

    public HopRelation(SparqlQueryConnection qef, BinaryRelation relation, List<Hop> hops) {
        super(qef);
        this.relation = relation;
        this.hops = hops;
    }

    public BinaryRelation getRelation() {
        return relation;
    }

    public List<Hop> getHops() {
        return hops;
    }

    @Override
    public String toString() {
        return "HopRelation [relation=" + relation + ", qef="
                + super.toString() + ", hops=" + hops + "]";
    }

}
