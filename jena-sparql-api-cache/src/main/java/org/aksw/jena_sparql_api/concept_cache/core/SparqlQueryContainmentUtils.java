package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.collections.trees.Tree;
import org.aksw.jena_sparql_api.algebra.transform.TransformDistributeJoinOverUnion;
import org.aksw.jena_sparql_api.algebra.transform.TransformJoinToSequence;
import org.aksw.jena_sparql_api.algebra.transform.TransformUnionToDisjunction;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.concept_cache.op.OpUtils;
import org.aksw.jena_sparql_api.sparql.algebra.mapping.VarMapper;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherProjectionUtils;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherUtils;
import org.aksw.jena_sparql_api.views.index.OpIndex;
import org.aksw.jena_sparql_api.views.index.OpIndexerImpl;
import org.aksw.jena_sparql_api.views.index.OpViewMatcherTreeBased;
import org.aksw.jena_sparql_api.views.index.QuadPatternIndex;
import org.aksw.jena_sparql_api.views.index.SparqlViewMatcherSystemImpl;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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
		ProjectedQuadFilterPattern pqfp = SparqlCacheUtils.transform(element);
        Generator<Var> generator = VarGeneratorImpl2.create();

        QuadFilterPatternCanonical result = SparqlCacheUtils.canonicalize2(pqfp.getQuadFilterPattern(), generator);

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


	public static ProjectedOp toProjectedOp(Query query) {
		Op op = Algebra.compile(query);
//		op = Transformer.transform(TransformUnionToDisjunction.fn, op);

		// Push down joins until there is no more change
		Op current;
		do {
			current = op;
			op = TransformDistributeJoinOverUnion.transform(current);
		} while(!current.equals(op));


//		op = Transformer.transform(TransformUnionToDisjunction.fn, op);
		op = Transformer.transform(new TransformMergeBGPs(), op);
		op = Algebra.toQuadForm(op);

		System.out.println("asQuery: "+ OpAsQuery.asQuery(op));
		ProjectedOp result = SparqlCacheUtils.cutProjection(op);
		return result;
	}


	public static boolean tryMatch(Query viewQuery, Query userQuery) {
		// Op maps without var maps do not count
		boolean result = match(viewQuery, userQuery)
				.filter(opVarMap -> opVarMap.getVarMaps().iterator().hasNext())
				.count() > 0;
		return result;
	}

	public static Stream<OpVarMap> match(Query viewQuery, Query userQuery) {
		ProjectedOp viewPop = toProjectedOp(viewQuery);
		ProjectedOp userPop = toProjectedOp(userQuery);

		// Check whether the view's residual op can be converted to a canonical quad filter pattern
		Op viewResOp = viewPop.getResidualOp();
		Op userResOp = userPop.getResidualOp();

		// TODO Add utility method for creating the varInfo object
		VarInfo viewVarInfo = new VarInfo(new HashSet<>(viewPop.getProjection().getVars()), viewPop.isDistinct() ? 2 : 0);

		VarInfo userVarInfo = new VarInfo(new HashSet<>(userPop.getProjection().getVars()), userPop.isDistinct() ? 2 : 0);

    	Function<Op, OpIndex> opIndexer = new OpIndexerImpl();

    	OpIndex viewIndex = opIndexer.apply(OpViewMatcherTreeBased.normalizeOp(viewResOp));
    	OpIndex userIndex = opIndexer.apply(OpViewMatcherTreeBased.normalizeOp(userResOp));

    	Tree<Op> viewTree = viewIndex.getTree();
    	Tree<Op> userTree = userIndex.getTree();

        Multimap<Op, Op> candOpMapping = SparqlViewMatcherSystemImpl.getCandidateLeafMapping(viewIndex, userIndex);

        // TODO: Maybe this qfpc check shoud be part of the viewIndex?
		QuadFilterPatternCanonical viewQfpc = SparqlCacheUtils.extractQuadFilterPatternCanonical(viewResOp);

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
					Iterable<Map<Var, Var>> varMapping = () -> VarMapper.createVarMapCandidates(viewQfpc, userQfpc).iterator();
					Map<Op, Op> opMapping = Collections.singletonMap(viewResOp, userOp);
					OpVarMap r = new OpVarMap(opMapping, varMapping);
					return r;
				});


		} else {
			// tree matcher

	        // TODO: Require a complete match of the tree - i.e. cache and query trees must have same number of nodes / same depth / some other criteria that can be checked quickly
	        // In fact, we could use these features as an additional index
	        solutionStream = SparqlViewMatcherUtils.generateTreeVarMapping(candOpMapping, viewTree, userTree);
		}

		// TODO: Analyze query's varUsage
		Stream<OpVarMap> result = solutionStream.map(varOpMap -> {
			// Wrap the varOpMap's iterable such that projections

			//Op viewRootOp = viewIndex.getTree().getRoot();
			Map<Op, Op> opMap = varOpMap.getOpMap();
			Op userOp = opMap.get(viewResOp);
			// TODO Precompute / Cache var usages
			//VarUsage userVarUsage = OpUtils.analyzeVarUsage(userIndex.getTree(), userOp);

			Iterable<Map<Var, Var>> varMap = () -> StreamUtils.stream(varOpMap.getVarMaps())
					//.filter(vm -> SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarUsage, vm))
					.filter(vm -> SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, vm))
					.iterator();

			OpVarMap r = new OpVarMap(opMap, varMap);
			return r;
		});

		return result;
	}
}