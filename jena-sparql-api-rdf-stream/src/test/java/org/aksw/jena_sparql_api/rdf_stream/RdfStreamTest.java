package org.aksw.jena_sparql_api.rdf_stream;

import java.util.Arrays;
import java.util.Set;

import org.aksw.jena_sparql_api.rdf_stream.core.RdfStream;
import org.aksw.jena_sparql_api.rdf_stream.enhanced.ModelFactoryEnh;
import org.aksw.jena_sparql_api.rdf_stream.enhanced.ResourceEnh;
import org.aksw.jena_sparql_api.vocabs.IV;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class RdfStreamTest {


	public static void main(String[] args) {
		// The primary problem that needs to be solved is how Java objects can be attached to resources
		// without the overhead of requiring (RDF) serialization / deserialization

		// One ugly way could be to do something like
		// (Resource, Map<Resource, Object>)

		// The task executors, which are just bi-consumers of (Resource, T)

		// The Resource::as() mechanism only works reliable when all state is stored to and read from the graph

		// It would be possible to temporarily attach information to a resource
		// This could be sufficient for the use case.



		//GraphFactory.createDefaultGraph()
		//EnhGraph g = new EnhGraph(g, p)

		Model m = ModelFactoryEnh.createModel();

		for(int i = 0; i < 3; ++i) {
			Resource r = m.createResource("http://foo.bar/baz#" + i);
			r.addProperty(RDF.type, OWL.Class);
			ResourceEnh p = r.as(ResourceEnh.class);
			p.addTrait("Test " + i);
		}

		for(int i = 0; i < 10; ++i) {
			Resource r = m.createResource("http://foo.bar/baz#" + i);
			ResourceEnh p = r.as(ResourceEnh.class);
			System.out.println(p.getTrait(String.class).orNull());
		}

		ResourceEnh y = m.getResource("http://foo.bar/baz#1").as(ResourceEnh.class).rename("http://example.org/foo");
		//ResourceUtils.renameResource(m.getResource("http://foo.bar/baz#1"), "http://example.org/foo");
		//ResourceEnh y = m.getResource("http://example.org/foo").as(ResourceEnh.class);
		System.out.println(y.getTrait(String.class));



//		Function<Stream<ResourceEnh>, Stream<ResourceEnh>> fn =
//			repeat(5)
		//Function<Supplier<Stream<Resource>>, Supplier<Stream<Resource>>> wf =
		Set<Resource> items = m.listSubjectsWithProperty(RDF.type, OWL.Class).toSet();//mapWith(r -> r.as(ResourceEnh.class)).toSet();

		// Copy original resource into a snapshot model
		String template = "http://ex.org/{0}/{1}/{2}";

		RdfStream.start()
			// Allocate a new resource
			.map(r -> r.getModel().createResource().addProperty(RDFS.seeAlso, r))
			.withIndex(IV.item) // item index, always 1
			.repeat(2)
			.withIndex(IV.run) // counts to 2 per item
			//repeat(3))
			.repeatForLiterals(RDFS.label, Arrays.asList("a", "b", "c").stream())
			.withIndex(IV.experiment) // counts to 6
			.peek(r -> { if(r.getProperty(IV.run).getInt() < 3) { r.addProperty(RDF.type, RDFS.Class); }})
			.map(r -> r.as(ResourceEnh.class).rename(template, RDFS.label, IV.run, "x-" + r.getProperty(IV.item).getInt()))
			//map(r ->))
			.apply(() -> items.stream()).get()
			//.map(r -> r.as(ResourceEnh.class))
			//.forEach(r -> r.getModel().write(System.out, "TURTLE"));
			.forEach(System.out::println);


		//repeat()
	}
}
