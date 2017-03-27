package org.aksw.jena_sparql_api.benchmark.rdf_mapper.model;

import java.util.Set;

import net.enilink.composition.annotations.Iri;


@Iri(Library.NS + "Book")
public interface Book extends Document {

	@Iri(Library.NS + "author")
	Set<Person> authors();

	Document authors(Set<Person> persons);

}
