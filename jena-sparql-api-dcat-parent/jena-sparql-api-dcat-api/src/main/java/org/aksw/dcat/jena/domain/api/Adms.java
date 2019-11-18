package org.aksw.dcat.jena.domain.api;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class Adms {
	public static final String NS = "http://www.w3.org/ns/adms#";
	
	public static final String _identifier = NS + "identifier";
	public static final String _versionNotes = NS + "versionNotes";
	public static final String _sample = NS + "sample";
	public static final String _status = NS + "status";
	
	public static final Property identifier = ResourceFactory.createProperty(_identifier);
	public static final Property versionNotes = ResourceFactory.createProperty(_versionNotes);
	public static final Property sample = ResourceFactory.createProperty(_sample);
	public static final Property status = ResourceFactory.createProperty(_sample);
	
	// identifier -> adms:Identifier
	// versionNotes - literal
	// sample -> adms:asset
	// status -> skos:Concept
}
