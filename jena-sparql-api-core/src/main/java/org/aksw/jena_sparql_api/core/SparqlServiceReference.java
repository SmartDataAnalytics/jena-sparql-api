/**
 *
 */
package org.aksw.jena_sparql_api.core;

import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.annotation.Base;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.jena.sparql.core.DatasetDescription;

/**
 * Wraps a SPARQL service instance(dataset) accessible via HTTP.
 *
 * 2015-10-15: Added authenticator ~ Claus
 *
 * @author Lorenz Buehmann
 *
 *
 */

@Base("http://jpa.aksw.org/ontology/")
@RdfType("SparqlServiceReference")
@DefaultIri("r:#{#md5(serviceURL + datasetDescription.defaultGraphURIs + datasetDescription.namedGraphURIs)}")
public class SparqlServiceReference {

    @Iri
    private final String serviceURL;
    //private final Collection<String> defaultGraphURIs;
    //private final Collection<String> namedGraphURIs;

    @Iri
    private final DatasetDescription datasetDescription;

//    public SparqlServiceReference(String serviceURL) {
//        this(serviceURL, Collections.<String>emptySet());
//    }
//
//    public SparqlServiceReference(String serviceURL, List<String> defaultGraphURIs) {
//        this(serviceURL, defaultGraphURIs, Collections.<String>emptySet());
//    }
    //private final Object authenticator;
    protected final UsernamePasswordCredentials credentials;

    public SparqlServiceReference(String serviceURL, DatasetDescription datasetDescription) {
        this(serviceURL, datasetDescription, null);
    }

    public SparqlServiceReference(String serviceURL, DatasetDescription datasetDescription, UsernamePasswordCredentials credentials) {
        this.serviceURL = serviceURL;
        this.datasetDescription = datasetDescription;
        this.credentials = credentials;
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

    /**
     * @return the namedGraphURIs
     */
    public Collection<String> getNamedGraphURIs() {
        return datasetDescription.getNamedGraphURIs();
    }

    public UsernamePasswordCredentials getCredentials() {
        return credentials;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((credentials == null) ? 0 : credentials.hashCode());
        result = prime * result + ((datasetDescription == null) ? 0
                : datasetDescription.hashCode());
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
        if (credentials == null) {
            if (other.credentials != null)
                return false;
        } else if (!credentials.equals(other.credentials))
            return false;
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

    @Override
    public String toString() {
        return "SparqlServiceReference [serviceURL=" + serviceURL
                + ", datasetDescription=" + datasetDescription
                + ", authenticator=" + credentials + "]";
    }

}
