package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

public class RdfTypeLiteralTyped
    extends RdfTypePrimitiveBase
{
    protected RDFDatatype rdfDatatype;

    public RdfTypeLiteralTyped(RdfTypeFactory typeFactory, RDFDatatype rdfDatatype) {
        super();
        this.rdfDatatype = rdfDatatype;
    }

//    public RdfTypeLiteralTyped(RdfTypeFactory typeFactory, RDFDatatype rdfDatatype) {
//        super(typeFactory);
//        this.rdfDatatype = rdfDatatype;
//    }

    @Override
    public Class<?> getEntityClass() {
        return rdfDatatype.getJavaClass();
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
    public Object createJavaObject(RDFNode node) {
        Object result;
        if(node.isLiteral()) {
            Literal literal = node.asLiteral();
            //RDFDatatype nodeDt = literal.getDatatype();

            // TODO Make sure that the node datatype/value is actually compatible with this one
            // I think node datatype must be a sub-type of this one (e.g. nodedt=Student, thisdt=Person)

            Object tmp = literal.getValue();

            String lexicalForm = rdfDatatype.unparse(tmp);
            result = rdfDatatype.parse(lexicalForm);

        } else {
            throw new RuntimeException("Literal node expected, but got: " + node);
        }

        return result;
    }

    @Override
    public String toString() {
        return "RdfTypeLiteralTyped [rdfDatatype=" + rdfDatatype + "]";
    }

    @Override
    public boolean hasIdentity() {
        return false;
    }


}
