package org.aksw.jena_sparql_api.cache.tests;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystemImpl;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.codepoetics.protonpack.StreamUtils;

@Ignore
public class SparqlViewMatcherTreeTests {

    private static final Logger logger = LoggerFactory.getLogger(SparqlViewMatcherTreeTests.class);

    @Test
    public void testNoProjection() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new ClassPathResource("tree-matcher-queries.ttl").getInputStream(), Lang.TURTLE);
        runTests(model);
    }

    //@Test
    public void testProjection() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new ClassPathResource("tree-matcher-queries-projection.ttl").getInputStream(), Lang.TURTLE);
        runTests(model);
    }


    public static void runTests(Model model) {
        List<Resource> tests = model
                .listSubjectsWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList().stream()
                //.filter(r -> r.getURI().equals("http://example.org/test/t1a"))
                .collect(Collectors.toList());



        SparqlQueryParser queryParser = SparqlQueryParserImpl.create();
        for(Resource test : tests) {
            logger.debug("Test ID: " + test);
            String qas = test.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();
            String qbs = test.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();

            Set<Map<Var, Var>> expected = test.listProperties(SparqlQcVocab.result)
                    .mapWith(stmt -> stmt.getObject()).toSet().stream()
                    .map(o -> VarUtils.parseVarMap(o.asLiteral().getString()))
                    .collect(Collectors.toSet());
//			System.out.println("Expected: " + expected);

            logger.debug("View Query: " + qas);
            Query qa = queryParser.apply(qas);

            logger.debug("User Query: " + qbs);
            Query qb = queryParser.apply(qbs);

//            System.out.println(qa);
//            System.out.println(qb);

            logger.debug("mappings:");
            Set<Map<Var, Var>> actual = SparqlViewMatcherSystemImpl.match(qa, qb).flatMap(x -> {
                return StreamUtils.stream(x.getVarMaps());
            }).collect(Collectors.toSet());


            expected.forEach(x -> logger.debug("expected: " + x));
            logger.debug("Actual: " + actual);

            Assert.assertEquals(expected, actual);

            System.out.println("done.");
        }


        // TODO We now need to rewrite the query using the canonical quad filter patterns
        // for this purpose, we could create a map that maps original ops to qfpcs
//        SparqlCacheUtils.toMap(mm)

//
//        Map<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Boolean>> opToMatcherTest = new HashMap<>();
//        opToMatcherTest.put(OpDisjunction.class, (as, bs, mapping) -> true);
//
////        Function<Entry<Op, Op>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Boolean>> fnOpToMatcherTest = (nodeMapping) ->
////            determineMatchingStrategy(nodeMapping);
//            //opToMatcherTest.getOrDefault(op.getClass(), (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping).findFirst().isPresent());
//
//        Map<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Stream<Map<Op, Op>>>> opToMatcher = new HashMap<>();
//
//        opToMatcher.put(OpDisjunction.class, (as, bs, mapping) -> KPermutationsOfNUtils.kPermutationsOfN(mapping));
//        //opToMatcher.put(OpLeftJoin.class, (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping));
//
//        // Function that maps an operator to the matching strategy
//        Function<Class<?>, TriFunction<List<Op>, List<Op>, Multimap<Op, Op>, Stream<Map<Op, Op>>>> fnOpToMatcher = (op) ->
//            opToMatcher.getOrDefault(op, (as, bs, mapping) -> SequentialMatchIterator.createStream(as, bs, mapping));
//
//
//        List<String> as = Arrays.asList("a", "b", "c");
//        List<Integer> bs = Arrays.asList(1, 2, 3, 4);
//
//        Multimap<String, Integer> ms = HashMultimap.create();
//        ms.put("a", 1);
//        ms.put("a", 2);
//        ms.put("b", 2);
//        ms.put("b", 3);
//        ms.put("c", 3);
//        ms.put("c", 4);
//
//        //Iterator<Map<String, Integer>> it = new SequentialMatchIterator<>(as, bs, (a, b) -> ms.get(a).contains(b));
//        Stream<Map<String, Integer>> it = SequentialMatchIterator.createStream(as, bs, ms);
//
//        it.forEach(x -> System.out.println("seq match: " + x));
//
//
//
//        int test = 3;
//
//
//        Op opCache;
//
//        if(test != 2) {
//            opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?s { { { ?a ?a ?a } UNION {   { SELECT DISTINCT ?b { ?b ?b ?b} }   } } ?c ?c ?c } LIMIT 10")));
//            //opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT * { { { ?a ?a ?a } UNION {   { SELECT DISTINCT ?b { ?b ?b ?b} }   } } ?c ?c ?c }")));
//        } else {
//            opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT * { ?a ?a ?a }")));
//        }
//        //Op opQuery = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?s { { { ?0 ?0 ?0 } UNION { ?1 ?1 ?1 } } { { ?2 ?2 ?2 } UNION { ?3 ?3 ?3 } } { ?4 ?4 ?4 } } LIMIT 10")));
//
//        // Multiple cache matches
//
//        Op opQuery;
//        if(test == 3) {
//        // Single cache match
//            opQuery = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?0 { { { ?0 ?0 ?0 } UNION {   { SELECT DISTINCT ?1 { ?1 ?1 ?1} }   } } ?2 ?2 ?2 } LIMIT 10")));
//        } else {
//            opQuery = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("Select * { { SELECT DISTINCT ?s { { { ?0 ?0 ?0 } UNION { {   SELECT DISTINCT ?1 { ?1 ?1 ?1 } }   } } { { ?2 ?2 ?2 } UNION { ?3 ?3 ?3 } } { ?4 ?4 ?4 } } LIMIT 10 } { ?f ?c ?k } }")));
//
//        }
//
//        OpViewMatcher<Node> viewMatcher = OpViewMatcherTreeBased.create();
//        viewMatcher.put(NodeFactory.createURI("http://foo.bar"), opCache);
//        viewMatcher.lookup(opCache);
//
//
//        if(true) {
//            System.exit(0);
//        }
//
//
//        opCache = Transformer.transform(TransformJoinToConjunction.fn, Transformer.transform(TransformUnionToDisjunction.fn, opCache));
//        opQuery = Transformer.transform(TransformJoinToConjunction.fn, Transformer.transform(TransformUnionToDisjunction.fn, opQuery));
//
//        Generator<Var> generatorCache = VarGeneratorImpl2.create();
//        opCache = OpUtils.substitute(opCache, false, (op) -> SparqlCacheUtils.tryCreateCqfp(op, generatorCache));
//
//        Generator<Var> generatorQuery = VarGeneratorImpl2.create();
//        opQuery = OpUtils.substitute(opQuery, false, (op) -> SparqlCacheUtils.tryCreateCqfp(op, generatorQuery));
//
//
//        Tree<Op> cacheTree = TreeImpl.create(opCache, (o) -> OpUtils.getSubOps(o));
//        Tree<Op> queryTree = TreeImpl.create(opQuery, (o) -> OpUtils.getSubOps(o));
//
//
//
//
//        // The candidate multimapping from cache to query
//        Multimap<Op, Op> candOpMapping = HashMultimap.create();
//        List<Op> cacheLeafs = TreeUtils.getLeafs(cacheTree);
//        List<Op> queryLeafs = TreeUtils.getLeafs(queryTree);
//
//
//        if(test == 0) {
//            // Expected: a:1 - b:2
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(2));
//
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(0));
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(2));
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(3));
//
//            candOpMapping.put(cacheLeafs.get(2), queryLeafs.get(4));
//        }
//
//        if(test == 1) {
//            // Expected: a:1 - b:0
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(1));
//
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(0));
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(2));
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(3));
//        }
//
//        // test case where there the cache op is just a single node (i.e. there is no parent)
//        if(test == 2) {
//            // Expected: a:1 - b:0
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
//
//        }
//
//        if(test == 3) {
//            // Expected: a:1 - b:0
//            candOpMapping.put(cacheLeafs.get(0), queryLeafs.get(0));
//            candOpMapping.put(cacheLeafs.get(1), queryLeafs.get(1));
//            candOpMapping.put(cacheLeafs.get(2), queryLeafs.get(2));
//
//        }
//
//        Stream<OpVarMap> treeVarMappings = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, cacheTree, queryTree);
//
//        treeVarMappings.forEach(e -> {
//            Map<Op, Op> nodeMapping = e.getOpMap();
//
//            Op sourceRoot = cacheTree.getRoot();
//            Op targetNode = nodeMapping.get(sourceRoot);
//
//            if(targetNode == null) {
//                throw new RuntimeException("Could not match root node of a source tree to a node in the target tree - Should not happen.");
//            }
//
//            QuadPattern yay = new QuadPattern();
//            Node n = NodeFactory.createURI("yay");
//            yay.add(new Quad(n, n, n, n));
//            Op repl = OpUtils.substitute(queryTree.getRoot(), false, op -> {
//               return op == targetNode ? new OpQuadBlock(yay) : null;
//            });
//
//
//            System.out.println("yay: " + repl);
//            System.out.println();
//        });

    }


}
