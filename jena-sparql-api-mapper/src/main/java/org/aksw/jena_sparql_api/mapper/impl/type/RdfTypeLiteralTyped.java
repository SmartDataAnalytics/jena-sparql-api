package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class RdfTypeLiteralTyped
    extends RdfTypePrimitiveBase
{
    protected RDFDatatype rdfDatatype;

    public RdfTypeLiteralTyped(RdfTypeFactory typeFactory, RDFDatatype rdfDatatype) {
        super(typeFactory);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Class<?> getEntityClass() {
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
    public Node getRootNode(Object entity) {
        String lex = rdfDatatype.unparse(entity);
        Node result = NodeFactory.createLiteral(lex, rdfDatatype);

        return result;
    }

    @Override
    public Object createJavaObject(Node node) {
        Object result = node.getLiteralValue();
        return result;
    }
}
