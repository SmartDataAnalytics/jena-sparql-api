package org.aksw.jena_sparql_api.mapper.trash;

import java.util.List;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;


public class RdfValueMapperSimple
    implements RdfValueMapper
{
    protected Class<?> propertyType;

    protected RDFDatatype dtype;

    protected Node nullValue;

    public RdfValueMapperSimple(Class<?> propertyType, RDFDatatype dtype, Node nullValue) {
        this.propertyType = propertyType;
        this.dtype = dtype;
        this.nullValue = nullValue;
    }


    @Override
    public void writeValue(Object value, Node subject, Node predicate, Graph outputGraph) {
        Node o;
        if(value != null) {
            Class<?> valueType = value.getClass();
            boolean isValidType = propertyType.isAssignableFrom(valueType);

            if(!isValidType) {
                throw new RuntimeException("Invalid object provided");
            }

            String lex = dtype.unparse(value);
            o = NodeFactory.createLiteral(lex, dtype);
        } else {
            o = nullValue;
        }

        if(o != null) {
            Triple triple = new Triple(subject, predicate, o);
            outputGraph.add(triple);
        }
    }

    @Override
    public Object readValue(Graph graph, Node subject, Node predicate) {
        List<Node> nodes = GraphUtil.listObjects(graph, subject, predicate).toList();

        Object result = null;
        if(!nodes.isEmpty()) {
            Node node = nodes.iterator().next();

            if(node != null) {
                result = node.equals(nullValue)
                    ? null
                    : node.getLiteralValue()
                    ;
            }
        }

        return result;
    }
}

