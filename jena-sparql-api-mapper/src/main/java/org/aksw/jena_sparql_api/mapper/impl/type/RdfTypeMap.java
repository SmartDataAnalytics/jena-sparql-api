package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

/**
 * Issue: How to give property access to key/value attributes of the entries?
 * JPA models map-typed attributes using MapAttribute which derives from
 * PluralAttribute A map can be modeled as a set (collection) of entry objects.
 * Under this view, a Map is a collection attribute, which has an item type of
 * Entry. But then again, this is not how JPA does it: A map is a
 * PluralAttribute, which in addition to the value type also has a key type - so
 * its not a collection of entries, but a collection of values with an
 * additional key type.
 *
 * The essence is, that it should be possible to resolve access to 'key' and
 * 'value' attributes
 *
 *
 *
 *
 * @author raven
 *
 */
public class RdfTypeMap
        // extends RdfPopulatorPropertyBase
        extends RdfTypeComplexBase {
    public static final Property entry = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/entry");
    public static final Property key = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/key");
    public static final Property value = ResourceFactory.createProperty("http://jsa.aksw.org/ontology/value");

    // public static final Property keyClass =
    // ResourceFactory.createProperty("http://jsa.aksw.org/ontology/keyClass");
    // public static final Property valueClass =
    // ResourceFactory.createProperty("http://jsa.aksw.org/ontology/valueClass");

    protected RdfType keyRdfType;
    protected RdfType valueRdfType;
    protected Class<?> keyClazz;
    protected Class<?> valueClazz;

    // protected MapOps mapOps;
    protected Function<Object, Map> createMapView;

    // , PropertyOps propertyOps, Node predicate, RdfType targetRdfType

    // public RdfTypeMap(
    // Function<Object, Map> createMapView)
    // {
    // this(createMapView, Object.class, Object.class);
    // }

    public RdfTypeMap(Function<Object, Map> createMapView) {
        this(createMapView, Object.class, Object.class);
    }

    public RdfTypeMap(Function<Object, Map> createMapView, RdfType keyRdfType, RdfType valueRdfType
    // Class<?> keyClazz,
    // Class<?> valueClazz
    ) {
        /// super(typeFactory);
        this.createMapView = createMapView;
        this.keyRdfType = keyRdfType;
        this.valueRdfType = valueRdfType;
        // this.keyClazz = keyClazz;
        // this.valueClazz = valueClazz;
    }

    public RdfTypeMap(Function<Object, Map> createMapView, Class<?> keyClazz, Class<?> valueClazz) {
        /// super(typeFactory);
        this.createMapView = createMapView;
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder shapeBuilder) {
        ResourceShapeBuilder tmp = shapeBuilder.out(entry.asNode());

        tmp.out(key.asNode());
        tmp.out(value.asNode());
    }

    // @Override
    // public void emitTriples(RdfEmitterContext emitterContext, Object entity,
    // Node subject,
    // Graph shapeGraph, Consumer<Triple> sink) {

    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
        @SuppressWarnings("unchecked")
        Map<? super Object, ? super Object> map = createMapView.apply(entity);

        int i = 1;
        for (Entry<?, ?> e : map.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();

            Resource subject = out.getResource();
            Model m = subject.getModel();

            Resource r = m.createResource(priorState.getURI() + "-" + i);

            Resource kNode = m.createResource();
            Resource vNode = m.createResource();

            subject.addProperty(entry, r);

            r.addProperty(key, kNode).addProperty(value, vNode);

            // ValueHolder vh = new ValueHolderImpl(
            // () -> map.get(k),
            // x -> map.put(k, x)
            // );

            out.getPlaceholders().put(kNode, new PlaceholderInfo(keyClazz, null, entity, null, null, k, null, null));
            out.getPlaceholders().put(vNode, new PlaceholderInfo(valueClazz, null, entity, null, null, v, null, null));

            ++i;
        }

    }

    /**
     * The fragment will contain information about which nodes need to be
     * resolved. Once everything is resolved, there needs to be a function that
     * carries out the actualy population - so its more like
     *
     * Populator populator = exposePopulator(shape, entity) // Maybe the entity
     * is not needed at this stage
     *
     * populator.refs.forEach((key, class, node) -> context.put(key,
     * rdfMapperEngine.resolve(class, node))) populator.resolve(context)
     *
     *
     */
    @Override
    public EntityFragment populate(Resource shape, Object entity) {

        // <Object, Object>
        Map<Object, Object> map = createMapView.apply(entity);

        EntityFragment result = new EntityFragment(entity);
        for (Statement stmt : shape.listProperties(entry).toList()) {
            Resource e = stmt.getObject().asResource();

            Statement kStmt = e.getProperty(key);
            Statement vStmt = e.getProperty(value);

            if(kStmt != null && vStmt != null) {

                RDFNode kNode = kStmt.getObject();
                RDFNode vNode = vStmt.getObject();

                PlaceholderInfo kPlaceholder = new PlaceholderInfo(keyClazz, null, entity, shape, null, null, kNode, null);
                PlaceholderInfo vPlaceholder = new PlaceholderInfo(valueClazz, null, entity, shape, null, null, vNode,
                        null);

                List<PlaceholderInfo> entryPlaceholders = Arrays.asList(kPlaceholder, vPlaceholder);

                ResolutionTask<PlaceholderInfo> task = new ResolutionTaskBase<PlaceholderInfo>(entryPlaceholders) {
                    @Override
                    public Collection<ResolutionTask<PlaceholderInfo>> resolve(List<Object> resolutions) {
                        Object k = resolutions.get(0);
                        Object v = resolutions.get(1);
                        map.put(k, v);
                        return Collections.emptyList();
                    }
                };

                result.getTasks().add(task);
            }
        }

        return result;
    }

    @Override
    public Class<?> getEntityClass() {
        return Map.class;
    }

    @Override
    public Node getRootNode(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object createJavaObject(RDFNode node) {
        @SuppressWarnings("rawtypes")
        Map result = new HashMap();
        return result;
    }

    @Override
    public boolean hasIdentity() {
        // TODO Auto-generated method stub
        return false;
    }

    public PathFragment resolve(String propertyName) {
        PathFragment result = "key".equals(propertyName)
                ? new PathFragment(RelationUtils.createRelation(key, false), keyClazz, keyRdfType, null)
                : "value".equals(propertyName)
                        ? new PathFragment(RelationUtils.createRelation(value, false), keyClazz, valueRdfType, null)
                        : null;

        return result;
    }

}

//
// @Override
// public void populateEntity(RdfPersistenceContext persistenceContext, Object
// entity, Node subject, Graph graph, Consumer<Triple> outSink) {
// Model model = ModelFactory.createModelForGraph(graph);
// RDFNode root = ModelUtils.convertGraphNodeToRDFNode(subject, model);
//
// // <Object, Object>
// Map map = createMapView.apply(entity);
//
//
// for(Statement stmt : root.asResource().listProperties(entry).toList()) {
// Resource e = stmt.getObject().asResource();
//
// Node kNode = e.getProperty(key).getObject().asNode();
// Node vNode = e.getProperty(value).getObject().asNode();
//
//
// // TODO: We need to dynamically figure out which entity the node could be
// RdfType rdfType = null;
// Object k = persistenceContext.entityFor(Object.class, kNode, null);//new
// TypedNode(rdfType, kNode));
// Object v = persistenceContext.entityFor(Object.class, vNode, null);//new
// TypedNode(rdfType, vNode));
//
// map.put(k, v);
// }
// }
