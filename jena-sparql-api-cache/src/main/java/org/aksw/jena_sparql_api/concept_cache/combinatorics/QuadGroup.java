package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;

import org.apache.jena.sparql.core.Quad;

public class QuadGroup
    extends CollectionPair<Quad>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public QuadGroup(Collection<Quad> key, Collection<Quad> value) {
        super(key, value);
    }

    /**
     * Convenience accessors with nicer naming
     *
     * @return
     */
    public Collection<Quad> getCandQuads() {
        return key;
    }

    public Collection<Quad> getQueryQuads() {
        return value;
    }
}
