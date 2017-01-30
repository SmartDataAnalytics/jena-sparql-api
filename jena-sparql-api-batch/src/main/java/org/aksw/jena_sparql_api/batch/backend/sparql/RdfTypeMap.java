package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.context.TypedNode;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeComplexBase;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.ModelUtils;

public class RdfTypeMap
    //extends RdfPopulatorPropertyBase
    extends RdfTypeComplexBase
{
    public static final Property entry = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/entry");
    public static final Property key = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/key");
    public static final Property value = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/value");

    public static final Property keyClass = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/keyClass");
    public static final Property valueClass = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/valueClass");

    //protected MapOps mapOps;
    protected Function<Object, Map> createMapView;

    // , PropertyOps propertyOps, Node predicate, RdfType targetRdfType
    public RdfTypeMap(Function<Object, Map> createMapView) {
        ///super(typeFactory);
        this.createMapView = createMapView;
    }


    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        ResourceShapeBuilder tmp = shapeBuilder.out(entry.asNode());

        tmp.out(key.asNode());
        tmp.out(value.asNode());
    }

    @Override
    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject,
            Graph shapeGraph, Consumer<Triple> sink) {

        @SuppressWarnings("unchecked")
        Map<? super Object, ? super Object> map = createMapView.apply(entity);


        int i = 1;
        for(Entry<?, ?> e : map.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();

            Node eNode = NodeFactory.createURI(subject.getURI() + "-" + i);

            //persistenceContext.
            //persistenceContext.entityFor(new TypedNode(rdfType, node));


//            Node kNode = null; // emitterContext.getValueNode(entity, propertyName)//RdfPersistenceContextImpl.getOrCreateRootNode(persistenceContext, typeFactory, k);
//            Node vNode = null; //RdfPersistenceContextImpl.getOrCreateRootNode(persistenceContext, typeFactory, v);

//            emitterContext.add(k, entity, "key" + i);
//            emitterContext.add(v, entity, "value" + i);

            //Node keyNode = emitterContext.

            //Node kNode = emitterContext.getValueNode(entity, "key" + i, v);
            //Node vNode = emitterContext.getValueNode(entity, "value" + i, v);

            Node kNode = emitterContext.requestResolution(k);
            Node vNode = emitterContext.requestResolution(v);

            sink.accept(new Triple(subject, entry.asNode(), eNode));
            sink.accept(new Triple(eNode, key.asNode(), kNode));
            sink.accept(new Triple(eNode, value.asNode(), vNode));

            ++i;
        }

    }

    @Override
    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Node subject, Graph graph, Consumer<Triple> outSink) {
        Model model = ModelFactory.createModelForGraph(graph);
        RDFNode root = ModelUtils.convertGraphNodeToRDFNode(subject, model);

        // <Object, Object>
        Map map = createMapView.apply(entity);


        for(Statement stmt : root.asResource().listProperties(entry).toList()) {
            Resource e = stmt.getObject().asResource();

            Node kNode = e.getProperty(key).getObject().asNode();
            Node vNode = e.getProperty(value).getObject().asNode();


            // TODO: We need to dynamically figure out which entity the node could be
            RdfType rdfType = null;
            Object k = persistenceContext.entityFor(Object.class, kNode, null);//new TypedNode(rdfType, kNode));
            Object v = persistenceContext.entityFor(Object.class, vNode, null);//new TypedNode(rdfType, vNode));

            map.put(k, v);
        }
    }

    @Override
    public Class<?> getEntityClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getRootNode(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Object createJavaObject(Node node, Graph graph) {
        @SuppressWarnings("rawtypes")
        Map result = new HashMap();
        return result;
    }


    @Override
    public boolean hasIdentity() {
        // TODO Auto-generated method stub
        return false;
    }

//    @Override
//    public Object createJavaObject(Node node) {
//        //entityOps.
//        // TODO Auto-generated method stub
//        return null;
//    }

}
