package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.context.RdfEmitterContext;
import org.aksw.jena_sparql_api.mapper.context.RdfPersistenceContext;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.atlas.lib.Sink;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;

public abstract class RdfTypePrimitiveBase
    extends RdfTypeBase
{
    public RdfTypePrimitiveBase(RdfTypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public void emitTriples(RdfPersistenceContext persistenceContext, RdfEmitterContext emitterContext, Graph out, Object obj) {
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

  @Override
  public void populateEntity(RdfPersistenceContext persistenceContext, Object targetObj, Graph graph, Sink<Triple> outSink) {
  }

//	@Override
//	public void populateBean(RdfPopulationContext populationContext, Object targetObj, DatasetGraph datasetGraph) {
//	}
}
