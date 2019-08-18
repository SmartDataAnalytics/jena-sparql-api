package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.commons.collections.CollectionFromIterable;
import org.aksw.commons.collections.sets.SetFromCollection;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromRDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMapperImpl;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
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
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.google.common.base.Converter;


public class TestDynamicViews {

	public static final Property p = ResourceFactory.createProperty("http://foobar");
	
	public static final List<Class<?>> classes = Arrays.asList(
			Port.class, Car.class, Vessel.class, Motorboat.class, Sailboat.class, SailingYacht.class);

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
	public static abstract class Port
		extends ResourceImpl
	{		
	    public Port(Node n, EnhGraph m) {
	        super( n, m );
	    }

	    @IriNs("eg")
	    public abstract void setObject(Object o);
	    public abstract Object getObject();
	    
	    
	    @IriNs("eg")
	    public abstract Vessel getBiggestVessel();
	   
	    public abstract void setBiggestVessel(Vessel vessel);

	    
	    public Vessel getBiggestVesselAdvanced() {
	    	Vessel result = ResourceUtils.getPropertyValue(this, ResourceFactory.createProperty("http://www.example.org/biggestVessel"), RDFNodeMappers.from(Vessel.class, JenaPluginUtils.getTypeDecider()));
	    	return result;
	    }

	    
		//@IriNs("eg")
		public <T extends Vessel> Collection<T> getVessels(Class<T> clazz) {
			//TypeDecider typeDecider = new TypeDeciderImpl().registerClasses(classes);
			TypeDecider typeDecider = JenaPluginUtils.getTypeDecider();
			
			Collection<T> it = CollectionFromIterable.create(() -> ResourceUtils.listPropertyValues(this, p, RDFNodeMappers.from(clazz, typeDecider)));
			return it;
		}
	}

	@Test
	public void testTypeDecider() {
		
		List<Integer> b = Lists.newArrayList(1, 1, 2, 2, 2, 3, 3, 3);
		Set<Integer> test = new SetFromCollection<>(b);
		System.out.println(test + " " + test.size() + " " + b);
		
		test.remove(1);
		System.out.println(test + " " + test.size() + " " + b);

		Iterator<Integer> it = test.iterator();
		it.next();
		it.remove();
		System.out.println(test + " " + test.size() + " " + b);
		
		
		JenaPluginUtils.registerResourceClasses(classes);
		
//		TypeDecider typeDecider = new TypeDeciderImpl().scan(TestDynamicViews.class);
		
		Model m = ModelFactory.createDefaultModel();
		
		Resource root = m.createResource()
			.addLiteral(RDFS.label, 1)
			.addLiteral(RDFS.label, 2.0)
			.addProperty(RDFS.label, m.createResource("x:mb1").as(Motorboat.class))
			.addProperty(RDFS.label, m.createResource("x:sb1").as(Sailboat.class))
			.addProperty(RDFS.label, m.createResource("x:sy1").as(SailingYacht.class));

		TypeMapper typeMapper = TypeMapper.getInstance();
		TypeDecider typeDecider = JenaPluginUtils.getTypeDecider();
		
		RDFNodeMapper<?> mapper = new RDFNodeMapperImpl<>(Long.class, typeMapper, typeDecider);

		Collection<RDFNode> backend = new SetFromPropertyValues<RDFNode>(root, RDFS.label, RDFNode.class);
		
		
		Converter<RDFNode, ?> converter = new ConverterFromRDFNodeMapper<>(mapper);
		Collection<?> col = new CollectionFromConverter<>(backend, converter);

		for(Object o : col) {
			System.out.println(o + " -> " + o.getClass());
		}
		
		
//		ResourceUtils.listProperties(root, RDFS.label, )
		

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
		
		
		port.setBiggestVessel(sy1);


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

		
		System.out.println("Biggest vessel: " + port.getBiggestVessel() + " " + SailingYacht.class.isAssignableFrom(port.getBiggestVessel().getClass()));
		System.out.println("Biggest vessel: " + port.getBiggestVesselAdvanced() + " " + SailingYacht.class.isAssignableFrom(port.getBiggestVesselAdvanced().getClass()));
		
		
		Port port2 = ModelFactory.createDefaultModel().createResource().as(Port.class);
		port2.setObject(1);
		port2.setObject(2);
		RDFDataMgr.write(System.out, port2.getModel(), RDFFormat.TURTLE_PRETTY);
		
		
	}
	

}
