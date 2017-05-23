package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;



public class OpIndexTests {
    public static void main(String[] args) {
        String queryString = "Select Distinct ?s { { ?s a <http://Foo> } Union { ?s a <http://Bar> } Union { ?s a <http://Baz> } . Filter(?s = <http://Bar>) }";
        Query query = QueryFactory.create(queryString);

        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);
        Transform transform = new TransformFilterPlacement();

        op = Optimize.apply(transform, op);
        //op = Optimize.optimize(op, new Context());



        Map<Var, Var> varMap = new HashMap<Var, Var>();
        varMap.put(Var.alloc("s"), Var.alloc("x"));
        NodeTransform nodeTransform = new NodeTransformRenameMap(varMap);
        op = NodeTransformLib.transform(nodeTransform, op);


        System.out.println(op);
        Map<Op, Op> parentMap = OpUtils.parentMap(op);

        for(Entry<Op, Op> entry : parentMap.entrySet()) {
            Op v = entry.getKey();
            if(v instanceof OpQuadPattern) {
                System.out.println("QuadPattern: " + v);
            }

            //System.out.println("Entry: " + entry);
        }



    }
}
