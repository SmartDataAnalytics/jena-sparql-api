package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.algebra.utils.FixpointIteration;
import org.aksw.jena_sparql_api.stmt.SparqlStmtMgr;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctionResource;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.function.user.ExprTransformExpand;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;

public class MainUdfTest2 {
    public static void main(String[] args) {
        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);

        Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/jena"));
        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);
        UserDefinedFunctions.registerAll(map);

        Expr e = new E_Function("http://ns.aksw.org/function/decodeBnodeIri", new ExprList(new ExprVar(Vars.x)));
        ExprTransform xform = new ExprTransformExpand(map);
        e = FixpointIteration.apply(100, e, x -> ExprTransformer.transform(xform, x));


        System.out.println("INVERSES: " + model.createResource("http://ns.aksw.org/function/skolemizeBnodeLabel")
            .as(UserDefinedFunctionResource.class)
            .getDefinitions().iterator().next()
            .getInverses()
            .iterator().next()
            .getFunction()
            .getDefinitions().iterator().next()
            .getExpr()
        );

//		NodeValue x = ExprTransformVirtualBnodeUris.eval("http://ns.aksw.org/function/str", NodeValue.makeInteger(666));
        System.out.println(e);

//		System.out.println(model.size());
    }
}
