/**
 * 
 */
package org.aksw.jena_sparql_api.core;

import java.util.Collection;
import java.util.Collections;

/**
 * Wraps a SPARQL service instance(dataset) accessible via HTTP.
 * @author Lorenz Buehmann
 *
 */
public class SparqlServiceReference {
	
	private final String serviceURL;
	private final Collection<String> defaultGraphURIs;
	private final Collection<String> namedGraphURIs;
	
	public SparqlServiceReference(String serviceURL) {
		this(serviceURL, Collections.<String>emptySet());
	}
	
	public SparqlServiceReference(String serviceURL, Collection<String> defaultGraphURIs) {
		this(serviceURL, defaultGraphURIs, Collections.<String>emptySet());
	}
	
	public SparqlServiceReference(String serviceURL, Collection<String> defaultGraphURIs, Collection<String> namedGraphURIs) {
		this.serviceURL = serviceURL;
		this.defaultGraphURIs = defaultGraphURIs;
		this.namedGraphURIs = namedGraphURIs;
	}
	
	/**
	 * @return the serviceURL
	 */
	public String getServiceURL() {
		return serviceURL;
	}
	
	/**
	 * @return the defaultGraphURIs
	 */
	public Collection<String> getDefaultGraphURIs() {
		return defaultGraphURIs;
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
		return namedGraphURIs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultGraphURIs == null) ? 0 : defaultGraphURIs.hashCode());
		result = prime * result + ((namedGraphURIs == null) ? 0 : namedGraphURIs.hashCode());
		result = prime * result + ((serviceURL == null) ? 0 : serviceURL.hashCode());
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
		if (defaultGraphURIs == null) {
			if (other.defaultGraphURIs != null)
				return false;
		} else if (!defaultGraphURIs.equals(other.defaultGraphURIs))
			return false;
		if (namedGraphURIs == null) {
			if (other.namedGraphURIs != null)
				return false;
		} else if (!namedGraphURIs.equals(other.namedGraphURIs))
			return false;
		if (serviceURL == null) {
			if (other.serviceURL != null)
				return false;
		} else if (!serviceURL.equals(other.serviceURL))
			return false;
		return true;
	}

}
