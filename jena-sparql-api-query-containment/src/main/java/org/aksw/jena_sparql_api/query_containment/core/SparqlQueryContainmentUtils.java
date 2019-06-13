package org.aksw.jena_sparql_api.query_containment.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.commons.graph.index.jena.transform.QueryToGraph;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage2;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsageAnalyzer2Visitor;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.algebra.utils.ConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedOp;
import org.aksw.jena_sparql_api.algebra.utils.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.query_containment.index.ExpressionMapper;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOp;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpContainment;
import org.aksw.jena_sparql_api.query_containment.index.OpContext;
import org.aksw.jena_sparql_api.query_containment.index.ResidualMatching;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.query_containment.index.SparqlTreeMapping;
import org.aksw.jena_sparql_api.query_containment.index.TreeMapping;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.aksw.jena_sparql_api.views.index.OpIndex;
import org.aksw.jena_sparql_api.views.index.OpIndexerImpl;
import org.aksw.jena_sparql_api.views.index.QuadPatternIndex;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherProjectionUtils;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystemImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.jgrapht.Graph;

import com.codepoetics.protonpack.StreamUtils;
import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class SparqlQueryContainmentUtils {
    // TODO Move default parsers to some central place
    public static final SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxSPARQL_10);
    public static final SparqlElementParser elementParser = SparqlElementParserImpl.create(Syntax.syntaxSPARQL_10, null);

    public static QuadFilterPatternCanonical canonicalize(String elementStr) {
        Element element = elementParser.apply(elementStr);
        QuadFilterPatternCanonical result = canonicalize(element);
        return result;
    }


    public static QuadFilterPatternCanonical canonicalize(Element element) {
        ProjectedQuadFilterPattern pqfp = AlgebraUtils.transform(element);
        Generator<Var> generator = VarGeneratorImpl2.create();

        QuadFilterPatternCanonical result = AlgebraUtils.canonicalize2(pqfp.getQuadFilterPattern(), generator);

        return result;
    }


    public static boolean tryMatch(String viewStr, String queryStr) {
        Element viewEl = elementParser.apply(viewStr);
        Element queryEl = elementParser.apply(queryStr);

        boolean result = tryMatch(viewEl, queryEl);
        return result;

        //Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQpfc);
        //candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        //System.out.println("Done.");
    }

    public static boolean tryMatch(Element viewEl, Element queryEl) {
        QuadFilterPatternCanonical viewQfpc = canonicalize(viewEl);
        QuadFilterPatternCanonical queryQfpc = canonicalize(queryEl);

        boolean result = tryMatch(viewQfpc, queryQfpc);
        return result;


        //candidateSolutions.forEach(cs -> System.out.println("Candidate solution: " + cs));
        //System.out.println("Done.");

    }

    public static boolean tryMatch(QuadFilterPatternCanonical viewQfpc, QuadFilterPatternCanonical queryQfpc) {
        Stream<Map<Var, Var>> candidateSolutions = VarMapper.createVarMapCandidates(viewQfpc, queryQfpc)
                //.peek(foo -> System.out.println(foo))
                ;
        boolean result = candidateSolutions.count() > 0;
        return result;
    }

    public static boolean tryMatchOld(Query viewQuery, Query userQuery) {
        Element viewEl = viewQuery.getQueryPattern();
        Element userEl = userQuery.getQueryPattern();

        boolean result = tryMatch(viewEl, userEl);
        return result;
    }



    /**
     * This is the entry point to the legacy query containment check; it is superseded by tryMatch(...)
     * @param viewQuery
     * @param userQuery
     * @param qfpcMatcher
     * @return
     */
    @Deprecated
    public static boolean tryMatchOld(
            Query viewQuery,
            Query userQuery,
            BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher) {
        // Op maps without var maps do not count
        boolean result = match(viewQuery, userQuery, qfpcMatcher)
                .filter(opVarMap -> {
                	Iterable<Map<Var, Var>> varMaps = opVarMap.getVarMaps();
                	boolean r = varMaps.iterator().hasNext();
                	return r;
                })
                .count() > 0;
        return result;
    }

    public static boolean tryMatch(Query view, Query user) {
    	boolean result = tryMatch(view, user, false);
    	return result;
    }
    

    public static boolean tryMatch(Query view, Query user, boolean validate) {
    	
    	TriFunction<OpContext, OpContext, Table<Op, Op, BiMap<Node, Node>>, NodeMapperOp> nodeMapperFactory = NodeMapperOpContainment::new; //(aContext, bContext) -> new NodeMapperOpContainment(aContext, bContext);
        
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexA = QueryContainmentIndexImpl.create(nodeMapper);
        //QueryContainmentIndex<Node, DirectedGraph<Node, Triple>, Node, Op, Op> indexB = QueryContainmentIndexImpl.createFlat(nodeMapper);

//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiTreeTags = SubgraphIsomorphismIndexJena.create();
//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiFlat = SubgraphIsomorphismIndexJena.createFlat();
//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiTagBased = SubgraphIsomorphismIndexJena.createTagBased(new TagMapSetTrie<>(NodeUtils::compareRDFTerms));

//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> siiValidating = ValidationUtils.createValidatingProxy(SubgraphIsomorphismIndex.class, siiTreeTags, siiTagBased);
//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> sii = siiValidating;
//        SubgraphIsomorphismIndex<Entry<Node, Long>, DirectedGraph<Node, Triple>, Node> sii = siiTreeTags;
        
        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> sii = ExpressionMapper.createIndex(validate);
        
        //QueryContainmentIndex<Node, Var, Op, ResidualMatching, TreeMapping<Op, Op, BiMap<Var, Var>, ResidualMatching>> index = QueryContainmentIndexImpl.create(sii, nodeMapperFactory);

        SparqlQueryContainmentIndex<Node, ResidualMatching> index = SparqlQueryContainmentIndexImpl.create(sii, nodeMapperFactory);
 
        //view = QueryFactory.create("PREFIX ex: <http://ex.org/> SELECT * { ?s a ex:Person ; ex:name ?n . FILTER(contains(?n, 'fr')) }");
        //user = QueryFactory.create("PREFIX ex: <http://ex.org/> SELECT DISTINCT ?s { ?s a ex:Person ; ex:name ?n . FILTER(contains(?n, 'franz')) }");
        
        
        
        
        Node viewKey = NodeFactory.createURI("http://ex.org/view");
//        Op viewOp = Algebra.toQuadForm(Algebra.compile(view));
//        Op userOp = Algebra.toQuadForm(Algebra.compile(user));
        Op viewOp = Algebra.compile(view);
        Op userOp = Algebra.compile(user);

        

        {
        	Op op = QueryToGraph.normalizeOp(userOp, true);
        	//op = QueryToGraph.normalizeOp(op, true);
	        //VarUsageAnalyzer2Visitor varUsageAnalyzer = new VarUsageAnalyzer2Visitor();
	        Map<Op, VarUsage2> map = VarUsageAnalyzer2Visitor.analyze(op);
//	        for(Entry<Op, VarUsage2> e : map.entrySet()) {
//	        	System.out.println("VarUsage: " + e);
//	        }
//	        System.out.println("Normalized Op: " + op);
        }        
        
        

        
        index.put(viewKey, viewOp);

        Stream<Entry<Node, SparqlTreeMapping<ResidualMatching>>> tmp = index.match(userOp);

        List<Entry<Node, SparqlTreeMapping<ResidualMatching>>> matches = 
        		tmp.collect(Collectors.toList());

        boolean printOutMappings = false;
        if(printOutMappings) {
	        System.out.println("Begin of matches:");
			for(Entry<Node, SparqlTreeMapping<ResidualMatching>> match : matches) {
	        	System.out.println("  Match: " + match);
	        }
	        System.out.println("End of matches");
        }
        
        boolean hasMatches = !matches.isEmpty();
        return hasMatches;
    }



    /**
     * TODO Possibly deprecate in favor of the conjunctive query and tree based indexers.
     *
     * TODO: Somehow add the graph based qfpc matching version here...
     * The main issue is, that the qfpc indexing works different in that case...
     *
     * QfpcMatchers:
     * - VarMapper::createVarMapCandidate
     * - QueryToGraph::match
     *
     *
     *
     * @param viewQuery
     * @param userQuery
     * @return
     */
    public static Stream<OpVarMap> match(
            Query viewQuery,
            Query userQuery,
            BiFunction<QuadFilterPatternCanonical, QuadFilterPatternCanonical, Stream<Map<Var, Var>>> qfpcMatcher
        ) {
        ProjectedOp viewPop = AlgebraUtils.toProjectedOp(viewQuery);
        ProjectedOp userPop = AlgebraUtils.toProjectedOp(userQuery);

        // Check whether the view's residual op can be converted to a canonical quad filter pattern
        Op viewResOp = viewPop.getResidualOp();
        Op userResOp = userPop.getResidualOp();

        // TODO Add utility method for creating the varInfo object
//		VarInfo viewVarInfo = new VarInfo(new HashSet<>(viewPop.getProjection().getVars()), viewPop.isDistinct() ? 2 : 0);
//		VarInfo userVarInfo = new VarInfo(new HashSet<>(userPop.getProjection().getVars()), userPop.isDistinct() ? 2 : 0);

        VarInfo viewVarInfo = viewPop.getProjection();
        VarInfo userVarInfo = userPop.getProjection();

        Function<Op, OpIndex> opIndexer = new OpIndexerImpl();

        Op normViewResOp = QueryToGraph.normalizeOp(viewResOp, false);
        Op normUserResOp = QueryToGraph.normalizeOp(userResOp, false);

        OpIndex viewIndex = opIndexer.apply(normViewResOp);
        OpIndex userIndex = opIndexer.apply(normUserResOp);

        Tree<Op> viewTree = viewIndex.getTree();
        Tree<Op> userTree = userIndex.getTree();

        Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(viewIndex, userIndex);

        // TODO: Maybe this qfpc check shoud be part of the viewIndex?
        //QuadFilterPatternCanonical viewQfpc = SparqlCacheUtils.extractQuadFilterPatternCanonical(viewResOp);
        ConjunctiveQuery viewCq = normViewResOp instanceof OpExtConjunctiveQuery
                ? ((OpExtConjunctiveQuery)normViewResOp).getQfpc()
                : null;

        QuadFilterPatternCanonical viewQfpc = viewCq != null
                ? viewCq.getPattern()
                : null;

        Stream<OpVarMap> solutionStream;
        // Use tree matcher or pattern matcher
        if(viewQfpc != null) {
            // pattern matcher
            // TODO: We want an index to get all leaf candidates
            Collection<QuadPatternIndex> leafs = Sets.newIdentityHashSet();
            leafs.addAll(userIndex.getQuadPatternIndex().values());

            solutionStream = leafs.stream()
                .map(userQpIndex -> {
                    QuadFilterPatternCanonical userQfpc = userQpIndex.getQfpc();
                    Op userOp = userQpIndex.getOpRef().getNode();
                    //Iterable<Map<Var, Var>> varMapping = () -> VarMapper.createVarMapCandidates(viewQfpc, userQfpc).iterator();

                    Iterable<Map<Var, Var>> varMapping = () -> qfpcMatcher.apply(viewQfpc, userQfpc)
                            .filter(vm -> SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, vm, false))
                            .iterator();

                    Map<Op, Op> opMapping = Collections.singletonMap(viewResOp, userOp);
                    OpVarMap r = new OpVarMap(opMapping, varMapping);
                    return r;
                });


        } else {
            // tree matcher

            // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
            // In fact, we could use these features as an additional index
            solutionStream = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, viewTree, userTree);


            // TODO: Analyze query's varUsage
            //Stream<OpVarMap> result = solutionStream.map(varOpMap -> {
            solutionStream = solutionStream.map(varOpMap -> {
                // Wrap the varOpMap's iterable such that projections

                //Op viewRootOp = viewIndex.getTree().getRoot();
                Map<Op, Op> opMap = varOpMap.getOpMap();
                Op userOp = opMap.get(viewResOp);
                // TODO Precompute / Cache var usages
                //VarUsage userVarUsage = OpUtils.analyzeVarUsage(userIndex.getTree(), userOp);

                Iterable<Map<Var, Var>> varMap = () -> StreamUtils.stream(varOpMap.getVarMaps())
                        //.filter(vm -> SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarUsage, vm))
                        .filter(vm -> { 
                        	boolean r = SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, vm, false);
                			return r;
                        })
                        .iterator();

                OpVarMap r = new OpVarMap(opMap, varMap);
                return r;
            });
        }

        //result = solutionStream;

        //return result;
        return solutionStream;
    }
}