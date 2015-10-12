package org.aksw.jena_sparql_api.hop;

import java.util.ArrayList;
import java.util.List;


public class Hop {
    protected List<HopQuery> hopQueries;
    protected List<HopRelation> hopRelations;

    public Hop() {
        this(new ArrayList<HopQuery>(), new ArrayList<HopRelation>());
    }

    public Hop(List<HopQuery> hopQueries, List<HopRelation> hopRelations) {
        super();
        this.hopQueries = hopQueries;
        this.hopRelations = hopRelations;
    }

    public List<HopQuery> getHopQueries() {
        return hopQueries;
    }

    public List<HopRelation> getHopRelations() {
        return hopRelations;
    }

    @Override
    public String toString() {
        return "Hop [hopQueries=" + hopQueries + ", hopRelations="
                + hopRelations + "]";
    }
}
