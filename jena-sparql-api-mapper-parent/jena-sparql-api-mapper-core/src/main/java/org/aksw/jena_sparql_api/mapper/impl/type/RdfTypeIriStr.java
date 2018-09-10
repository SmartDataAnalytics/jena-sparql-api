package org.aksw.jena_sparql_api.mapper.impl.type;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;

/**
 * RdfType to map Java Strings to IRIs and vice versa
 * @author raven
 *
 */
public class RdfTypeIriStr
    extends RdfTypePrimitiveBase
{
    public RdfTypeIriStr() { //RdfTypeFactory typeFactory) {
//        super(typeFactory);
      super();
    }


    @Override
    public Class<?> getEntityClass() {
        return String.class;
    }

    @Override
    public Node getRootNode(Object obj) {
        String str = obj.toString();
        Node result = NodeFactory.createURI(str);
        return result;
    }

    @Override
    public Object createJavaObject(RDFNode node) {
        String result = node.asNode().getURI();
        return result;
    }
    
	@Override
	public boolean hasIdentity() {
		return false;
	}


}
