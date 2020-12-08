package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.concepts.XExpr;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eccenca.access_control.triple_based.core.ElementTransformTripleRewrite;
import com.eccenca.access_control.triple_based.core.GenericLayer;

//class Contrib {
//	protected BinaryRelation reachingRelation;
//	protected TernaryRelation graphRelation;
//
//	public Contrib(BinaryRelation reachingRelation, TernaryRelation graphRelation) {
//		super();
//		this.reachingRelation = reachingRelation;
//		this.graphRelation = graphRelation;
//	}
//
//	public BinaryRelation getReachingRelation() {
//		return reachingRelation;
//	}
//
//	public TernaryRelation getGraphRelation() {
//		return graphRelation;
//	}
//}





//
//interface PathResolver<P extends PathResolver<P, S, T>, S, T> {
//	P parent();
//	P step(S step);
//	T value();
//}
//
//interface StepResolver<S, C> {
//	C resolveContrib(S step);
//}
//
//
//class ParentLikn
//
//class PathResolverSimple<P extends PathResolverSimple<P>>
//	implements PathResolver<P, P_Path0, BinaryRelation>
//{
//
//	@Override
//	public P parent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public P step(P_Path0 step) {
//		BinaryRelationImpl.create(p)
//	}
//
//	@Override
//	public BinaryRelation value() {
//
//
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}




/**
 * Notes on path resolutions:
 * 	- Aliased paths do not appear to make sense here:
 *    - One might think that aliases could be used to resolve properties in templates of
 *      partitioned queries such as ?s rdfs:label ?v1, ?v2.
 *      (E.g. consider a base table with multiple columns of alternative labels).
 *      If this is the way it is mapped, then we simply accept it here.
 *      There is no need to resolve rdfs:label to e.g. only ?v1 here - if this is desired, the place to
 *      where to "fix" this is in the partitioned query.
 *    - There is also does not appear to be a good reason / user case
 *      for aliases to affect the naming of variables:
 *      The output of the resolution in a virtual RDF graph according to the partitioned queries.
 *      In a virtual RDF graph, the naming of the variables are meaningless anyway,
 *      as the rewriting system on top just cares about subject, predicate and object positions
 *      but not how they are named.
 *
 *
 * @author raven
 *
 */
public class VirtualPartitionedQuery {
    private static final Logger logger = LoggerFactory.getLogger(VirtualPartitionedQuery.class);

//
//	public static void rewrite(Collection<PartitionedQuery1> views, Iterable<Entry<P_Path0, String>> aliasedPath) {
//		// Rewrite a path over a collection of partitioned query views
//
//
//
//		//return null;
//	}
//
//	public void step(Collection<PartitionedQuery1> views, P_Path0 step, String alias) {
//		for(PartitionedQuery1 pq : views) {
//
//		}
//	}
//
//
//	// Note: The code below may not work with literals in the template due to
//	// jena not allowing literals to act as resources
//	// but actually its a pointless limitation for our purposes
//	public Resolver createResolver(PartitionedQuery1 pq, Iterable<? extends P_Path0> path) {
//		Node rootNode = pq.getPartitionVar();
//
//		Query query = pq.getQuery();
//		Template template = query.getConstructTemplate();
//		GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
//		GraphUtil.add(graphVar, template.getTriples());
//		Model model = ModelFactory.createModelForGraph(graphVar);
//
//		Resource root = model.getRDFNode(rootNode).asResource();
//		System.out.println(root.listProperties().toList());
//
//		Collection<RDFNode> starts = Collections.singleton(root);
//		for(P_Path0 step : path) {
////			Property p = ResourceUtils.getProperty(step);
//			List<RDFNode> targets =
//				starts.stream().flatMap(s ->
//					ResourceUtils.listPropertyValues(s.asResource(), step).toList().stream())
//				.collect(Collectors.toList());
//			starts = targets;
//		}
//
//
//		//Element basePattern = query.getQueryPattern();
//
//		Set<Node> result = starts.stream().map(RDFNode::asNode).collect(Collectors.toSet());
//		return result;
//	}
//
////	public static Set<Var> resolve(PartitionedQuery1 pq, Collection<Var> startVars, P_Path0 step) {
////
////	}
//
//
//	public static Set<Var> resolve() {
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//
//		}
//	}
//
//


//	public static Resolver createResolver(PartitionedQuery1 pq) {
//		RDFNode node = toRdfModel(pq);
//		Resolver result = new ResolverTemplate(pq, Collections.singleton(node));
//		return result;
//	}



//	public void step(SimplePath basePath, PartitionedQuery1 pq, P_Path0 step, boolean isFwd, String alias) {
//		System.out.println(root.listProperties().toList());
//
//		Property p = ResourceUtils.getProperty(step);
//		List<RDFNode> targets = ResourceUtils.listPropertyValues(root, step).toList();
//
//		Element basePattern = query.getQueryPattern();
//		//Relation baseRelation = RelationImpl.create(basePattern, PatternVars.vars(basePattern));
//
//		//FacetedQueryGenerator.createRelationForPath(PathToRelationMapper<P> mapper, PathAccessor<P> pathAccessor, P childPath, boolean includeAbsent) {
//
//
//		List<TernaryRelation> trs;
//		for(RDFNode target : targets) {
//			// Generate the triple pattern (target, p, o)
//			Var var = (Var)target.asNode();
//			System.out.println(var);
//
//			BinaryRelation br =
//				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
//				.joinOn(var).with(new Concept(basePattern, var))
//				.toBinaryRelation();
//
//		}
//
////		// Resolve the path to a
////		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
////		PathToRelationMapper<SimplePath> mapper = new PathToRelationMapper<>(pathAccessor, "w");
////
////		basePath.
////		mapper.getOverallRelation(path);
//
////		BinaryRelation br =
////				BinaryRelationImpl.create(var, Vars.p, Vars.o, isFwd)
////				.joinOn(var).with(new Concept(basePattern, var))
////				.toBinaryRelation();
//
//
//
//		System.out.println(ResourceUtils.listPropertyValues(root, step).toList());
//	}

    public static TernaryRelation unionTernary(Collection<? extends TernaryRelation> items) {
        Relation tmp = union(items, Arrays.asList(Vars.s, Vars.p, Vars.o));
        TernaryRelation result = tmp.toTernaryRelation();
        return result;
    }

    /**
     * Create a union from a given collection of unary relations
     * If the collection is empty, return a relation with an empty (unit) table.
     *
     * TODO Move to ConceptUtils
     *
     * @param relations
     * @return
     */
    public static UnaryRelation unionUnary(Collection<? extends UnaryRelation> relations) {
        Relation tmp = VirtualPartitionedQuery.union(relations, Collections.singletonList(Vars.s));
        UnaryRelation result = tmp.toUnaryRelation();
        return result;

//        Set<Var> mentionedVars = relations.stream()
//                .map(Relation::getVarsMentioned)
//                .flatMap(Collection::stream)
//                .collect(Collectors.toSet());
//        UnaryRelation result;
//
//        if (relations.isEmpty()) {
//            result = new Concept(new ElementData(), Vars.x);
//        } else {
//
//            UnaryRelation first = relations.iterator().next();
//            Var tgtVar = first.getVar();
//            List<Var> tgtVars = Collections.singletonList(tgtVar);
//
//            result = VirtualPartitionedQuery.union(relations, tgtVars).toUnaryRelation();

//            List<Element> elements = relations.stream()
//                    .map(r -> RelationUtils.rename(r, tgtVars))
//                    .map(Relation::toUnaryRelation)
//                    .map(Relation::getElement)
//                    .collect(Collectors.toList());

//            Element e = ElementUtils.unionIfNeeded(elements);

//            result = new Concept(e, tgtVar);
//        }
//        return result;
    }

    public static Relation union(Collection<? extends Relation> items, List<Var> proj) {

        // TODO Handle the case where items is empty
        // Option 1: Inject FILTER(false) (but this does not project vars)
        // Option 2: Inject VALUES(proj) { }

        List<Element> elements = items.stream()
                .map(e -> RelationUtils.rename(e, proj))
                .map(Relation::getElement)
                .collect(Collectors.toList());

        Element e = ElementUtils.unionIfNeeded(elements);

        Relation result = new RelationImpl(e, proj);
        return result;
    }


//	public static Query rewrite(Resolver resolver, boolean isFwd, Query query) {
//		Collection<TernaryRelation> views = resolver.getContrib(true);
//
//		TernaryRelation tr = unionTernary(views);
////		System.out.println(tr);
//
//		GenericLayer layer = GenericLayer.create(tr);
//
//		Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
//		Query result = DataQueryImpl.rewrite(raw, DataQueryImpl.createDefaultRewriter()::rewrite);
//
//		if(false) {
//			System.out.println("Views:");
//			for(TernaryRelation view : views) {
//				System.out.println(view);
//			}
//		}
//
//		return result;
//	}
//

    public static Query rewrite(Collection<TernaryRelation> views, Query query) {
//		Resolver resolver = createResolver(view, viewVar);
//		Query result = rewrite(resolver, true, query);
        TernaryRelation tr = unionTernary(views);
//		System.out.println(tr);

        GenericLayer layer = GenericLayer.create(tr);

        Query raw = ElementTransformTripleRewrite.transform(query, layer, true);
        logger.debug("Query over View: Raw rewritten query:\n" + raw);

        Query result = QueryUtils.rewrite(raw, AlgebraUtils.createDefaultRewriter()::rewrite);
        logger.debug("Query over View: Final rewritten query:\n" + result);

        return result;
    }


    /**
     *
     * @return The updated partitioned query with the variable set to the target of the path
     *
     * TODO Maybe we want to return a PartitionedQuery2 - with source and target var
     */
    /*
    public static PartitionedQuery1 extendQueryWithPath(PartitionedQuery1 base, AliasedPath path) {
        Var targetVar = Var.alloc("todo-fresh-var");

        ResolverNode node = ResolverNodeImpl.from(base, null);
        ResolverNode target = node.walk(path);

        Collection<BinaryRelation> rawBrs = target.getPaths();

        // Set the target variable of the paths to the desired alias
//		Collection<BinaryRelation> brs = rawBrs.stream()
//				.map(br -> RelationUtils.rename(br, Arrays.asList(br.getSourceVar(), targetVar)).toBinaryRelation())
//				.collect(Collectors.toList());

        for(BinaryRelation br : rawBrs) {
            System.out.println("Relation: " + br);
        }

        return null;
    }
    */

    /**
     * Convert each triple pattern occuring in the template of a SPARQL construct query
     * into a ternary relation. This is a somewhat poor-man's approach to creating views over rdf data:
     * An improved rewriter would not treat the triple patterns in isolation, but rather take care of
     * doing self-join elimination if multiple triple patterns of a view match that of a query.
     *
     *
     */
    public static Collection<TernaryRelation> toViews(Query query) {
        if(!query.isConstructType() || query.isConstructQuad()) {
            throw new RuntimeException("Construct query (without quads) expected");
        }

        Op op = Algebra.compile(query);

        Set<Var> visibleVars = OpVars.visibleVars(op);
        Generator<Var> gen = VarGeneratorBlacklist.create(visibleVars);

        Collection<TernaryRelation> result = new ArrayList<>();
        Template template = query.getConstructTemplate();
        //BasicPattern bgp = template.getBGP();
        //TransformReplaceConstants.transform(new OpBGP(bgp));


        Element pattern = query.getQueryPattern();
        for(Triple t : template.getTriples()) {
            List<Node> nodes = TripleUtils.tripleToList(t);
            Map<Node, Var> nodeToVar = new HashMap<>();
            Map<Node, Var> substs = TransformReplaceConstants.transform(nodeToVar, nodes, gen);

            Triple newT = NodeTransformLib.transform(new NodeTransformRenameMap(substs), t);

            Element newE;
            if(substs.isEmpty()) {
                newE = pattern;
            } else {
                ElementGroup tgt = new ElementGroup();
                ElementUtils.copyElements(tgt, pattern);

                // Add the BINDs afterwards in order to get a nicer algebra:
                // We get extend(subOp, bindings) instead of join(extend(unit, bindings), subOp)
                for(Entry<Node, Var> e : substs.entrySet()) {
                    tgt.addElement(new ElementBind(e.getValue(), NodeValue.makeNode(e.getKey())));
                }

                newE = tgt;
            }


            TernaryRelation tr = new TernaryRelationImpl(newE,
                    (Var)newT.getSubject(),
                    (Var)newT.getPredicate(),
                    (Var)newT.getObject());

            result.add(tr);
        }

        return result;
    }


/*
    public static void main(String[] args) {
//CONSTRUCT { ?s ?p ?o } WHERE {?x <http://wikiba.se/ontology#claim> ?s . ?x ?p ?o }



        if(true) {
            List<TernaryRelation> views = Arrays.asList(
                //new TernaryRelationImpl(Concept.parseElement("{ ?s ?p ?o }", null), Vars.s, Vars.p, Vars.o),
                new TernaryRelationImpl(Concept.parseElement(
                        "{ ?x <http://wikiba.se/ontology#claim> ?s"
                        + ". ?x ?p ?o }", null), Vars.s, Vars.p, Vars.o)
            );

        //Query view = QueryFactory.create("CONSTRUCT {?s ?p ?o } { ?s ?pRaw ?o . BIND(URI(CONCAT('http://foobar', STR(?pRaw))) AS ?p) }");
        //PartitionedQuery1 pq = PartitionedQuery1.from(view, Vars.s);
        //Resolver resolver = Resolvers.from(pq);
//			FILTER(?s = <http://www.wikidata.org/prop/P299>)
        String queryStr = "SELECT ?s ?o { ?s a <http://www.w3.org/2002/07/owl#ObjectProperty> ; <http://www.w3.org/2000/01/rdf-schema#label> ?o . FILTER(?s = <http://www.wikidata.org/prop/P299>)}";

        Query example1 = rewrite(
                views,
                QueryFactory.create(queryStr));
        System.out.println("Example 1\n" + example1);

        try(RDFConnection conn = RDFConnectionFactory.connect("https://query.wikidata.org/sparql")) {

            //example1 = DataQueryImpl.rewrite(example1, DataQueryImpl.createDefaultRewriter()::rewrite);
            try(QueryExecution qe = conn.query(example1)) {
                System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        }

        return;
        }


        Query view = QueryFactory.create("CONSTRUCT { ?p <http://facetCount> ?c } { { SELECT ?p (COUNT(?o) AS ?c) { ?s ?p ?o } GROUP BY ?p } }");
        PartitionedQuery1 pq = PartitionedQuery1.from(view, Vars.p);
        Resolver resolver = Resolvers.from(pq);

        if(false) {

        Query example1 = rewrite(
                resolver
                    .getRdfGraphSpec(true),
                QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
        System.out.println("Example 1\n" + example1);

        Query example2 = rewrite(
                resolver
                    .getRdfGraphSpec(true),
                QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
        System.out.println("Example 2\n" + example2);

        Query example3 = rewrite(
                resolver
                    .resolve(new P_Link(NodeFactory.createURI("http://facetCount")))
                    .getRdfGraphSpec(true),
                QueryFactory.create("SELECT ?x ?y ?z { ?x ?y ?z }"));
        System.out.println("Example 3\n" + example3);

        Query example4a = rewrite(
                resolver
                    .resolve(new P_Link(NodeFactory.createURI("http://facetCount")))
                    .getRdfGraphSpec(true),
                QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
        System.out.println("Example 4a\n" + example4a);
        Query example4b = rewrite(
                resolver
                    .resolve(new P_Link(NodeFactory.createURI("http://facetCount")), "someAlias")
                    .getRdfGraphSpec(true),
                QueryFactory.create("SELECT DISTINCT ?y { ?x ?y ?z }"));
        System.out.println("Example 4b\n" + example4b);
        }

        // TODO We may need to tag alias as whether it corresponds to a fixed var name
        // or a relative path id
//		System.out.println(
//				resolver
//					.resolve(new P_Link(NodeFactory.createURI("http://facetCount")), "p")
//					.resolve(new P_Link(NodeFactory.createURI("http://label")), "labelAlias")
//					.getPaths());

        AliasedPath path = PathBuilderNode.start()
            .fwd("http://facetCount").viaAlias("a")
            .fwd("http://label").one()//viaAlias("b")
            .aliasedPath();

        if(false) {
            path = PathBuilderNode.start()
                    .fwd("http://facetCount").one()
                    .fwd("http://label").one()
                    .aliasedPath();
        }

        System.out.println("built path: " + path);


        // High level API:
//		System.out.println("Paths: " + (ResolverNode.from(resolver)
//			.fwd("http://facetCount").viaAlias("a")
//			.fwd("http://label").viaAlias("b")
//			.getPaths());

        System.out.println(pq);
        extendQueryWithPath(pq, path);

//
//		System.out.println(resolver
//			.resolve(new P_Link(NodeFactory.createURI("http://facetCount")))
//			.getPaths());

    }
    */

    static class GeneralizedStep {
        boolean isFwd;
        XExpr expr;
    }

    //processor.step(pq, new P_Link(NodeFactory.createURI("http://facetCount")), true, "a");


    //VirtualPartitionedQuery processor = new VirtualPartitionedQuery();



//	Query query = QueryFactory.create("CONSTRUCT { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party } { ?city <http://hasMayor> ?mayor . ?mayor <http://hasParty> ?party }");
//	PartitionedQuery1 pq = new PartitionedQuery1(query, Var.alloc("city"));
//	Resolver resolver = createResolver(pq);
//	resolver = resolver.resolve(new P_Link(NodeFactory.createURI("http://hasMayor")));


}

