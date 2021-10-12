package org.aksw.jena_sparql_api.algebra.path;

import java.util.Map.Entry;

import org.aksw.commons.path.core.Path;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class MainQueryToPaths {



    public static void main(String[] args) {
        String queryStr = "SELECT ?a { { {?s a ?x } { ?s a ?y } } UNION { BIND(<foo> AS ?bar) } FILTER(?a = 'yay') } LIMIT 10";
        // String queryStr = "SELECT (<foo> AS ?bar) { }";
        Op op = Algebra.compile(QueryFactory.create(queryStr));
        PathState pathState = PathState.create(op);

        // Transformer.transform(new TransformCopy(), op);
        TransformerWithPath.transform(pathState, TransformWithPathExample::new);


        for (Entry<Path<String>, Op> e : pathState.getPathToOp().entrySet()) {
            System.out.println(e.getKey());
            System.out.println("---------------------------");
            System.out.println(e.getValue());

            System.out.println();
        }

        // System.out.println(visitor.getPathToOp());
        // Transformer.transform(null, null, null, null)

    }
}
