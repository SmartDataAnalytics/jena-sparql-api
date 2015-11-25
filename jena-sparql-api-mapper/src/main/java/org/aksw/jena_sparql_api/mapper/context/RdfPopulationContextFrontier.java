package org.aksw.jena_sparql_api.mapper.context;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierStatus;

import com.hp.hpl.jena.graph.Node;

public class RdfPopulationContextFrontier
    implements RdfPopulationContext
{
    protected EntityContext<Object> entityContext = EntityContextImpl.createIdentityContext(Object.class);
    protected EntityContext<TypedNode> typedNodeContext = new EntityContextImpl<TypedNode>();

    protected Frontier<TypedNode> frontier;

    public RdfPopulationContextFrontier(Frontier<TypedNode> frontier) {
        super();
        this.frontier = frontier;
    }


    @Override
    public Object getEntity(TypedNode typedNode) {
        Object result = typedNodeContext.getAttribute(typedNode, "entity", null);
        return result;
    }

    @Override
    public Object objectFor(TypedNode typedNode) {
        RdfType rdfType = typedNode.getRdfType();
        Node node = typedNode.getNode();

        // Check if there is already a java object for the given class with the given id
        Object result = typedNodeContext.getAttribute(typedNode, "entity", null);
        if(result == null) {
            result = rdfType.createJavaObject(node);
            typedNodeContext.setAttribute(typedNode, "entity", result);
        }

        entityContext.setAttribute(result, "rootNode", node);

//        TypedNode pr = new TypedNode(rdfType, node);
        frontier.add(typedNode);

        return result;
    }


    /**
     * TODO It could happen that multiple typedNodes map to the same entity
     * Probably additional context would be needed to resolve the root node for
     * an entity
     */
    @Override
    public Node getRootNode(Object entity) {
        Node result = entityContext.getAttribute(entity, "rootNode", null);
        return result;
    }


    public void checkManaged(Object bean) {
        if(!isManaged(bean)) {
            throw new RuntimeException("Bean was expected to be managed: " + bean);
        }
    }

    public boolean isManaged(Object bean) {
        FrontierStatus status = frontier.getStatus(bean);
        boolean result = !FrontierStatus.UNKNOWN.equals(status);
        return result;
    }


    /**
     * Convenience accessors
     *
     * @param bean
     * @return
     */

    public boolean isPopulated(Object populationRequest) {
        boolean result = FrontierStatus.DONE.equals(frontier.getStatus(populationRequest));

        return result;
    }

}
