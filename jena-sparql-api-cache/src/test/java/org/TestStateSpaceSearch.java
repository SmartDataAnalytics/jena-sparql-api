package org;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.isomorphism.Problem;
import org.aksw.isomorphism.ProblemContainerImpl;
import org.aksw.isomorphism.StateProblemContainer;
import org.aksw.jena_sparql_api.concept_cache.collection.ContainmentMap;
import org.aksw.jena_sparql_api.concept_cache.collection.ContainmentMapImpl;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingExpr;
import org.aksw.jena_sparql_api.concept_cache.combinatorics.ProblemVarMappingQuad;
import org.aksw.jena_sparql_api.concept_cache.core.SparqlCacheUtils;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.state_space_search.core.StateSearchUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
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

        // We could create a graph over quads and expressions that variables

        SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);
        Element queryElement = elementParser.apply("?x <my://type> <my://Airport> ; <my://label> ?l . FILTER(langMatches(lang(?l), 'en'))");

        Element cacheElement = elementParser.apply("?s <my://type> <my://Airport> ; ?p ?l . FILTER(?p = <my://label> || ?p = <my://name>)");


        ProjectedQuadFilterPattern cachePqfp = SparqlCacheUtils.transform(cacheElement);
        System.out.println("ProjectedQuadFilterPattern: " + cachePqfp);

        Generator<Var> generator = VarGeneratorImpl2.create();
        QuadFilterPatternCanonical canonical = SparqlCacheUtils.canonicalize2(cachePqfp.getQuadFilterPattern(), generator);
        System.out.println("QuadFilterPatternCanonical: " + canonical);


        IBiSetMultimap<Quad, Set<Set<Expr>>> index = SparqlCacheUtils.createMapQuadsToFilters(canonical);
        System.out.println("Index: " + index);

        // Features are objects that describe view
        // A query needs to cover all features of view
        // so it must hold that |featuresOf(query)| >= |featuresOf(cache)|
        Set<Object> features = new HashSet<Object>();
        index.asMap().values().stream().flatMap(cnfs -> cnfs.stream())
        .flatMap(cnf -> cnf.stream())
        .filter(clause -> clause.size() == 1)
        .flatMap(clause -> clause.stream())
        .forEach(feature -> features.add(feature));


        ContainmentMap<Object, Object> featuresToCache = new ContainmentMapImpl<>();
        featuresToCache.put(features, canonical);

        Collection<Entry<Set<Object>, Object>> candidates = featuresToCache.getAllEntriesThatAreSubsetOf(
                new HashSet<>(Arrays.asList(
                        ExprUtils.parse("?o = <my://Airport>"),
                        ExprUtils.parse("?p = <my://type>")
              )));
        System.out.println("Candidates: " + candidates);

//        ContainmentMap<Set<Expr>, Quad> clauseToQuads = new ContainmentMapImpl<>();
//        for(Entry<Quad, Set<Set<Expr>>> entry : index.entries()) {
//            clauseToQuads.put(entry.getValue(), entry.getKey());
//        }
//
        // given a query, we need to conver at least all constraints of the cache
        //clauseToQuads.getAllEntriesThatAreSubsetOf(prototye)


//        SparqlViewCacheImpl x;
//        x.lookup(queryQfpc)

        //QuadFilterPatternCanonical


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
