package org.aksw.jena_sparql_api.mapper.trash;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

public class RdfValueMapperStringIri
    implements RdfValueMapper
{
    @Override
    public void writeValue(Object value, Node subject, Node predicate,
            Graph outputGraph) {

        if(value != null) {
            // value must be a string
            String iri = value.toString();

            Node o = NodeFactory.createURI(iri);
            Triple t = new Triple(subject, predicate, o);
            outputGraph.add(t);
        }

    }

    @Override
    public Object readValue(Graph graph, Node subject, Node predicate) {
        List<Node> os = GraphUtil.listObjects(graph, subject, predicate).toList();

        Object result = null;
        if(!os.isEmpty()) {
            Node o = os.iterator().next();

            if(o.isURI()) {
                result = o.getURI();
            }

        }

        return result;
    }
}
