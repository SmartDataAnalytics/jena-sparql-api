package org.aksw.jena_sparql_api.mapper.context;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

/**
 * Work in progress
 *
 * Attempt at an RdfPopulationContext that offers a view on a subset of
 * beans that need population.
 * The idea is to use this subset to be able to populate a batch at beans at once.
 * For example, consider this example:
 * List<MyClass> list = entityManager.list(MyClass.class);
 *
 * for(MyClass item : list) {
 *   // Assome we are now accessing a lazily fetched attribute:
 *   list.getSomeCollection(); // On first access, resolve all collections for all items in the list
 * }
 *
 *
 *
 *
 * @author raven
 *
 */
public class RdfPopulationContextFragment
//	implements RdfPopulationContext
{
    protected RdfPopulationContext parentContext;
    protected RdfMapperEngineImpl engine;

    protected Set<Object> unpopulatedBeans = new HashSet<Object>();

    //@Override
    public Object objectFor(TypedNode typedNode) {
        Object result = parentContext.objectFor(typedNode);
        boolean isPopulated = true;

        if(!isPopulated) {
            unpopulatedBeans.add(result);
        }

        //parentContext

        // TODO Auto-generated method stub
        return result;
    }

//	public void resolveAll() {
//		Set<Object> open = new HashSet<Object>(unpopulatedBeans);
//		for(Object bean : open) {
//			resolve(bean);
//		}
//	}

    /**
     * Get the bean's node. For primitive types, this is usually the corresponding
     * plain literal node (There could be cases where multiple triples should be generated from a primitive,
     * such as representing a Geometry with neo geo).
     * For classes, this is usually an IRI.
     *
     *
     * @param bean
     * @return
     */
    public Node getRootNode(Object bean) {
        //Node result = parent.getRootNode(bean);
        //return result;
        //beanToNode.
        return null;
    }

//	public void resolve(Object bean) {
//		RdfType type = null;
//		Node node = getRootNode(bean);
//
//		ResourceShapeBuilder builder = null;
//		type.exposeShape(builder);
//
//		ResourceShape shape = builder.getResourceShape();
//		MappedConcept<Graph> mc = shape.createMappedConcept(shape, filter);
//
//		ListService<Concept, Node, Node> ls = ListServiceUtils.createListServiceMappedConcept(qef, mc, true);
//
//		Map<Node, Graph> nodeToGraph = ls.fetchData(concept, null, null);
//
//
//
//
//		unpopulatedBeans.remove(bean);
//
//
//
//	}
}
