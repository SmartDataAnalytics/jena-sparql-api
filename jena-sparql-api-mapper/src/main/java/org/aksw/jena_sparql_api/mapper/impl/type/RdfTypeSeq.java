package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.PropertyRelation;
import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.mapper.model.RdfSeqUtils;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

/**
 * A type that
 *
 * @author raven
 *
 */
public class RdfTypeSeq
    extends RdfTypeBase
{
    public RdfTypeSeq(RdfTypeFactory typeFactory, RdfType itemRdfType) {
        super(typeFactory);
        this.itemRdfType = itemRdfType;
    }

    private RdfType itemRdfType;

    public PropertyRelation createRelation() {
        return RdfSeqUtils.seqRelation;
    }

    @Override
    public void exposeShape(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<?> getBeanClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getRootNode(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object createJavaObject(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSimpleType() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void writeGraph(Graph out, Object obj) {
        // TODO Auto-generated method stub

    }

	@Override
	public void setValues(RdfPopulationContext populationContext, Object targetObj, DatasetGraph datasetGraph) {
		// TODO Auto-generated method stub

	}

}
