package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.Resource;

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


	@Override
	public void exposeFragment(ResourceFragment out, Resource priorState, Object entity) {
	}


	@Override
	public EntityFragment populate(Resource shape, Object entity) {
		return null;
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
