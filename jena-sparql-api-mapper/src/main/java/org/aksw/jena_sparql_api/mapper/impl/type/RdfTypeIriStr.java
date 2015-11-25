package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.context.RdfPopulationContext;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

/**
 * RdfType to map Java Strings to IRIs and vice versa
 * @author raven
 *
 */
public class RdfTypeIriStr
    extends RdfTypePrimitive
{
    public RdfTypeIriStr(RdfTypeFactory typeFactory) {
        super(typeFactory);
        // TODO Auto-generated constructor stub
    }


    @Override
    public Class<?> getBeanClass() {
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

}
