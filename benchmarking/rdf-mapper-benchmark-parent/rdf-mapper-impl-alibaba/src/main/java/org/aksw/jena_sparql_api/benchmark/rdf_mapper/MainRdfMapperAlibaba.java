package org.aksw.jena_sparql_api.benchmark.rdf_mapper;


import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.Book;
import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.BookImpl;
import org.aksw.jena_sparql_api.benchmark.rdf_mapper.model.Document;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MainRdfMapperAlibaba {
	public static void main(String[] args) throws Exception {
		// create a repository
		Repository store = new SailRepository(new MemoryStore());
		store.initialize();

		// wrap in an object repository
		ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
		ObjectRepository repository = factory.createRepository(store);

		// create a Document
		Document doc = new BookImpl();
		doc.title("Getting Started");

		// add a Document to the repository
		ObjectConnection con = repository.getConnection();
		ValueFactory vf = con.getValueFactory();
		URI id = vf.createURI("http://example.com/data/2012/getting-started");
		con.addObject(id, doc);

		// retrieve a Document by id
		Object foo = con.getObject(Document.class, id);
		System.out.println(foo);

		// remove a Document from the repository
		doc = con.getObject(Book.class, id);
		doc.title(null);
		con.removeDesignation(doc, Book.class);

		// close everything down
		con.close();
	}
}
