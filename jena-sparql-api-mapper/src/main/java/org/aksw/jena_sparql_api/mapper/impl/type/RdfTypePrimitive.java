package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Graph;

public abstract class RdfTypePrimitive
    extends RdfTypeBase
{
    public RdfTypePrimitive(RdfTypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public void emitTriples(RdfEmitterContext emitterContext, Graph out, Object obj) {
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

  @Override
  public void populateBean(RdfPopulationContext populationContext, Object targetObj, Graph graph) {
  }

//	@Override
//	public void populateBean(RdfPopulationContext populationContext, Object targetObj, DatasetGraph datasetGraph) {
//	}
}
