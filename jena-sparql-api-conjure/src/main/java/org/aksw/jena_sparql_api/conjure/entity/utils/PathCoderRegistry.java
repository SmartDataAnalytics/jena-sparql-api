package org.aksw.jena_sparql_api.conjure.entity.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PathCoderRegistry {
	protected static PathCoderRegistry INSTANCE = null;

	protected Map<String, PathCoder> coders = new LinkedHashMap<>();
	
	public static PathCoderRegistry get() {
		if(INSTANCE == null) {
			INSTANCE = new PathCoderRegistry();

			INSTANCE.coders.put("bzip2", new PathCoderLbZip());
			INSTANCE.coders.put("gzip", new PathCoderGzip());
		}
		
		return INSTANCE;
	}
	
	
	public PathCoder getCoder(String name) {
		return coders.get(name);
	}
	
	public Set<String> getCoderNames() {
		return Collections.unmodifiableSet(coders.keySet());
	}
}
