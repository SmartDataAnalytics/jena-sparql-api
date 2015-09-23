package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
import com.hp.hpl.jena.vocabulary.XSD;

public class NodeValueJson
    extends NodeValue
{
    private Object json;

    public NodeValueJson(Object json) {
        super();
        this.json = json;
    }

    public Object getJson() {
        return json;
    }

    @Override
    protected Node makeNode() {
        RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(XSD.getURI() + "json");
        String str = datatype.unparse(json);
        Node result = NodeFactory.createLiteral(str, null, datatype);

        return result;
    }

    @Override
    public void visit(NodeValueVisitor arg0) {
        throw new NotImplementedException();
    }
}