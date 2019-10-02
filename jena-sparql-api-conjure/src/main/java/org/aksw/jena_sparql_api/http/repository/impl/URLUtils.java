package org.aksw.jena_sparql_api.http.repository.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLUtils {
  	public static URL newURL(String uri) {
  		// There was some reason why to go from String to URL via URI... but i forgot...
  		URI tmp = URIUtils.newURI(uri);
  		URL result;
  		try {
			result = tmp.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
  		return result;
  	}

}
