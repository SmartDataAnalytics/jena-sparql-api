package org.aksw.jena_sparql_api.benchmark.rdf_mapper.model;

import javax.xml.datatype.XMLGregorianCalendar;

import net.enilink.composition.annotations.Iri;

@Iri(Library.NS + "Document")
public interface Document {

	@Iri(Library.NS + "dateOfRelease")
	XMLGregorianCalendar dateOfRelease();

	Document dateOfRelease(XMLGregorianCalendar dateOfRelease);

	@Iri(Library.NS + "title")
	String title();

	Document title(String title);

}
