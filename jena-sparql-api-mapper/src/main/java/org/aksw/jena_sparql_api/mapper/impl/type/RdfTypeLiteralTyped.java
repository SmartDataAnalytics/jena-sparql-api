package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

public class RdfTypeLiteralTyped
    extends RdfTypeBase
{
    protected RDFDatatype rdfDatatype;

    public RdfTypeLiteralTyped(RdfTypeFactory typeFactory, RDFDatatype rdfDatatype) {
        super(typeFactory);
        this.rdfDatatype = rdfDatatype;
    }

    @Override
    public Class<?> getTargetClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void build(ResourceShapeBuilder rsb) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setValues(Object targetObj, DatasetGraph datasetGraph) {
        // TODO Auto-generated method stub

    }

    @Override
    public DatasetGraph createDatasetGraph(Object obj, Node g) {
        // TODO Auto-generated method stub
        return null;
    }

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

    @Override
    public boolean isSimpleType() {
        return true;
    }
}
