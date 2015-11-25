package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

public class RdfTypeLiteralTyped
    extends RdfTypePrimitive
{
    protected RDFDatatype rdfDatatype;

    public RdfTypeLiteralTyped(RdfTypeFactory typeFactory, RDFDatatype rdfDatatype) {
        super(typeFactory);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Class<?> getBeanClass() {
        return null;
    }

//    @Override
//    public void exposeShape(ResourceShapeBuilder rsb) {
//        // TODO Auto-generated method stub
//
//    }

//    @Override
//    public void populateBean(RdfPopulationContext populationContext, Object targetObj, Graph ) {
//        // TODO Auto-generated method stub
//
//    }

    @Override
    public Node getRootNode(Object obj) {
        String lex = rdfDatatype.unparse(obj);
        Node result = NodeFactory.createLiteral(lex, rdfDatatype);

        return result;
    }

    @Override
    public Object createJavaObject(Node node) {
        Object result = node.getLiteralValue();
        return result;
    }
}
