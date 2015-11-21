package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.model.RdfProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.proxy.MethodInterceptorRdf;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;

import com.google.common.base.Function;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * An RdfClass is one type of implementation that can map Java objects to and from RDF graphs.
 *
 */
public class RdfClass
    extends RdfTypeBase
{
    // TODO: Add type parameters

    /**
     * The affected class (maybe we should use the fully qualified class name instead?)
     */
    protected Class<?> targetClass;

    /**
     * The concept that captures rdf terms that are instances of this class.
     * In the simplest case this is { ?s | ?s a SomeType }
     *
     */
    protected Concept concept;

    /**
     * Function that emits triples that should be emitted for a given object
     *
     * This should yield all triples needed to satisfy the concept.
     */
    protected Function<Object, Graph> enforcedTriples;


    /**
     * A function for obtaining a default IRI from an object
     */
    protected Function<Object, String> defaultIriFn;

    protected Prologue prologue;

    protected Map<String, RdfProperty> propertyToMapping;

    protected boolean isPopulated;

    public RdfClass(RdfTypeFactory typeFactory, Class<?> targetClass, Function<Object, String> defaultIriFn, Prologue prologue) {
        this(typeFactory, targetClass, defaultIriFn, prologue, new LinkedHashMap<String, RdfProperty>());
    }

    public Collection<RdfProperty> getRdfProperties() {
        Collection<RdfProperty> result = propertyToMapping.values();
        return result;
    }

    public void setPopulated(boolean isPopulated) {
        this.isPopulated = isPopulated;
    }

    /**
     * Whether this RdfClass instance is fully initialized.
     *
     * @return
     */
    public boolean isPopulated() {
        return isPopulated;
    }

    public RdfClass(RdfTypeFactory typeFactory, Class<?> targetClass, Function<Object, String> defaultIriFn, Prologue prologue, Map<String, RdfProperty> propertyToMapping) {
        super(typeFactory);
        this.targetClass = targetClass;
        this.defaultIriFn = defaultIriFn;
        this.prologue = prologue;
        this.propertyToMapping = propertyToMapping;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public void build(ResourceShapeBuilder builder) {
        for(RdfProperty p : propertyToMapping.values()) {
            builder.outgoing(p.getRelation());
        }
    }

    public MappedConcept<DatasetGraph> getMappedQuery() {
        ResourceShapeBuilder builder = new ResourceShapeBuilder(prologue);


        for(RdfProperty p : propertyToMapping.values()) {
            builder.outgoing(p.getRelation());
        }

        ResourceShape shape = builder.getResourceShape();
        MappedConcept<DatasetGraph> result = ResourceShape.createMappedConcept2(shape, null);
        return result;
    }

    public static MethodInterceptorRdf getMethodInterceptor(Object o) {
        MethodInterceptorRdf result = null;

        if(o != null && Enhancer.isEnhanced(o.getClass())) {
            Factory factory = (Factory)o;
            Callback callback = factory.getCallback(0);
            result = callback != null && callback instanceof MethodInterceptorRdf
                    ? (MethodInterceptorRdf)callback : null;
        }

        return result;
    }

    /**
     * Returns the subject of a given object or null if not present.
     * First the object is checked for whether it is a proxy referring to a prior subject, which is returned if present.
     * Otherwise, a default iri will be generated.
     *
     * @param o
     * @return
     */
    @Override
    public Node getRootNode(Object o) {
        MethodInterceptorRdf m = getMethodInterceptor(o);
        Node result = m != null ? m.getPresetSubject() : null;

        if(result == null) {
            String str = defaultIriFn != null ? defaultIriFn.apply(o) : null;
            str = prologue.getPrefixMapping().expandPrefix(str);

            result = str != null ? NodeFactory.createURI(str) : null;
        }

        return result;
    }


    public Object toJava(Node node) {
        Object result;
        if(node == null) {
            result = null;
        } else if(node.isURI()) {
            result = node.getURI();
        } else if(node.isLiteral()) {
            result = node.getLiteralValue();
        } else { //if(node.isBlank()) {
            throw new RuntimeException("not supported (yet)");
        }

        return result;
    }


    /**
     * Set property values of the given target object based a DatasetGraph.
     *
     * @param targetObj
     * @param datasetGraph
     */
    @Override
    public void setValues(Object targetObj, DatasetGraph datasetGraph) {
        PrefixMapping prefixMapping = prologue.getPrefixMapping();

        DatasetGraph result = DatasetGraphFactory.createMem();

        Graph graph = result.getDefaultGraph();

        Collection<RdfProperty> rdfProperties = propertyToMapping.values();

        BeanWrapper bean = new BeanWrapperImpl(targetObj);

        Node s = getRootNode(targetObj);
        //TypeMapper typeMapper = TypeMapper.getInstance();

        for(RdfProperty pd : rdfProperties) {
            //String propertyName = pd.getName();
            //Object propertyValue = bean.getPropertyValue(propertyName);


            pd.readPropertyValue(graph, s);

//            System.out.println("Value of " + propertyName + " = " + propertyValue);
//
//            Relation relation = pd.getRelation();
//            Triple t = RelationUtils.extractTriple(relation);
//            if(t != null) {
//                Node pRaw = t.getPredicate();
//                if(Node.ANY.equals(pRaw)) {
//                    throw new RuntimeException("Could not obtain a valid RDF property for bean property " + propertyName + " with value " + propertyValue);
//                }
//
//                String pStr = prefixMapping.expandPrefix(pRaw.getURI());
//                Node p = NodeFactory.createURI(pStr);
//
//                // Get the value of the triple
//                List<Node> os = ListObjectsOfDatasetGraph.create(datasetGraph, Node.ANY, s, p);
//                Node o = os.isEmpty() ? null : os.iterator().next();
//
//                // Convert o to java
//                Object java = toJava(o);
//
//                bean.setPropertyValue(propertyName, java);
//            }
        }
    }

    //writeGraph(Object obj, Node g, )

    /**
     * Extract triples for a given object in the specified target graph.
     *
     * @param obj
     * @param g
     * @return
     */
    @Override
    public void writeGraph(Graph out, Object obj) {
        PrefixMapping prefixMapping = prologue.getPrefixMapping();

        //DatasetGraph result = DatasetGraphFactory.createMem();

        //Graph graph = result.getGraph(g);

        Collection<RdfProperty> rdfProperties = propertyToMapping.values();

        BeanWrapper bean = new BeanWrapperImpl(obj);

        Node s = getRootNode(obj);

        for(RdfProperty pd : rdfProperties) {
            String propertyName = pd.getName();
            Object propertyValue = bean.getPropertyValue(propertyName);

            if(propertyValue != null) {

                Class<?> propertyClass = propertyValue.getClass();

                System.out.println("Value of " + propertyName + " = " + propertyValue);

                pd.writePropertyValue(out, obj, s);

                if(false) {
                    Relation relation = pd.getRelation();
                    Triple t = RelationUtils.extractTriple(relation);
                    if(t != null) {
                        Node pRaw = t.getPredicate();
                        if(Node.ANY.equals(pRaw)) {
                            throw new RuntimeException("Could not obtain a valid RDF property for bean property " + propertyName + " with value " + propertyValue);
                        }

                        String pStr = prefixMapping.expandPrefix(pRaw.getURI());
                        Node p = NodeFactory.createURI(pStr);

                        TypeMapper typeMapper = TypeMapper.getInstance();
                        RDFDatatype datatype = typeMapper.getTypeByClass(propertyClass);
                        String lex = datatype.unparse(propertyValue);
                        Node o = NodeFactory.createLiteral(lex, datatype);

                        //datasetDescription.getDefaultGraphURIs()
                        //Quad quad = new Quad(g, s, p, o);
                        //out.add(quad);
                        Triple triple = new Triple(s, p, o);
                        out.add(triple);

                        // TODO Now apply lang filtering

                        //int i = 0;

                        //Node o = rep;
                    }
                }
            }
        }

        //return result;
    }



    /**
     * Create a proxied instance of the class based on the given graph
     *
     * @deprecated Use createJavaObject first, and initialize values from a datasetGraph using a separate call to one of the methods of this class
     * TODO which method?
     *
     * @param datasetGraph
     * @return
     */
    @Deprecated
    public Object createProxy(DatasetGraph datasetGraph, Node subject) {

        Object o;
        try {
            o = targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MethodInterceptorRdf interceptor = new MethodInterceptorRdf(o, this, subject, datasetGraph);
        //new Class<?>[] { ProxiedRdf.class }
//        Object result = Enhancer.create(targetClass, null, interceptor);
        Object result = Enhancer.create(targetClass, null, interceptor);

        return result;
    }

    @Override
    public Object createJavaObject(Node subject) {
        Object o;
        try {
            o = targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MethodInterceptorRdf interceptor = new MethodInterceptorRdf(o, this, subject);
        Object result = Enhancer.create(targetClass, null, interceptor);

        return result;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

}
