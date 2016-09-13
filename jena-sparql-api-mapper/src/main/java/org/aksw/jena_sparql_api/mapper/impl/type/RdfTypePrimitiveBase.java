package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public abstract class RdfTypePrimitiveBase
    extends RdfTypeBase
{
//    public RdfTypePrimitiveBase(RdfTypeFactory typeFactory) {
//        super(typeFactory);
//    }

    @Override
    public boolean isSimpleType() {
        return true;
    }


    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    public void populateEntity(RdfPersistenceContext persistenceContext, Object entity, Node subject, Graph inGraph, Consumer<Triple> sink) {
    }

    public void emitTriples(RdfEmitterContext emitterContext, Object entity, Node subject, Graph shapeGraph, Consumer<Triple> sink) {        
    }
    
//    @Override
//    public void exposeTypeDeciderShape(ResourceShapeBuilder rsb) {
//    }
//
//    @Override
//    public Collection<RdfType> getApplicableTypes(Resource resource) {
//        //return Collections.singleton(getEntityClass());
//        return Collections.emptySet();
//    }


}
