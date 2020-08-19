package org.aksw.jena_sparql_api.mapper.proxy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.sets.SetFromCollection;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.PolymorphicOnly;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromRDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.google.common.base.Converter;


/**
 * Test cases for the annotation-driven Resource proxy generation.
 * These tests making use of the simple port / vessels model
 * with the following type hierarchy:
 *
 *
 *        Vessel          Port     Car
 *    /           \
 * Sailboat    Motorboat
 *    \           /
 *    Sailing yacht
 *
 * @author raven
 *
 */
public class TestDynamicRDFNodeViews {

    // Workaround for Guava's package scanning not picking up nested classes
    // For the sake of keeping this demonstration self contained we enumerate these classes
    // here.
    public static final List<Class<?>> classes = Arrays.asList(
            Port.class, Vessel.class, Motorboat.class, Sailboat.class, SailingYacht.class, Car.class);

    @ResourceView
    public static interface Vessel
        extends Resource
    {
    }

    // The @RdfType annotation indicates to generate an appropriate rdf:type whenever
    // rdfNode.as(View.class) is invoked.
    // @ResourceView indicates that the interface/class is subject to classpath scanning
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

    @RdfType
    @ResourceView
    public static interface Car
        extends Resource
    {
    }


    /**
     *
     * @author raven
     *
     */
    @RdfType("http://www.example.org/Port")
    @ResourceView
    public static abstract class Port
        extends ResourceImpl
    {
        public Port(Node n, EnhGraph m) {
            super( n, m );
        }

        @IriNs("eg")
        public abstract Vessel getBiggestVessel();
        public abstract void setBiggestVessel(Vessel vessel);

        // PolymorphicOnly: Yield a collection that only exposes those resources that are known subclasses
        // of Vessel - no views are requested or returned for resources with other or unknown types.
        // Example: With @PolymorphicOnly, getVessel(Motorboat.class) will only return known instances of Motorboat and its subclasses.
        //          Without @PolymorphicOnly, getVessel(Motorboat.class) will in addition return any other resource using a Motorboat view.
        @IriNs("eg")
        @PolymorphicOnly
        public abstract <T extends Vessel> Collection<T> getVessels(Class<T> clazz);

        // Test case for setting/getting an arbitrary object
        @IriNs("eg")
        public abstract void setObject(Object o);
        public abstract Object getObject();

        //@IriNs("eg")
//		public <T extends Vessel> Collection<T> getVessels(Class<T> clazz) {
//			//TypeDecider typeDecider = new TypeDeciderImpl().registerClasses(classes);
//			TypeDecider typeDecider = JenaPluginUtils.getTypeDecider();
//
//			Collection<T> it = CollectionFromIterable.create(() -> ResourceUtils.listPropertyValues(this, p, RDFNodeMappers.from(clazz, typeDecider)));
//			return it;
//		}
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

        RDFNodeMapper<?> mapper = RDFNodeMappers.from(Long.class, typeMapper, typeDecider, true, false);

        Collection<RDFNode> backend = new SetFromPropertyValues<RDFNode>(root, RDFS.label, RDFNode.class);


        Converter<RDFNode, ?> converter = new ConverterFromRDFNodeMapper<>(mapper);
        Collection<?> col = ConvertingCollection.createSafe(backend, converter);

        for(Object o : col) {
            System.out.println(o + " -> " + o.getClass());
        }
    }

    @Test
    public void testDynamicSetViewsWithTypes() {
        // Register the view classes
        JenaPluginUtils.registerResourceClasses(classes);

        Model m = ModelFactory.createDefaultModel();

        Motorboat mb1 = m.createResource("x:mb1").as(Motorboat.class);
        Motorboat mb2 = m.createResource("x:mb2").as(Motorboat.class);
        Motorboat mb3 = m.createResource("x:mb3").as(Motorboat.class);

        Sailboat sb1 = m.createResource("x:sb1").as(Sailboat.class);
        Sailboat sb2 = m.createResource("x:sb2").as(Sailboat.class);

        SailingYacht sy1 = m.createResource("x:sy1").as(SailingYacht.class);

        Port port = m.createResource("x:port").as(Port.class);

        // Port has a custom @RdfType annotation - check that an RDF type was generated by
        // requesting the Port.class view
        Assert.assertEquals("http://www.example.org/Port",
                port.getPropertyResourceValue(RDF.type).getURI());


        Collection<Vessel> vessels = Arrays.asList(mb1, mb2, mb3, sb1, sb2, sy1);
        port.getVessels(Vessel.class).addAll(vessels);

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


        // Ensure that adding a superclass w.r.t. the TypeDecider type does not add a subsumed type
        {
            List<RDFNode> types = ResourceUtils.listPropertyValues(sy1.as(Motorboat.class), RDF.type).toList();
            System.out.println("Sailying yacht types after viewing as super class: " + types);
            Assert.assertEquals(1, types.size());
        }


        // Requesting a view with an unrelated type however adds that type
        {
            List<RDFNode> types = ResourceUtils.listPropertyValues(sy1.as(Car.class), RDF.type).toList();
            System.out.println("Sailying yacht types after viewing as unrelated class: " + types);

            Assert.assertEquals(2, types.size());
        }


        System.out.println("Biggest vessel: " + port.getBiggestVessel() + " " + SailingYacht.class.isAssignableFrom(port.getBiggestVessel().getClass()));


        Port port2 = ModelFactory.createDefaultModel().createResource().as(Port.class);
        port2.setObject(1);
        Assert.assertEquals(1, port2.getObject());

        port2.setObject(2l);
        // ISSUE Jena's Node.getLiteralValue() may narrow the type (e.g. a Longs may be converter to Int)
        Assert.assertEquals(2, port2.getObject());
        System.out.println("Object is: " + port2.getObject());

        // This only adds the sy1 resource to port2's model.
        // However, no triples are copied, hence sy1's type information is lost
        port2.setObject(sy1);
        Assert.assertFalse(port2.getObject() instanceof SailingYacht);

        // Add the sy1 to port2's model and generate the rdf:type triples
        // by requesting the SailingYacht.class view
        port2.setObject(sy1.inModel(port2.getModel()).as(SailingYacht.class));
        Assert.assertTrue(port2.getObject() instanceof SailingYacht);

        RDFDataMgr.write(System.out, port2.getModel(), RDFFormat.TURTLE_PRETTY);
        System.out.println("Object is: " + port2.getObject());
        Assert.assertEquals(sy1, port2.getObject());
    }


}
