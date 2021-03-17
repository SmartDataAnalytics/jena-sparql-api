package org.aksw.jena_sparql_api.mapper.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.commons.beans.model.PropertyOps;
import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.jena_sparql_api.mapper.impl.engine.EntityGraphMap;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.MutableClassToInstanceMap;

public class RdfPersistenceContextImpl
    implements RdfPersistenceContext
{
    protected Set<Object> managedEntities = Sets.newIdentityHashSet();

    protected RdfTypeFactory typeFactory;

    protected Map<Object, Node> entityToPrimaryNode = new IdentityHashMap<>();

    //protected Map<RdfType, Map<Node, Object>> typeToNodeToEntity;
    protected Map<Node, ClassToInstanceMap<Object>> nodeToTypeToEntity = new HashMap<>();

    //protected Frontier<ResolutionRequest>
    protected List<ResolutionRequest> resolutionRequests = new ArrayList<>();

    //protected Multimap<Object, EntityId> entity

    protected ReversibleMap<EntityId, Object> idToEntity = new ReversibleMapImpl<>(new HashMap<>(), Multimaps.newSetMultimap(Maps.newIdentityHashMap(), HashSet::new));
    //protected Map<Object, EntityId> entityToId = new IdentityHashMap<>();
    protected EntityGraphMap<EntityId> entityGraphMap = new EntityGraphMap<EntityId>();

    public Map<Object, Node> getPrimaryNodeMap() {
        return entityToPrimaryNode;
    }

    public List<ResolutionRequest> getResolutionRequests() {
        return resolutionRequests;
    }

    @Override
    public Set<Object> getManagedEntities() {
        return managedEntities;
    }

    @Override
    public void requestResolution(PropertyOps propertyOps, Object entity,
            Node node) {
        ResolutionRequest request = new ResolutionRequest(propertyOps, entity, node, null);
        resolutionRequests.add(request);
    }

    @Override
    public Object entityFor(Class<?> clazz, Node node, Supplier<Object> newInstance) {
        ClassToInstanceMap<Object> typeToEntity = nodeToTypeToEntity.computeIfAbsent(node, (x) -> MutableClassToInstanceMap.create());

        Object result = newInstance != null
                ? typeToEntity.computeIfAbsent(clazz, (type) -> newInstance.get())
                : typeToEntity.get(clazz);

        return result;
    }

    @Override
    public EntityGraphMap<EntityId> getEntityGraphMap() {
        return entityGraphMap;
    }

    @Override
    public ReversibleMap<EntityId, Object> getIdToEntityMap() {
        return idToEntity;
    }

    @Override
    public Map<Object, EntityId> getEntityToIdMap() {
        // TODO Auto-generated method stub
        return null;
    }




//
//    protected EntityContext<Object> entityContext = EntityContextImpl.createIdentityContext(Object.class);
//    protected EntityContext<TypedNode> typedNodeContext = new EntityContextImpl<TypedNode>();
//
//    protected EntityGraphMap entityGraphMap = new EntityGraphMap();
//
//    protected Frontier<TypedNode> frontier;
//
//
//    public RdfPersistenceContextImpl(Frontier<TypedNode> frontier, RdfTypeFactory typeFactory) {
//        super();
//        this.typeFactory = typeFactory;
//        this.frontier = frontier;
//    }
//
//    public Frontier<TypedNode> getFrontier() {
//        return frontier;
//    }
//
//
//
////	public void setFrontier(Frontier<TypedNode> frontier) {
////		this.frontier = frontier;
////	}
//
//    @Override
//    public RdfTypeFactory getTypeFactory() {
//        return typeFactory;
//    }
//
//    /**
//     * TODO Exposing the entity graph map directly does not seem to be good encapsulation
//     *
//     * @return
//     */
//    @Override
//    public EntityGraphMap getEntityGraphMap() {
//        return entityGraphMap;
//    }
//
//    @Override
//    public Object getEntity(TypedNode typedNode) {
//        Object result = typedNodeContext.getAttribute(typedNode, "entity", null);
//        return result;
//    }
//
//    @Override
//    public Object entityFor(TypedNode typedNode) {
//        RdfType rdfType = typedNode.getRdfType();
//        Node node = typedNode.getNode();
//
//        // TODO If we request an entity for a typed node for which an entity
//        // already exists for a base class, what then?
//        // i.e: given Person implements Thing, .entityFor(Thing, node1) followed by .entityFor(Person, node1)
//        // I suppose on merging all entities have to be updated.
//        // or entityFor needs to be replaced by entityPoolFor().
//        // and all entities in a pool are proxied such that setting any property in the pool
//        // updates all other entities in that pool - hm, but then entityFor would always have to return a proxy
//
//        // Check if there is already a java object for the given class with the given id
//        Object result = typedNodeContext.getAttribute(typedNode, "entity", null);
//        if(result == null) {
//            result = rdfType.createJavaObject(node, null);
//            typedNodeContext.setAttribute(typedNode, "entity", result);
//        }
//
//        entityContext.setAttribute(result, "rootNode", node);
//
////        TypedNode pr = new TypedNode(rdfType, node);
//        frontier.add(typedNode);
//
//        return result;
//    }
//
//
//    /**
//     * TODO It could happen that multiple typedNodes map to the same entity
//     * Probably additional context would be needed to resolve the root node for
//     * an entity
//     */
//    @Override
//    public Node getRawRootNode(Object entity) {
//        Node result = entityContext.getAttribute(entity, "rootNode", null);
//        return result;
//    }
//
//
//    public void checkManaged(Object bean) {
//        if(!isManaged(bean)) {
//            throw new RuntimeException("Bean was expected to be managed: " + bean);
//        }
//    }
//
//    public boolean isManaged(Object bean) {
//        FrontierStatus status = frontier.getStatus(bean);
//        boolean result = !FrontierStatus.UNKNOWN.equals(status);
//        return result;
//    }
//
//
//    /**
//     * Convenience accessors
//     *
//     * @param bean
//     * @return
//     */
//
//    public boolean isPopulated(Object populationRequest) {
//        boolean result = FrontierStatus.DONE.equals(frontier.getStatus(populationRequest));
//
//        return result;
//    }
//
//    public static Node getOrCreateRootNode(RdfPersistenceContext persistenceContext, RdfTypeFactory typeFactory, Object entity) {
//        if(true) {
//            System.out.println("getOrCreateRootNodeX");
//            //throw new RuntimeException("Do not use this method at least for now");
//        }
//        Node result = persistenceContext.getRawRootNode(entity);
//        if(result == null) {
//
//            Class<?> clazz = entity.getClass();
//            RdfType rdfType = typeFactory.forJavaType(clazz);
//            result = rdfType.getRootNode(entity);
//            persistenceContext.getFrontier().add(new TypedNode(rdfType, result));
//        }
//        return result;
//    }
//
//    @Override
//    public Node getRootNode(Object entity) {
//        Node result = getRawRootNode(entity); //getOrCreateRootNode(this, typeFactory, entity);
//
//        return result;
//    }
//
//    @Override
//    public void put(Node node, Object entity) {
//        RdfType rdfType = typeFactory.forJavaType(entity.getClass());
//        TypedNode typedNode = new TypedNode(rdfType, node);
//
//        typedNodeContext.setAttribute(typedNode, "entity", entity);
//        entityContext.setAttribute(entity, "rootNode", node);
//    }
//
//    @Override
//    public void requestResolution(Object entity, String propertyName,
//            Node subject, RdfType rdfType) {
//        // TODO Auto-generated method stub
//
//    }
//
}
