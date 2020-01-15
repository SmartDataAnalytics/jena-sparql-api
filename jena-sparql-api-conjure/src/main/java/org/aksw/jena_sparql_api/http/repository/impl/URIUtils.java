package org.aksw.jena_sparql_api.http.repository.impl;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {	
  	public static URI newURI(String uri) {
		URI result;
		try {
			result = new URI(uri);
		} catch (URISyntaxException e) {
			result = null;
		}
		return result;
	}
}
