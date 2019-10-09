package org.aksw.jena_sparql_api.io.endpoint;

import java.nio.file.Path;

import com.google.common.io.ByteSource;

public class Destinations {
	public static Destination fromFile(Path path) {
		return new DestinationFromFile(path);
	}
	
	public static DestinationFromByteSource fromByteSource(ByteSource byteSource) {
		return new DestinationFromByteSource(byteSource);	
	}
}
