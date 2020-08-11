package org.aksw.jena_sparql_api.sparql_path.core.algorithm;

public class PathFinderConfig {
    private Integer maxDirectionChanges;
    private Integer maxPaths;
    private Integer timeoutInMillis;
    
    public PathFinderConfig(Integer maxDirectionChanges, Integer maxPaths,
            Integer timeoutInMillis) {
        super();
        this.maxDirectionChanges = maxDirectionChanges;
        this.maxPaths = maxPaths;
        this.timeoutInMillis = timeoutInMillis;
    }

    public Integer getMaxDirectionChanges() {
        return maxDirectionChanges;
    }

    public Integer getMaxPaths() {
        return maxPaths;
    }

    public Integer getTimeoutInMillis() {
        return timeoutInMillis;
    }

    @Override
    public String toString() {
        return "PathFinderConfig [maxDirectionChanges=" + maxDirectionChanges
                + ", maxPaths=" + maxPaths + ", timeoutInMillis="
                + timeoutInMillis + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((maxDirectionChanges == null) ? 0 : maxDirectionChanges
                        .hashCode());
        result = prime * result
                + ((maxPaths == null) ? 0 : maxPaths.hashCode());
        result = prime * result
                + ((timeoutInMillis == null) ? 0 : timeoutInMillis.hashCode());
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
        PathFinderConfig other = (PathFinderConfig) obj;
        if (maxDirectionChanges == null) {
            if (other.maxDirectionChanges != null)
                return false;
        } else if (!maxDirectionChanges.equals(other.maxDirectionChanges))
            return false;
        if (maxPaths == null) {
            if (other.maxPaths != null)
                return false;
        } else if (!maxPaths.equals(other.maxPaths))
            return false;
        if (timeoutInMillis == null) {
            if (other.timeoutInMillis != null)
                return false;
        } else if (!timeoutInMillis.equals(other.timeoutInMillis))
            return false;
        return true;
    }
}
