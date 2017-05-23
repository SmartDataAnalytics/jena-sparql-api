package org.aksw.combinatorics.collections;

import com.google.common.collect.Multimap;

public class Cluster<A, B, S> {
    protected S cluster;
    protected Multimap<A, B> mappings;
    
    public Cluster(S cluster, Multimap<A, B> mappings) {
        super();
        this.cluster = cluster;
        this.mappings = mappings;
    }

    public S getCluster() {
        return cluster;
    }

    public Multimap<A, B> getMappings() {
        return mappings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
        result = prime * result
                + ((mappings == null) ? 0 : mappings.hashCode());
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
        Cluster<?, ?, ?> other = (Cluster<?, ?, ?>) obj;
        if (cluster == null) {
            if (other.cluster != null)
                return false;
        } else if (!cluster.equals(other.cluster))
            return false;
        if (mappings == null) {
            if (other.mappings != null)
                return false;
        } else if (!mappings.equals(other.mappings))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Cluster [cluster=" + cluster + ", mappings=" + mappings + "]";
    }
}