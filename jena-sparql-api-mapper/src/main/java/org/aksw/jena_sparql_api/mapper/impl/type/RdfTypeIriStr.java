package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

/**
 * RdfType to map Java Strings to IRIs and vice versa
 * @author raven
 *
 */
public class RdfTypeIriStr
    extends RdfTypeBase
{
    public RdfTypeIriStr(RdfTypeFactory typeFactory) {
        super(typeFactory);
        // TODO Auto-generated constructor stub
    }


    @Override
    public Class<?> getTargetClass() {
        return String.class;
    }

    @Override
    public Node getRootNode(Object obj) {
        String str = obj.toString();
        Node result = NodeFactory.createURI(str);
        return result;
    }

    @Override
    public Object createJavaObject(Node node) {
        String result = node.getURI();
        return result;
    }

    @Override
    public void build(ResourceShapeBuilder rsb) {
        // Nothing to do as we do not require any sub properties
    }

    @Override
    public void setValues(Object targetObj, DatasetGraph datasetGraph) {
    }

    @Override
    public DatasetGraph createDatasetGraph(Object obj, Node g) {
        return null;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

}
