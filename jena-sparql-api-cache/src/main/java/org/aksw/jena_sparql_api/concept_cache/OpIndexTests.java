package org.aksw.jena_sparql_api.concept_cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.graph.NodeTransform;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib;



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
