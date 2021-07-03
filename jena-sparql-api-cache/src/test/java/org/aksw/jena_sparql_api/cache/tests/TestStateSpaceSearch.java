package org.aksw.jena_sparql_api.cache.tests;

import java.io.FileNotFoundException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.combinatorics.algos.KPermutationsOfNUtils;
import org.aksw.combinatorics.collections.Combination;
import org.aksw.combinatorics.solvers.ProblemContainerNeighbourhoodAware;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.collections.FeatureMap;
import org.aksw.commons.collections.FeatureMapImpl;
import org.aksw.commons.collections.cache.RemovalListenerMultiplexer;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeImpl;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinToSequence;
import org.aksw.jena_sparql_api.algebra.transform.TransformUnionToDisjunction;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.OpRewriteViewMatcherStateful;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewMatcherMaster;
import org.aksw.jena_sparql_api.concept_cache.core.StorageEntry;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.parse.QueryExecutionFactoryParse;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.SequentialMatchIterator;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.unsorted.ExprMatcher;
import org.aksw.jena_sparql_api.utils.DnfUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.aksw.jena_sparql_api.views.index.OpIndex;
import org.aksw.jena_sparql_api.views.index.OpIndexerImpl;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOp;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherOpImpl;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystemImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.function.library.leviathan.log;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class TestStateSpaceSearch {
	private static final Logger logger = LoggerFactory.getLogger(TestStateSpaceSearch.class);


	public static void main(String[] args) throws Exception {

		//OpUtils.

		// Initialize a global custom OpExecutor which handles SERVICE <view://...> ops
		JenaExtensionViewMatcher.register();


		Resource r = new ClassPathResource("data-lorenz.nt");
		Model model = ModelFactory.createDefaultModel();
		model.read(r.getInputStream(), "http://ex.org/", "NTRIPLES");

		//OpViewMatcher<Node> viewMatcher =


		// Create an implemetation of the view matcher - i.e. an object that supports
		// - registering (Op, value) entries
		// - rewriting an Op using references to the registered ops
		RemovalListenerMultiplexer<Node, StorageEntry> removalListeners = new RemovalListenerMultiplexer<>();

		Cache<Node, StorageEntry> queryCache = CacheBuilder.newBuilder()
				.maximumSize(10000)
				.removalListener(removalListeners)
				.build();



		//Map<Node, StorageEntry> storageMap = queryCache.asMap();//OpExecutorFactoryViewMatcher.get().getStorageMap();
		OpRewriteViewMatcherStateful viewMatcherRewriter = new OpRewriteViewMatcherStateful(queryCache, removalListeners.getClients());

		// Obtain the global service map for registering temporary handlers for <view://...> SERVICEs
		// for the duration of a query execution
		// Note: JenaExtensionViewMatcher.register(); already registered this object at ARQ's global query execution context

		// A map which associates SERVICE ids with an interface for fetching slices of data.
		// Map<Node, RangedSupplier<Long, Binding>> dataSupplier;


        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model).create();
        ExecutorService executorService = Executors.newCachedThreadPool();
        //200000l
        //executorService

        qef = new QueryExecutionFactoryViewMatcherMaster(qef, viewMatcherRewriter, executorService, true);
        qef = new QueryExecutionFactoryParse(qef, SparqlQueryParserImpl.create());

        Stopwatch sw = Stopwatch.createStarted();

        for(int i = 0; i < 3; ++i) {
        	{
    	        System.out.println("Cache size before: " + queryCache.size());

    	        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> } Limit 10");
		        ResultSet rs = qe.execSelect();
    	        System.out.println(ResultSetFormatter.asText(rs));

    	        System.out.println("Cache size after: " + queryCache.size());
//	        	ResultSetFormatter.consume(rs);
        	}
//        	{
//    	        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> ; a <foo://bar> } Limit 10");
//    	        ResultSet rs = qe.execSelect();
//    	        System.out.println(ResultSetFormatter.asText(rs));
//    	        //System.out.println(t);
//            	//ResultSetFormatter.consume(rs);
//        	}
        }

        logger.info("Awaiting termination of thread pool...");
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        logger.info("done.");
	}


	public static void mainYY(String[] args) throws Exception {

		//System.out.println("VISIBLE: " + OpVars.mentionedVars(Algebra.compile(QueryFactory.create("SELECT * { {} Filter(?s = <Foo>) }"))));
		//if(true) { System.exit(0); }

		//Op opCache = Algebra.toQuadForm(Algebra.compile(QueryFactory.create("SELECT DISTINCT ?s { { { ?a ?a ?a } UNION {   { SELECT DISTINCT ?b { ?b ?b ?b} }   } } ?c ?c ?c } LIMIT 10")));

        //viewMatcher.add(opCache);
        //viewMatcher.lookup(opCache);
		Resource r = new ClassPathResource("data-lorenz.nt");
		Model model = ModelFactory.createDefaultModel();
		model.read(r.getInputStream(), "http://ex.org/", "NTRIPLES");


		//Op op = Algebra.compile(QueryFactory.create("Select Distinct * { { ?s a <http://dbpedia.org/ontology/MusicalArtist> } UNION { ?x ?p <foobar> } Optional { ?s <ex:mailbox> ?m } Optional { ?s <ex:label> ?l } Filter(?s = <foo>) } Limit 10"));
		//Op op = Algebra.compile(QueryFactory.create("Select * { ?s a <ex:Person> Optional { ?s <ex:knows> ?o Optional { ?o <ex:label> ?s } } }"));
//		Op op = Algebra.compile(QueryFactory.create("Select ((?s + ?y) As ?z) { { Select ?s (Sum(?x) As ?y) { ?s a <ex:Person> Optional { ?s <ex:knows> ?o Optional { ?o <ex:knows> ?x . Filter(?x = ?s) } } } Group By ?s } }", Syntax.syntaxARQ));
		Op op = Algebra.compile(QueryFactory.create("Select DISTINCT ((?s + ?y) As ?z) { { Select ?s (Sum(?x) As ?y) { ?s ?o ?x } Group By ?s } }", Syntax.syntaxARQ));

//		VarFinder varFinder = VarFinder.process(op);
//		System.out.println("assign: " + varFinder.getAssign());
//		System.out.println("filter: " + varFinder.getFilter());
//		System.out.println("fixed: " + varFinder.getFixed());
//		System.out.println("opt: " + varFinder.getOpt());

		//op = Transformer.transform(TransformLeftJoinToSet.fn, op);
		//op = Transformer.transform(TransformSetToLeftJoin.fn, op);
//		System.out.println(op);
		OpIndex opIndex = new OpIndexerImpl().apply(op);
		Tree<Op> tree = opIndex.getTree();
		System.out.println(op);
		System.out.println("depth: " + TreeUtils.depth(tree));
System.out.println("----- yay");
//		List<Op> leafs = TreeUtils.getLeafs(opIndex.getTree());
//		VarUsage vu = OpUtils.analyzeVarUsage(opIndex.getTree(), leafs.get(0));
//		System.out.println(vu);
//
		//if(true) { System.exit(0); }




        //OpViewMatcher viewMatcher = OpViewMatcherTreeBased.create();
//		OpRewriteViewMatcherStateful viewMatcher = new OpRewriteViewMatcherStateful();
//        //QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", "http://dbpedia.org").create();
//        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(model).create();
//        ExecutorService executorService = Executors.newCachedThreadPool();
//        qef = new QueryExecutionFactoryViewMatcherMaster(qef, viewMatcher, executorService, 200000l);
//        qef = new QueryExecutionFactoryParse(qef, SparqlQueryParserImpl.create());
//
//        Stopwatch sw = Stopwatch.createStarted();
//
//        for(int i = 0; i < 10; ++i) {
//        	{
//		        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> } Limit 10");
//		        ResultSet rs = qe.execSelect();
//	        	ResultSetFormatter.consume(rs);
//        	}
//        	{
//    	        QueryExecution qe = qef.createQueryExecution("select * { ?s a <http://dbpedia.org/ontology/MusicalArtist> ; a <foo://bar> } Limit 10");
//    	        ResultSet rs = qe.execSelect();
//    	        System.out.println(ResultSetFormatter.asText(rs));
//    	        //System.out.println(t);
//            	//ResultSetFormatter.consume(rs);
//        	}
//        }
//
//        System.out.println("DONE. - " + + sw.stop().elapsed(TimeUnit.MILLISECONDS));


//		try {
//			IntStream
//				.range(0, 10)
//				.mapToObj(i -> { if(i == 5) { throw new RuntimeException("x"); } return i;})
//				.forEach(x -> System.out.println(x));
//		} catch(Exception e) {
//			System.out.println("early exit");
//		}

	}



//    public static void main(String[] args) {
//        Multimap<String, Integer> m = HashMultimap.create();
//
//        m.put("a", 1);
//        m.put("a", 3);
//
//        m.put("b", 1);
//        m.put("b", 3);
//        m.put("b", 4);
//        F
//        Stream<CombinationStack<String, Integer, Object>> s = KPermutationsOfNUtils.kPermutationsOfN(m);
//
//        s.forEach(i -> System.out.println(i.asList()));
//    }

    //public static void


    public static <A, B, S> Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> createProblemsFromUnaryAncestors(List<A> aOps, List<B> bOps) {
        // for now the sequences must match
        int as = aOps.size();
        int bs = bOps.size();

        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> result = new ArrayList<>();
        if(as == bs) {
            for(int i = 0; i < as; ++i) {
                A a = aOps.get(i);
                B b = bOps.get(i);

                Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = SparqlViewMatcherUtils.createProblems(a, b);
                result.addAll(problems);
            }
        }

        return result;
    }






//    public static void foo() {
//        Dataset ds = DatasetFactory.create();
//
//        Model evals = ModelFactory.createDefaultModel();
//
//        JsonObject jo = null;
//        for(Entry<String, JsonElement> e : jo.entrySet()) {
//            switch(e.getKey()) {
//            case "foobar":
//                Triple x;
//                StringUtils.md5Hash("" + x);
//                Resource r = evals.createResource("http://eval1");
//                    r
//                        .addLiteral(RDF.type, r.getModel().createResource("http://verlinks.aksw.org/ontology/LinkEval"))
//                        .addLiteral(arg0, );
//                break;
//            }
//        }
//
//        ds.addNamedModel("http://myevals", evals);
//        NQuadsWriter.write(System.out, ds.asDatasetGraph().find());
//    }


    public static Entry<Map<Op, Op>, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> augmentUnaryOpMappings(
            Op sourceNode,
            Op targetNode,
            Tree<Op> sourceTree,
            Tree<Op> targetTree,
            Tree<Op> sourceMultiaryTree,
            Tree<Op> targetMultiaryTree,
            BiFunction<Op, Op, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> valueComputation
    ) {
        Stream<Combination<Op, Op, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>> stream = augmentUnaryMappings(
            sourceNode, targetNode,
            sourceTree, targetTree,
            sourceMultiaryTree, targetMultiaryTree,
            valueComputation);

        Map<Op, Op> varMapping = new HashMap<Op, Op>();
        List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();

        stream.forEach(e -> {
            Op sourceOp = e.getKey();
            Op targetOp = e.getValue();
            List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> ps = e.getSolution();

            varMapping.put(sourceOp, targetOp);
            problems.addAll(ps);
        });

        Entry<Map<Op, Op>, List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> result =
                new SimpleEntry<>(varMapping, problems);

        return result;
    }



    public static <A, B, X> Stream<Combination<A, B, X>> augmentUnaryMappings(
            A sourceNode,
            B targetNode,
            Tree<A> sourceTree,
            Tree<B> targetTree,
            Tree<A> sourceMultiaryTree,
            Tree<B> targetMultiaryTree,
            BiFunction<A, B, X> valueComputation
            )
    {
        //Op cacheLeaf = cacheLeafs.get(1);
        List<A> sourceUnaryAncestors = TreeUtils.getUnaryAncestors(sourceNode, sourceTree, sourceMultiaryTree);

        //Op queryLeaf = queryLeafs.get(1);
        List<B> targetUnaryAncestors = TreeUtils.getUnaryAncestors(targetNode, targetTree, targetMultiaryTree);

        int n = sourceUnaryAncestors.size();
        int m = targetUnaryAncestors.size();

        boolean sameSize = n == m;

        //List<Entry<Map<T, T>, Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>>>> result = new ArrayList<>(n);

        Stream<Combination<A, B, X>> result;
        if(sameSize) {
            result = IntStream.range(0, n)
                .mapToObj(i -> {
                    A sourceAncestor = sourceUnaryAncestors.get(i);
                    B targetAncestor = targetUnaryAncestors.get(i);
                    //Entry<T, T> e = new SimpleEntry<>(sourceAncestor, targetAncestor);

                    X value = valueComputation.apply(sourceAncestor, targetAncestor);

                    Combination<A, B, X> r = new Combination<A, B, X>(sourceAncestor, targetAncestor, value);
                    return r;
                });
        } else {
            result = Stream.empty();
        }

        return result;
    }

//        System.out.println("unary parents: " + unaryParents);
        //Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = createProblemsFromUnaryAncestors(cacheUnaryAncestors, queryUnaryAncestors);
//        Stream<Map<Var, Var>> solutions = VarMapper.solve(problems);
//        solutions.forEach(s -> System.out.println("found solution: " + s));


    public static void main4(String[] args) {
    	//JenaParameters.disableBNodeUIDGeneration = true;
    	//Model m = RDFDataMgr.loadModel("/home/raven/Desktop/blanknode-test.nt");
//    	Model m = ModelFactory.createDefaultModel();
//
//
//    	n.add(m);
//    	//Model n = RDFDataMgr.loadModel("/home/raven/Desktop/blanknode-test.nt");
//    	m.add(n);
//    	m.write(System.out, Lang.NTRIPLES.getName());
//
//    	System.out.println(m.size());
//    	System.exit(0);






//        List<Set<Op>> cacheTreeLevels = TreeUtils.nodesPerLevel(cacheMultiaryTree);
//        List<Set<Op>> queryTreeLevels = TreeUtils.nodesPerLevel(queryMultiaryTree);



//
//



//            Stream<List<ProblemNeighborhoodAware<Map<Var, Var>, Var>>> problemsStream = cartX.stream().map(listOfMaps -> {
//                List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> ps =
//                        listOfMaps.stream()
//                        .flatMap(x -> x.entrySet().stream())
//                        .flatMap(e -> createProblems(e.getKey(), e.getValue()).stream())
//                        .collect(Collectors.toList());
//
//                return ps;
//            });

            // For every
            //childNodeMappingCandidates.


            // TODO Go through the stack, somehow obtain all the node mappings, and create the problem instances

            //Multimap<Op, Op> fullMap = mapping
            //Iterables.concat(m.getV

//
//            System.out.println("Tree Mapping stack size: " + stack.size());
//            for(LayerMapping<Op, Op, IterableUnknownSize<Map<Op, Op>>> layer : stack) {
//                System.out.println("layerMapping: #nodeMappings: " + layer.getNodeMappings().size());
//                for(NodeMapping<Op, Op, IterableUnknownSize<Map<Op, Op>>> nodeMapping : layer.getNodeMappings()) {
//                    Op cp = nodeMapping.getParentMapping().getKey();
//                    Op qp = nodeMapping.getParentMapping().getValue();
//
//                    System.out.println("nodeMapping: " + (cp == null ? null : cp.getClass()) + " - " + (qp == null ? null : qp.getClass()));
//                    System.out.println("childMapping: " + nodeMapping.getChildMapping());
//
//                    List<ProblemNeighborhoodAware<Map<Var, Var>, Var>> ps =
//                                nodeMapping.getValue().stream()
//                                .flatMap(x -> x.entrySet().stream())
//                                .flatMap(e -> createProblems(e.getKey(), e.getValue()).stream())
//                                .collect(Collectors.toList());
//
//                    if(!ps.isEmpty()) {
//                        problems.add(ps);
//                    } else {
//                        System.out.println("Skipping empty problem");
//                    }
//
//
////                    for(Map<Op, Op> cand : nodeMapping.getValue()) {
////
////                        createProblems(
////                    }
//                }
//            }

//            problemsStream.forEach(problems -> {
//                System.out.println("# problems found: " + problems.size());
//                VarMapper.solve(problems).forEach(vm -> System.out.println("VAR MAPPING: " + vm));
//            });

                //System.out.println("  Mapping candidate: " + layer.getNodeMappings());
//                for(Entry<Op, Op> mapping : layer.entrySet()) {
//
//                    MatchingStrategyFactory<Op, Op> f = determineMatchingStrategy(mapping.getKey(), mapping.getValue());
//                    f.apply(cacheMultiaryTree.getChildren(mapping.getKey()), bs, queryMultiaryTree.getChildren(mapping.getValue()));
//
//
//                    System.out.println("layer mapping: " + layer);
//
//                    // TODO We need the clusters together with the mapping strategy
//                }


                // From the candidate mapping we now need to create the concrete mappings
                //KPermutationsOfNUtils.
                //determineMatchingStrategy(cacheOp, queryOp)

            //System.out.println("Tree mapping solution: " + m);
//        });





        //

        // tm.recurse(0, HashMultimap.create());



//        Collections.reverse(cacheTreeLevels);
//        Collections.reverse(queryTreeLevels);
//
//        int cacheMultiaryTreeDepth = cacheTreeLevels.size();
//        int queryMultiaryTreeDepth = queryTreeLevels.size();
//
//        for(int i = 0; i < cacheMultiaryTreeDepth; ++i) {
//            Set<Op> keys = cacheTreeLevels.get(cacheMultiaryTreeDepth - 1 - i);
//            Set<Op> values = queryTreeLevels.get(queryMultiaryTreeDepth - 1 - i);
//
//            candOpMapping = Multimaps.filterEntries(candOpMapping, new Predicate<Entry<Op, Op>>() {
//                @Override
//                public boolean apply(Entry<Op, Op> input) {
//                    boolean result = keys.contains(input.getKey()) && values.contains(input.getValue());
//                    return result;
//                }
//            });
//
//            Stream<ClusterStack<Op, Op, Entry<Op, Op>>> stream = KPermutationsOfNUtils.<Op, Op>kPermutationsOfN(
//                    candOpMapping,
//                    cacheMultiaryTree,
//                    queryMultiaryTree);
//
//            stream.forEach(parentMapping -> {
//               recurse(depth + 1, parentMapping);
//            });
//        }


        // Now that we have the clusters, how to proceed?



        //stream.forEach(x -> System.out.println("Candidate Solution: " + x.asList().iterator().next()));
//
//
//        // we need a mapping from leaf op to problem instance in order to determine which of the candidates to pick first
//        //Map<Op, Problem<?>> cacheOpToProblem = new HashMap<>();
//        Function<Entry<Op, Op>, Long> opMappingToCost = (e) -> 1l;
//
//        //TreeMultimap<Long, Entry<Op, Op>> costToOpMapping = TreeMultimap.create();
//        TreeMap<Long, Set<Entry<Op, Op>>> costToOpMappings = new TreeMap<>();
//
//        // pick the cheapest mapping candidate
//        // actually, this is again a problem instance - right?
//        Entry<Long, Entry<Op, Op>> pick = ProblemContainerNeighbourhoodAware.firstEntry(costToOpMappings);
//
//        TreeUtils.clusterNodesByFirstMultiaryAncestor(queryTree, candOpMapping);
//
//



        /*
         *
         * We now need to pick one of the mappings, and update the remaining ones.
         *
         * 1. pick a mapping
         *
         * 2. cluster mapping by the query's parent node
         * 3.
         *
         */

    }



    //public static void pick(})


    public static void matchSequence(Tree<Op> cacheTree, Tree<Op> queryTree, Op cacheNode, Op queryNode) {

    }

    public static void matchAnyComination() {

    }





    public static void main2(String[] args) throws FileNotFoundException {
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


        // TODO How could we combine the state space

        {
            Set<Expr> a = DnfUtils.toSetDnf(ExprUtils.parse("?a = <http://foo> && strStarts(?b, 'foo')")).iterator().next();
            Set<Expr> b = DnfUtils.toSetDnf(ExprUtils.parse("?a = <http://foo> && strStarts(?b, 'foob') && strStarts(?b, 'foobar') && ?x = ?y && ?a = <http://bar>")).iterator().next();

            System.out.println("a: " + a);
            System.out.println("b: " + b);

            CartesianProduct<Combination<Expr, Expr, Expr>> c = ExprMatcher.match(a, b);

            for(List<Combination<Expr, Expr, Expr>> com : c) {
                System.out.println("GOT COMBINATION:" + com);
            }

        }

        // We could create a graph over quads and expressions that variables

        SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);
        Element queryElement = elementParser.apply("?x <my://type> <my://Airport> ; <my://label> ?n ; ?h ?i . FILTER(langMatches(lang(?n), 'en')) . FILTER(<mp://fn>(?x, ?n))");

        Element cacheElement = elementParser.apply("?s <my://type> <my://Airport> ; ?p ?l . FILTER(?p = <my://label> || ?p = <my://name>)");


        FeatureMap<String, String> tagsToCache = new FeatureMapImpl<>();
        Op cacheOp = Algebra.compile(cacheElement);
        cacheOp = Algebra.toQuadForm(cacheOp);

        //IndexSystem<Op, Op, ?> indexSystem = IndexSystem.create();

//        Set<String> cacheFeatures = OpVisitorFeatureExtractor.getFeatures(cacheOp, (op) -> op.getClass().getSimpleName());
//
//        tagsToCache.put(cacheFeatures, "cache1");


        Op queryOp = Algebra.compile(queryElement);
        queryOp = Algebra.toQuadForm(queryOp);
        //Set<String> queryFeatures = OpVisitorFeatureExtractor.getFeatures(queryOp, (op) -> op.getClass().getSimpleName());

//        Collection<Entry<Set<String>, String>> cacheCandidates = tagsToCache.getIfSubsetOf(queryFeatures);
//
//        cacheCandidates.forEach(x -> {
//           System.out.println("cache candidate: " + x.getValue());
//        });

        SparqlViewMatcherSystemImpl cacheSystem = new SparqlViewMatcherSystemImpl();
        cacheSystem.registerView("test", cacheOp);

        cacheSystem.rewriteQuery(queryOp);

//        if(true) {
//            System.out.println("weee");
//            System.exit(0);
//        }

        ProjectedQuadFilterPattern cachePqfp = AlgebraUtils.transform(cacheElement);
        System.out.println("ProjectedQuadFilterPattern[cache]: " + cachePqfp);

        ProjectedQuadFilterPattern queryPqfp = AlgebraUtils.transform(queryElement);
        System.out.println("ProjectedQuadFilterPattern[query]: " + queryPqfp);

        Generator<Var> generator = VarGeneratorImpl2.create();
        QuadFilterPatternCanonical cacheQfpc = AlgebraUtils.canonicalize2(cachePqfp.getQuadFilterPattern(), generator);
        System.out.println("QuadFilterPatternCanonical[cache]: " + cacheQfpc);


        QuadFilterPatternCanonical queryQfpc = AlgebraUtils.canonicalize2(queryPqfp.getQuadFilterPattern(), generator);
        System.out.println("QuadFilterPatternCanonical[query]: " + queryQfpc);


        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(cacheQfpc, queryQfpc);
        candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        System.out.println("Done.");
    }
}


        //ContainmentMap<Expr, CacheEntry> featuresToCache = indexDnf(queryQfpc.getFilterDnf());


        // Index the clauses of the cache
//        FeatureMap<Expr, Multimap<Expr, Expr>> cacheIndex = SparqlCacheUtils.indexDnf(cacheQfpc.getFilterDnf());
//        FeatureMap<Expr, Multimap<Expr, Expr>> queryIndex = SparqlCacheUtils.indexDnf(queryQfpc.getFilterDnf());

//
//        Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> problems = new ArrayList<>();
//        for(Entry<Set<Expr>, Collection<Multimap<Expr, Expr>>> entry : queryIndex.entrySet()) {
//            Set<Expr> querySig = entry.getKey();
//            Collection<Multimap<Expr, Expr>> queryMaps = entry.getValue();
//
//            System.out.println("CAND LOOKUP with " + querySig);
//            Collection<Entry<Set<Expr>, Multimap<Expr, Expr>>> cands = cacheIndex.getIfSubsetOf(querySig);
//
//            for(Entry<Set<Expr>, Multimap<Expr, Expr>> e : cands) {
//                Multimap<Expr, Expr> cacheMap = e.getValue();
//                System.out.println("  CACHE MAP: " + cacheMap);
//                for(Multimap<Expr, Expr> queryMap : queryMaps) {
//                    Map<Expr, Entry<Set<Expr>, Set<Expr>>> group = MapUtils.groupByKey(cacheMap.asMap(), queryMap.asMap());
//
//                    Collection<ProblemNeighborhoodAware<Map<Var, Var>, Var>> localProblems = group.values().stream()
//                        .map(x -> {
//                            Set<Expr> cacheExprs = x.getKey();
//                            Set<Expr> queryExprs = x.getValue();
//                            ProblemNeighborhoodAware<Map<Var, Var>, Var> p = new ProblemVarMappingExpr(cacheExprs, queryExprs, Collections.emptyMap());
//
//                            //System.out.println("cacheExprs: " + cacheExprs);
//                            //System.out.println("queryExprs: " + queryExprs);
//
//                            //Stream<Map<Var, Var>> r = p.generateSolutions();
//
//                            return p;
//                        })
//                        .collect(Collectors.toList());
//
//                    problems.addAll(localProblems);
//                    //problems.stream().forEach(p -> System.out.println("COMPLEX: " + p.getEstimatedCost()));
//
//
//                    //problemStream.forEach(y -> System.out.println("GOT SOLUTION: " + y));
//
//
//
//                    //System.out.println("    QUERY MAP: " + queryMap);
//                }
//            }
//
//            //cands.forEach(x -> System.out.println("CAND: " + x.getValue()));
//        }
//
//        ProblemVarMappingQuad quadProblem = new ProblemVarMappingQuad(cacheQfpc.getQuads(), queryQfpc.getQuads(), Collections.emptyMap());
//        problems.add(quadProblem);
//
//        for(int i = 0; i < 1000; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();
//
//            ProblemContainerNeighbourhoodAware.solve(
//                    problems,
//                    Collections.emptyMap(),
//                    Map::keySet,
//                    MapUtils::mergeIfCompatible,
//                    Objects::isNull,
//                    (s) -> { System.out.println("solution: " + s); });

//            ProblemContainer<Map<Var, Var>> container = ProblemContainerImpl.create(problems);
//
//            State<Map<Var, Var>> state = container.isEmpty()
//                    ? new StateProblemContainer<Map<Var, Var>>(null, Objects::isNull, container, (a, b) -> MapUtils.mergeIfCompatible(a, b))
//                    : new StateProblemContainer<Map<Var, Var>>(Collections.emptyMap(), Objects::isNull, container, (a, b) -> MapUtils.mergeIfCompatible(a, b));
//            Stream<Map<Var, Var>> xxx = StateSearchUtils.depthFirstSearch(state, 10000);
//
//            for(Map<Var, Var> m : xxx.collect(Collectors.toList())) {
//                if(m != null) {
//                    System.out.println("SOLUTION: " + m);
//                    NodeTransform nodeTransform = new NodeTransformRenameMap(m);
//                    QuadFilterPatternCanonical foo = cacheQfpc.applyNodeTransform(nodeTransform);
//                    System.out.println("Cache after var mapping: " + foo);
//                    QuadFilterPatternCanonical diff = queryQfpc.diff(foo);
//                    System.out.println("DIFF: " + diff + "\nfrom cache " + foo + "\nand query " + queryQfpc);
//                }
//            }

            //xxx.forEach(x -> System.out.println("SOLUTION: " + x));


//            System.out.println("TIME TAKEN: " + sw.stop().elapsed(TimeUnit.MILLISECONDS));
//        }
//        System.out.println("PROBLEMS: " + problems.size());




//        System.out.println(cacheSigToExprs);
//
//        ContainmentMap<Expr, Set<Expr>> querySigToExprs = new ContainmentMapImpl<>();
//        for(Set<Expr> clause : queryQfpc.getFilterDnf()) {
//            Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
//            querySigToExprs.put(clauseSig, clause);
//        }
//
//        for(Set<Expr> queryClause : querySigToExprs.keySet()) {
//            System.out.println("CAND LOOKUP with " + queryClause);
//            Collection<Entry<Set<Expr>, Set<Expr>>> cands = cacheSigToExprs.getAllEntriesThatAreSubsetOf(queryClause);
//            cands.forEach(x -> System.out.println("CAND: " + x));
//        }

//if(false) {
//        //ClauseUtils.signaturize(clause)
//        IBiSetMultimap<Quad, Set<Set<Expr>>> queryQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(queryQfpc);
//
//        IBiSetMultimap<Quad, Set<Set<Expr>>> cacheQuadIndex = SparqlCacheUtils.createMapQuadsToFilters(cacheQfpc);
//        System.out.println("Index: " + cacheIndex);
//
//        // Features are objects that describe view
//        // A query needs to cover all features of view
//        // so it must hold that |featuresOf(query)| >= |featuresOf(cache)|
//        Set<Object> features = new HashSet<Object>();
//        cacheQuadIndex.asMap().values().stream().flatMap(cnfs -> cnfs.stream())
//            .flatMap(cnf -> cnf.stream())
//            .filter(clause -> clause.size() == 1)
//            .flatMap(clause -> clause.stream())
//            .forEach(feature -> features.add(feature));
//
//
//        FeatureMap<Object, Object> featuresToCache = new FeatureMapImpl<>();
//        featuresToCache.put(features, cacheQfpc);
//
//
//        // The problem graph
//        //Graph<Problem<Map<Var, Var>>, DefaultEdge> problemGraph = new SimpleGraph<>(DefaultEdge.class);
//
//
//
//
//
//        // Probably cache entries should be indexed using DNFs and the table system,
//        // whereas lookups could be made using CNFs
//
//        Collection<Entry<Set<Object>, Object>> candidates = featuresToCache.getIfSubsetOf(
//                new HashSet<>(Arrays.asList(
//                        ExprUtils.parse("?o = <my://Airport>"),
//                        ExprUtils.parse("?p = <my://type>")
//              )));
//
//        System.out.println("Candidates: " + candidates);
//
//
//        problems.forEach(p -> System.out.println("SOLUTIONS for " + p + " " + p.generateSolutions().collect(Collectors.toList())));
//}

        //ProblemContainerImpl<Map<Var, Var>> container = ProblemContainerImpl.create(problems);
        //container.

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


//        Expr a = ExprUtils.parse("(?z = ?x + 1)");
//        Expr b = ExprUtils.parse("?a = ?b || (?c = ?a + 1) && (?k = ?i + 1)");
        //Expr b = ExprUtils.parse("?x = ?y || (?z = ?x + 1)");
//
//        Set<Set<Expr>> ac = CnfUtils.toSetCnf(b);
//        Set<Set<Expr>> bc = CnfUtils.toSetCnf(a);

//        Problem<Map<Var, Var>> p = new ProblemVarMappingExpr(ac, bc, Collections.emptyMap());
//
//        System.out.println("p");
//        System.out.println(p.getEstimatedCost());
//        ProblemVarMappingExpr.createVarMap(a, b).forEach(x -> System.out.println(x));

//        Collection<Quad> as = Arrays.asList(new Quad(Vars.g, Vars.s, Vars.p, Vars.o));
//        Collection<Quad> bs = Arrays.asList(new Quad(Vars.l, Vars.x, Vars.y, Vars.z));
//
//
//        //Collection<Quad> cq =
//        System.out.println("q");
//        Problem<Map<Var, Var>> q = new ProblemVarMappingQuad(as, bs, Collections.emptyMap());
//        System.out.println(q.getEstimatedCost());
//
//        q.generateSolutions().forEach(x -> System.out.println(x));


        //Maps.com

//        System.out.println("pc");
//        ProblemContainerImpl<Map<Var, Var>> pc = ProblemContainerImpl.create(p, q);
//        StateProblemContainer<Map<Var, Var>> state = new StateProblemContainer<>(Collections.emptyMap(), pc, SparqlCacheUtils::mergeCompatible);
        //SearchUtils.depthFirstSearch(state, isFinal, vertexToResult, vertexToEdges, edgeCostComparator, edgeToTargetVertex, depth, maxDepth)
//        StateSearchUtils.depthFirstSearch(state, 10).forEach(x -> System.out.println(x));


        // Next level: Matching Ops


        // Problem: We can now find whether there exist variable mappings between two expressions or sets of quads
        // But the next step is to determine which exact parts of the query can be substituted
        // The thing is: We need to compute the variable mapping, but once we have obtained it,
        // we could use the state configuration that led to the solution to efficiently determine
        // the appropriate substitutions



        //p.generateSolutions().forEach(x -> System.out.println(x));
//    }


//    public static ContainmentMap<Int, Multimap<Expr, Expr>> indexDnf(QuadFilterPatternCanonical qfpc) {
//        IBiSetMultimap<Quad, Set<Set<Expr>>> index = SparqlCacheUtils.createMapQuadsToFilters(qfpc);
//
//        IBiSetMultimap<Set<Set<Expr>>, Quad> map = index.getInverse();
//
//        ContainmentMap<Expr, Multimap<Expr, Expr>> result = new ContainmentMapImpl<>();
//        for(Set<Expr> clause : dnf) {
//            Multimap<Expr, Expr> exprSigToExpr = HashMultimap.create();
//            Set<Expr> clauseSig = new HashSet<>();
//            for(Expr expr : clause) {
//                Expr exprSig = org.aksw.jena_sparql_api.utils.ExprUtils.signaturize(expr);
//                exprSigToExpr.put(exprSig, expr);
//                clauseSig.add(exprSig);
//            }
//
//            //Set<Expr> clauseSig = ClauseUtils.signaturize(clause);
//            result.put(clauseSig, exprSigToExpr);
//        }
//
//        return result;
//    }

//}
