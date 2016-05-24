package org;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.aksw.isomorphism.Problem;
import org.aksw.isomorphism.ProblemContainerImpl;
import org.aksw.isomorphism.StateProblemContainer;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatesetDescription;
import org.aksw.state_space_search.core.StateSearchUtils;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

public class TestStateSpaceSearch {
    public static void main(String[] args) throws FileNotFoundException {
        {
//            QueryExecutionFactory qef = FluentQueryExecutionFactory
//                    .http("http://linkedgeodata.org/test/vsparql")
//                    .config()
//                        .withParser(SparqlQueryParserImpl.create())
//                        .withDatasetDescription(DatasetDescriptionUtils.createDefaultGraph("http://linkedgeodata.org/ne/"))
//                        .withQueryTransform(F_QueryTransformDatesetDescription.fn)
//                    .end()
//                    .create();
//            Model model = qef.createQueryExecution("CONSTRUCT WHERE { ?s ?p ?o }").execConstruct();
//
//            //System.out.println(ResultSetFormatter.asText(qef.createQueryExecution("SELECT (count(*) As ?c) FROM <http://linkedgeodata.org/ne/> WHERE { ?s ?p ?o }").execSelect()));
//            model.write(new FileOutputStream(new File("/tmp/ne.nt")), "NTRIPLES");
            //model.write(System.out);
        }


        Expr a = ExprUtils.parse("(?z = ?x + 1)");
        Expr b = ExprUtils.parse("?a = ?b || (?c = ?a + 1) && (?k = ?i + 1)");
        //Expr b = ExprUtils.parse("?x = ?y || (?z = ?x + 1)");

        Set<Set<Expr>> ac = CnfUtils.toSetCnf(b);
        Set<Set<Expr>> bc = CnfUtils.toSetCnf(a);

        Problem<Map<Var, Var>> p = new ProblemVarMappingExpr(ac, bc, Collections.emptyMap());

        System.out.println("p");
        System.out.println(p.getEstimatedCost());
        ProblemVarMappingExpr.createVarMap(a, b).forEach(x -> System.out.println(x));

        Collection<Quad> as = Arrays.asList(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));
        Collection<Quad> bs = Arrays.asList(new Quad(Vars.l, Vars.x, Vars.y, Vars.z));


        //Collection<Quad> cq =
        System.out.println("q");
        Problem<Map<Var, Var>> q = new ProblemVarMappingQuad(as, bs, Collections.emptyMap());
        System.out.println(q.getEstimatedCost());

        q.generateSolutions().forEach(x -> System.out.println(x));


        //Maps.com

        System.out.println("pc");
        ProblemContainerImpl<Map<Var, Var>> pc = ProblemContainerImpl.create(p, q);
        StateProblemContainer<Map<Var, Var>> state = new StateProblemContainer<>(Collections.emptyMap(), pc, SparqlCacheUtils::mergeCompatible);
        //SearchUtils.depthFirstSearch(state, isFinal, vertexToResult, vertexToEdges, edgeCostComparator, edgeToTargetVertex, depth, maxDepth)
        StateSearchUtils.depthFirstSearch(state, 10).forEach(x -> System.out.println(x));


        // Next level: Matching Ops


        // Problem: We can now find whether there exist variable mappings between two expressions or sets of quads
        // But the next step is to determine which exact parts of the query can be substituted
        // The thing is: We need to compute the variable mapping, but once we have obtained it,
        // we could use the state configuration that led to the solution to efficiently determine
        // the appropriate substitutions



        //p.generateSolutions().forEach(x -> System.out.println(x));
    }
}
