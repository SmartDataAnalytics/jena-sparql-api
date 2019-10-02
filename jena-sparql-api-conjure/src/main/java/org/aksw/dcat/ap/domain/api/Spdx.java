package org.aksw.dcat.ap.domain.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class Spdx {
	public static final String NS = "http://spdx.org/rdf/terms#";
	
	public static final String _Checksum = NS + "Checksum";

	public static final String _checksum = NS + "checksum";
	public static final String _checksumValue = NS + "checksumValue";
	public static final String _algorithm = NS + "algorithm";
	
	
	public static Resource Checksum = ResourceFactory.createResource(_Checksum);

	public static Property checksum = ResourceFactory.createProperty(_checksum);
	public static Property checksumValue = ResourceFactory.createProperty(_checksumValue);
	public static Property algorithm = ResourceFactory.createProperty(_algorithm);
}
