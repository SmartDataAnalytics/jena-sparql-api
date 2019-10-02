package org.aksw.jena_sparql_api.http.repository.impl;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.aksw.commons.util.strings.StringUtils;

public class UriToPathUtils {
	
	/**
	 * Default mapping of URIs to relative paths
	 * 
	 * scheme://host:port/path?query becomes
	 * host/port/path/query
	 * 
	 * @param uri
	 * @return
	 */
	public static Path resolvePath(URI uri) {
		String a = Optional.ofNullable(uri.getHost()).orElse("");
		String b = uri.getPort() == -1 ? "" : Integer.toString(uri.getPort());
		
		Path result = Paths.get(".")
		.resolve(a)
		.resolve(b)
		.resolve((a.isEmpty() && b.isEmpty() ? "" : ".") + Optional.ofNullable(uri.getPath()).orElse(""))
		.resolve(Optional.ofNullable(uri.getQuery()).orElse(""))
		.normalize();
		
		return result;
	}
	
	public static Path resolvePath(String uri)  {
		URI u = URIUtils.newURI(uri);
		
		Path tmp = u == null ?
			Paths.get(StringUtils.urlEncode(uri))
			: UriToPathUtils.resolvePath(u);
			
		// Make absolute paths relative (i.e. remove leading slashes)
		Path result;
		if(tmp.isAbsolute()) {
			Path root = tmp.getRoot();
			result = root.relativize(tmp);
		} else {
			result = tmp;
		}
			
		//logger.info("Resolved: " + uri + "\n  to: " + result + "\n  via: " + u);
		return result;
	}

}
