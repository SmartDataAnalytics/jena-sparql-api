package org.aksw.sparqlify.database;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;

public class FilterSplit {
    private RestrictionManagerImpl pushable;
    private RestrictionManagerImpl nonPushable;

    public FilterSplit(RestrictionManagerImpl pushable, RestrictionManagerImpl nonPushable) {
        this.pushable = pushable;
        this.nonPushable = nonPushable;
    }

    public RestrictionManagerImpl getPushable() {
        return pushable;
    }

    public RestrictionManagerImpl getNonPushable() {
        return nonPushable;
    }
}
