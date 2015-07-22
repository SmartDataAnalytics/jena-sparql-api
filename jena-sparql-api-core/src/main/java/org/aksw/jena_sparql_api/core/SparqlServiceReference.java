/**
 *
 */
package org.aksw.jena_sparql_api.core;

import java.util.Collection;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

/**
 * Wraps a SPARQL service instance(dataset) accessible via HTTP.
 * @author Lorenz Buehmann
 *
 */
public class SparqlServiceReference {

    private final String serviceURL;
    //private final Collection<String> defaultGraphURIs;
    //private final Collection<String> namedGraphURIs;
    private final DatasetDescription datasetDescription;

//    public SparqlServiceReference(String serviceURL) {
//        this(serviceURL, Collections.<String>emptySet());
//    }
//
//    public SparqlServiceReference(String serviceURL, List<String> defaultGraphURIs) {
//        this(serviceURL, defaultGraphURIs, Collections.<String>emptySet());
//    }

    public SparqlServiceReference(String serviceURL, DatasetDescription datasetDescription) {
        this.serviceURL = serviceURL;
        this.datasetDescription = datasetDescription;
    }

    /**
     * @return the serviceURL
     */
    public String getServiceURL() {
        return serviceURL;
    }

    public DatasetDescription getDatasetDescription() {
        return datasetDescription;
    }

    /**
     * @return the defaultGraphURIs
     */
    public Collection<String> getDefaultGraphURIs() {
        return datasetDescription.getDefaultGraphURIs();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @return the namedGraphURIs
     */
    public Collection<String> getNamedGraphURIs() {
        return datasetDescription.getNamedGraphURIs();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((datasetDescription == null) ? 0 : datasetDescription
                        .hashCode());
        result = prime * result
                + ((serviceURL == null) ? 0 : serviceURL.hashCode());
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
        SparqlServiceReference other = (SparqlServiceReference) obj;
        if (datasetDescription == null) {
            if (other.datasetDescription != null)
                return false;
        } else if (!datasetDescription.equals(other.datasetDescription))
            return false;
        if (serviceURL == null) {
            if (other.serviceURL != null)
                return false;
        } else if (!serviceURL.equals(other.serviceURL))
            return false;
        return true;
    }
}
