package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.function.Consumer;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.model.RdfSeqUtils;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A type that
 *
 * @author raven
 *
 */
public class RdfTypeSeq
    extends RdfTypeComplexBase
{
    public RdfTypeSeq(RdfTypeFactory typeFactory, RdfType itemRdfType) {
        this.itemRdfType = itemRdfType;
    }
//    public RdfTypeSeq(RdfTypeFactory typeFactory, RdfType itemRdfType) {
//        super(typeFactory);
//        this.itemRdfType = itemRdfType;
//    }

    private RdfType itemRdfType;

    public PropertyRelation createRelation() {
        return RdfSeqUtils.seqRelation;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

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
    public Object createJavaObject(RDFNode node) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
    	// TODO Auto-generated method stub	
    }
    
    @Override
    public EntityFragment populate(Resource shape, Object entity) {
    	// TODO Auto-generated method stub
    	return null;
    }

	@Override
	public boolean hasIdentity() {
		return false;
	}
    
}
