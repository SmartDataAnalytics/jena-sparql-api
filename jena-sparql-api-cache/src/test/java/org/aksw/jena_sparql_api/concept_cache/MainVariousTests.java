package org.aksw.jena_sparql_api.concept_cache;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.util.Context;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MainVariousTests {


    @Test
    public void test1() {
        String str = "Select Distinct ?s ?o { { ?s ?p ?o } Union { ?s ?p ?o } Filter(?p = <a>)}";
        Query query = QueryFactory.create(str, Syntax.syntaxSPARQL_11);
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);


        Context context = new Context();
        context.put(ARQ.optMergeBGPs, true);
        context.put(ARQ.optMergeExtends, true);
        context.put(ARQ.optExprConstantFolding, true);
        //context.put(ARQ.optFilterPlacement, true);
        context.put(ARQ.optFilterConjunction, true);
        context.put(ARQ.optImplicitLeftJoin, true);

        // It is important to keep optFilterEquality turned off!
        // Otherwise it may push constants back into the quads
        context.put(ARQ.optFilterEquality, false);
        context.put(ARQ.optFilterInequality, false);
        context.put(ARQ.optDistinctToReduced, false);
        context.put(ARQ.optFilterExpandOneOf, false);
        context.put(ARQ.optFilterPlacementBGP, false);

        Rewrite rewriter = Optimize.getFactory().create(context);

        op = rewriter.rewrite(op);// Algebra.optimize(op);

        // TODO The summarize method no longer exists in this module, as LSQ feature extractor would do the same thing

//        Object summary = OpUtils.summarize(op);
//        System.out.println("Summary: " + summary);
//        System.out.println("IsEquivalent: " + OpUtils.isEquivalent(op, op));
//
//
//        System.out.println("yay" + op);
    }
}
