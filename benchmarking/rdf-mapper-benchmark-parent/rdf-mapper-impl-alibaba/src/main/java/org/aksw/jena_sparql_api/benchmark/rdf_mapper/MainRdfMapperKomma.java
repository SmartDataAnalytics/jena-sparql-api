package org.aksw.jena_sparql_api.benchmark.rdf_mapper;


import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.Book;
import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.Library;
import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.Person;
import org.aksw.jena_sparql_api.benchmark.rdf_mapper.util.ExampleModule;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IQuery;
import net.enilink.komma.core.KommaModule;
import net.enilink.komma.em.util.ISparqlConstants;

public class MainRdfMapperKomma {

	public static void main(String[] args)
			throws DatatypeConfigurationException, RepositoryException {
		
		// Amongst others, access to data can be managed with KOMMA by
		// implementations of IEntityManager. In this tutorial we create an
		// EntityManager on top of Sesame's MemoryStore.
		//
		// We have to tell this manager to use the Book and Person interfaces to
		// encapsulate access to instances and properties of Books or Persons.
		// We have to register them as Concepts.

		SailRepository dataRepository = new SailRepository(new MemoryStore());
		dataRepository.initialize();
		IEntityManager manager = createEntityManager(new ExampleModule(
				dataRepository, new KommaModule() {
					{
						addConcept(Book.class);
						addConcept(Person.class);
//						addConcept(Map.class);
//						addBehaviour(HashMap.class);
					}
				}));

		// Create a book and add some authors
		Book book = manager.createNamed(Library.NS_URI.appendFragment("book1"),
				Book.class);
		// Set properties using method chaining
		book.title("Point of No Return").dateOfRelease(getCurrentTime());

		book.authors().add(
				createPerson(manager, "person1", "Clint Eastwood", new Date()));
		book.authors().add(
				createPerson(manager, "person2", "Marty McFly", new Date()));

		// This results in the following RDF statements
		// @Prefix om: <http://enilink.net/examples/objectmapping#>
		// om:book1 rdf:type om:Book
		// om:book1 rdf:type om:Document
		// om:book1 om:dateOfRelease "..."^^xsd:datetime
		// om:book1 om:title "Point of No Return"
		// om:person1 rdf:type om:Person
		// om:person1 om:name "Clint Eastwood"
		// om:person1 om:dateOfBirth "..."
		// om:book1 om:author person1
		// om:person2 rdf:type om:Person
		// om:person2 om:name "Marty McFly"
		// om:person2 om:dateOfBirth "..."
		// om:book1 om:author person2
		//
		// Please note that KOMMA is able to handle sets, as shown by the
		// representation of authors. Sets are represented as repeated
		// properties, i.e. they are represented by multiple
		// statements in the form of (book, author, person)

		// Do some queries!
		exampleRawQuery(manager);
		System.out.println(".........");
		exampleMappedQuery(manager);
		System.out.println(".........");
		exampleRemoveObjectAndQuery(manager, book);
		System.out.println(".........");

		System.out.println("Done!");
	}

	private static IEntityManager createEntityManager(ExampleModule module) {
		Injector injector = Guice.createInjector(module);
		IEntityManagerFactory factory = injector
				.getInstance(IEntityManagerFactory.class);
		IEntityManager manager = factory.get();
		return manager;
	}

	private static Person createPerson(IEntityManager manager, String id,
			String name, Date date) {
		XMLGregorianCalendar cal = getCurrentTime();
		Person person = manager.createNamed(Library.NS_URI.appendFragment(id),
				Person.class);
		person.setName(name);
		person.setDateOfBirth(cal);
		//person.getTags().put("a", "b");
		person.setTags(new HashMap<String, String>() {{
			put("a", "b");
		}});
		// This will result in the following RDF statements

		// person rdf:type <http://enilink.net/examples/objectmapping#Person>
		// person <http://enilink.net/examples/objectmapping#name> "..."
		// person <http://enilink.net/examples/objectmapping#dateOfBirth> "..."
		return person;
	}

	private static void exampleRawQuery(IEntityManager manager) {
		// We now can query the EntityManager for some data using SPARQL.

		System.out.println("Do a raw query:");
		IQuery<?> query = manager.createQuery( //
				"PREFIX om: <" + Library.NS + ">" //
						+ "SELECT ?title ?person ?authorDateOfBirth WHERE { " //
						+ "?book om:title ?title . " //
						+ "?book om:author ?person . " //
						+ "?person om:name ?authorName . " //
						+ "?person om:dateOfBirth ?authorDateOfBirth " //
						+ "}");

		// Expected output:
		// LinkedHashBindings: {title=Point of No Return, author=..., ...}
		// LinkedHashBindings: {title=Point of No Return, author=..., ...}

		for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
			System.out.println(bindings);
		}
	}

	private static void exampleMappedQuery(IEntityManager manager) {

		// Besides querying data with SPARQL, we can also use our model
		// interfaces for encapsulating data access to properties. In this
		// function we simply select all instances of Person and print the
		// properties defined by the respective interface.

		System.out.println("Do a mapped query:");
		IQuery<?> query = manager
				.createQuery(
						ISparqlConstants.PREFIX
								+ "SELECT ?person ?clazz WHERE {?person rdf:type ?clazz}")
				.setParameter("clazz", Library.NS_URI.appendLocalPart("Person"));

		// Expected output:
		// Name: Clint Eastwood
		// Place of birth:null
		// Name: Marty McFly
		// Place of birth:null

		for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
			Person person = (Person) bindings.get("person");
			System.out.println("Name: " + person.getName());
			System.out.println("Place of birth:" + person.getPlaceOfBirth());
		}

		// Please note, because of the fact that we did not set any place of
		// birth. The respective getter returns null.
	}

	private static void exampleRemoveObjectAndQuery(IEntityManager manager,
			Book book) {

		// We delete the book and show that it is really gone.

		System.out.println("Select all books");

		IQuery<?> query = manager.createQuery(
				ISparqlConstants.PREFIX
						+ "SELECT ?book WHERE { ?book rdf:type ?clazz .  }")
				.setParameter("clazz", Library.NS_URI.appendLocalPart("Book"));

		for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
			System.out.println(bindings.get("book"));
		}
		manager.remove(book);

		System.out.println("Select all books ... again!");
		query = manager.createQuery(
				ISparqlConstants.PREFIX
						+ "SELECT ?book WHERE { ?book rdf:type ?clazz .  }")
				.setParameter("clazz", Library.NS_URI.appendLocalPart("Book"));

		// Expected output:
		// Select all books
		// http://enilink.net/examples/objectmapping#book1
		// Select all books ... again!

		for (IBindings<?> bindings : query.evaluate(IBindings.class)) {
			System.out.println(bindings.get("book"));
		}
	}

	private static XMLGregorianCalendar getCurrentTime() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar cal = null;
		try {
			cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
		return cal;
	}

}