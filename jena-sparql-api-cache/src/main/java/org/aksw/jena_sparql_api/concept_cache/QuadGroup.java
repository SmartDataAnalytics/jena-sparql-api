package org.aksw.jena_sparql_api.concept_cache;

import java.util.Collection;

import com.hp.hpl.jena.sparql.core.Quad;

class QuadGroup {
    private Collection<Quad> candQuads;
    private Collection<Quad> queryQuads;
    public QuadGroup(Collection<Quad> candQuads, Collection<Quad> queryQuads) {
        super();
        this.candQuads = candQuads;
        this.queryQuads = queryQuads;
    }
    public Collection<Quad> getCandQuads() {
        return candQuads;
    }
    public Collection<Quad> getQueryQuads() {
        return queryQuads;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((candQuads == null) ? 0 : candQuads.hashCode());
        result = prime * result
                + ((queryQuads == null) ? 0 : queryQuads.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuadGroup other = (QuadGroup) obj;
        if (candQuads == null) {
            if (other.candQuads != null)
                return false;
        } else if (!candQuads.equals(other.candQuads))
            return false;
        if (queryQuads == null) {
            if (other.queryQuads != null)
                return false;
        } else if (!queryQuads.equals(other.queryQuads))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "QuadGroup [candQuads=" + candQuads + ", queryQuads="
                + queryQuads + "]";
    }
}