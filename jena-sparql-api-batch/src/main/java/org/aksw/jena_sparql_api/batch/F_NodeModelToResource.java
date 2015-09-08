package org.aksw.jena_sparql_api.batch;

import java.util.Map.Entry;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.util.ModelUtils;

public class F_NodeModelToResource<T extends RDFNode>
    implements Function<Entry<Node, Model>, T>
{
    @Override
    public T apply(Entry<Node, Model> input) {
        Node node = input.getKey();
        Model model = input.getValue();

        RDFNode tmp = ModelUtils.convertGraphNodeToRDFNode(node, model);
        @SuppressWarnings("unchecked")
        T result = (T)tmp;
        return result;
    }

    public static <T extends RDFNode> F_NodeModelToResource<T> create() {
        F_NodeModelToResource<T> result = new F_NodeModelToResource<T>();
        return result;
    }
}
