package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterators;


class CollectionFromIterable<T>
	extends AbstractCollection<T>
{
	protected Iterable<T> iterable;
	
	public CollectionFromIterable(Iterable<T> iterable) {
		super();
		this.iterable = iterable;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = iterable.iterator();
		return result;
	}

	@Override
	public int size() {
		int result = Iterators.size(iterator());
		return result;
	}
	
	@Override
	public String toString() {
		String result = Iterables.toString(iterable);
		return result;
	}
	
	public static <T> CollectionFromIterable<T> create(Iterable<T> iterable) {
		return new CollectionFromIterable<>(iterable);
	}
}

public class TestDynamicViews {

	public static final Property p = ResourceFactory.createProperty("http://foobar");
	
	public static final List<Class<?>> classes = Arrays.asList(Car.class, Vessel.class, Motorboat.class, Sailboat.class, SailingYacht.class,
			Port.class);

	@RdfType
	@ResourceView
	public static interface Car
		extends Resource
	{
		
	}

	@RdfType
	@ResourceView
	public static interface Vessel
		extends Resource
	{
		
	}

	@RdfType
	@ResourceView
	public static interface Motorboat
		extends Vessel
	{
	}

	
	@RdfType
	@ResourceView
	public static interface Sailboat
		extends Vessel
	{
	}
	
	@RdfType
	@ResourceView
	public static interface SailingYacht
		extends Motorboat, Sailboat
	{
	}
	
	@ResourceView
	public static class Port
		extends ResourceImpl
	{		
	    public Port(Node n, EnhGraph m) {
	        super( n, m );
	    }

		
		//@IriNs("eg")
		public <T extends Vessel> Collection<T> getVessels(Class<T> clazz) {
			//TypeDecider typeDecider = new TypeDeciderImpl().registerClasses(classes);
			TypeDecider typeDecider = JenaPluginUtils.getTypeDecider();
			
			Collection<T> it = CollectionFromIterable.create(() -> ResourceUtils.listPropertyValues(this, p, clazz, typeDecider));
			return it;
		}
	}
	
	@Test
	public void testDynamicSetViewsWithTypes() {
		//JenaSystem.init();
		
		// TODO Guava only scans top level classes...
		//JenaPluginUtils.scan(TestDynamicViews.class);
		
		JenaPluginUtils.registerResourceClasses(classes);

		
		
//		TypeDecider typeDecider = new TypeDeciderImpl().scan(TestDynamicViews.class);
		
		Model m = ModelFactory.createDefaultModel();
		
		Motorboat mb1 = m.createResource("x:mb1").as(Motorboat.class);
		Motorboat mb2 = m.createResource("x:mb2").as(Motorboat.class);
		Motorboat mb3 = m.createResource("x:mb3").as(Motorboat.class);

		Sailboat sb1 = m.createResource("x:sb1").as(Sailboat.class);
		Sailboat sb2 = m.createResource("x:sb2").as(Sailboat.class);
		
		SailingYacht sy1 = m.createResource("x:sy1").as(SailingYacht.class);
		
		Port port = m.createResource("x:port").as(Port.class);

		Collection<Resource> vessels = Arrays.asList(mb1, mb2, mb3, sb1, sb2, sy1);
//		Collection<Resource> vessels = Collections.emptySet();
		for(Resource vessel : vessels) {
			port.addProperty(p, vessel);
		}

		Assert.assertEquals(6, port.getVessels(Vessel.class).size());

		// Remove the sailing yacht
		port.getVessels(SailingYacht.class).clear();

		Assert.assertEquals(5, port.getVessels(Vessel.class).size());

		
		System.out.println("Vessels: " + port.getVessels(Vessel.class));
		System.out.println("Motorboats: " + port.getVessels(Motorboat.class));
		System.out.println("Sailboats: " + port.getVessels(Sailboat.class));
		System.out.println("Sailing Yachts: " + port.getVessels(SailingYacht.class));

		RDFDataMgr.write(System.out, port.getModel(), RDFFormat.TURTLE_PRETTY);
		
		
		// Adding a superclass type should not add another type
		
		{
			List<RDFNode> types = ResourceUtils.listPropertyValues(sy1.as(Motorboat.class), RDF.type).toList();
			System.out.println("Sailying yacht types after viewing as super class: " + types);
			Assert.assertEquals(1, types.size());
		}

		{
			List<RDFNode> types = ResourceUtils.listPropertyValues(sy1.as(Car.class), RDF.type).toList();
			System.out.println("Sailying yacht types after viewing as unrelated class: " + types);

			Assert.assertEquals(2, types.size());
		}

	}
	

}
