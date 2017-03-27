package org.aksw.jena_sparql_api.batch.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.Pair;

import com.google.common.base.Function;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.ModelUtils;

public class F_ToModel
    implements Function<Entry<Node, Graph>, Entry<Resource, Model>>
{
    @Override
    public Entry<Resource, Model> apply(Entry<Node, Graph> input) {
        Node n = input.getKey();
        Graph g = input.getValue();


        Model m = ModelFactory.createModelForGraph(g);
        RDFNode tmp = ModelUtils.convertGraphNodeToRDFNode(n, m);
        Resource r = (Resource)tmp;

        Entry<Resource, Model> result = Pair.create(r, m);
        return result;
    }

    public static final F_ToModel fn = new F_ToModel();

    public static <IK, IV, OK, OV> Map<OK, OV> transform(Map<IK, IV> map, Function<Entry<IK, IV>, Entry<OK, OV>> fn) {
        Map<OK, OV> result = new HashMap<OK, OV>();
        transform(result, map, fn);
        return result;
    }

    public static <IK, IV, OK, OV> Map<OK, OV> transform(Map<OK, OV> result, Map<IK, IV> map, Function<Entry<IK, IV>, Entry<OK, OV>> fn) {
        for(Entry<IK, IV> entry : map.entrySet()) {
            Entry<OK, OV> e = fn.apply(entry);
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }
}