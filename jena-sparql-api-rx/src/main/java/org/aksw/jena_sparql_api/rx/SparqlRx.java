package org.aksw.jena_sparql_api.rx;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.utils.QueryGenerationUtils;
import org.aksw.jena_sparql_api.http.HttpExceptionUtils;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;

/**
 * Utilities for wrapping SPARQL query execution with flows.
 *
 * @author raven
 *
 */
public class SparqlRx {

    private static final Logger logger = LoggerFactory.getLogger(SparqlRx.class);

    public static <K, X> Flowable<Entry<K, List<X>>> groupByOrdered(
            Flowable<X> in, Function<X, K> getGroupKey) {

          Object[] current = {null};
          Object[] prior = {null};
          PublishProcessor<K> boundaryIndicator = PublishProcessor.create();

          return in
             .doOnComplete(boundaryIndicator::onComplete)
             .doOnNext(item -> {
                    K groupKey = getGroupKey.apply(item);
                    boolean isEqual = Objects.equal(current, groupKey);

                    prior[0] = current[0];
                    if(prior[0] == null) {
                        prior[0] = groupKey;
                    }

                    current[0] = groupKey;


                    if(!isEqual) {
                        boundaryIndicator.onNext(groupKey);
                    }
              })
              .buffer(boundaryIndicator)
              .map(buffer -> {
                  K tmp = (K)prior[0];
                  K groupKey = tmp;

                  return Maps.immutableEntry(groupKey, buffer);
              });

    }

    /**
     *
     *
     * @param vars
     * @return
     */
    public static Function<List<Binding>, Table> createTableBuffer(List<Var> vars) {
        Function<List<Binding>, Table> result = rows -> new TableData(vars, rows);

        return result;
    }

    public static <T> void processExecSelect(FlowableEmitter<T> emitter, QueryExecution qex, Function<? super ResultSet, ? extends T> next) {
        Query q = qex.getQuery();
        try(QueryExecution qe = qex) {
            emitter.setCancellable(qe::abort);
            ResultSet rs = qe.execSelect();
            while(!emitter.isCancelled() && rs.hasNext()) {
                T binding = next.apply(rs);
                emitter.onNext(binding);
            }
            emitter.onComplete();
        } catch (Exception e) {
            Exception f = HttpExceptionUtils.makeHumanFriendly(e);
            emitter.onError(new Throwable("Error executing " + q, f));
        }
    }


    public static void processExecConstructTriples(FlowableEmitter<Triple> emitter, QueryExecution qex) {
        try(QueryExecution qe = qex) {
            emitter.setCancellable(qe::abort);
            Iterator<Triple> it = qe.execConstructTriples();
            while(!emitter.isCancelled() && it.hasNext()) {
                Triple item = it.next();
                emitter.onNext(item);
            }
            emitter.onComplete();
        } catch (Exception e) {
            emitter.onError(e);
        }
    }

//
//	public static void processExecConstructTriples(SingleEmitter<ResultSet> emitter, QueryExecution qe) {
//		try {
//			emitter.setCancellable(qe::abort);
//			Iterator<Triple> = qe.execConstructTriples();
//			emitter.onSuccess(rs);
//			//emitter.onComplete();
//		} catch (Exception e) {
//			emitter.onError(e);
//		}
//	}

    public static <T> Flowable<T> execSelect(Supplier<QueryExecution> qes, Function<? super ResultSet, ? extends T> next) {
        Flowable<T> result = Flowable.create(emitter -> {
            QueryExecution qe = qes.get();
            processExecSelect(emitter, qe, next);
            //new Thread(() -> process(emitter, qe)).start();
        }, BackpressureStrategy.BUFFER);

        return result;
    }

    public static Flowable<Binding> execSelectRaw(Supplier<QueryExecution> qes) {
        return execSelect(qes, ResultSet::nextBinding);
    }

    public static Flowable<QuerySolution> execSelect(Supplier<QueryExecution> qes) {
        return execSelect(qes, ResultSet::next);
    }

    public static Flowable<QuerySolution> execSelect(RDFConnection conn, String queryStr) {
        return execSelect(() -> conn.query(queryStr), ResultSet::next);
    }

    public static Flowable<QuerySolution> execSelect(RDFConnection conn, Query query) {
        return execSelect(() -> conn.query(query), ResultSet::next);
    }

    public static Flowable<Triple> execConstructTriples(Supplier<QueryExecution> qes) {
        Flowable<Triple> result = Flowable.create(emitter -> {
            QueryExecution qe = qes.get();
            processExecConstructTriples(emitter, qe);
            //new Thread(() -> process(emitter, qe)).start();
        }, BackpressureStrategy.BUFFER);

        return result;
    }

    public static Entry<List<Var>, Flowable<Binding>> mapToFlowable(ResultSet rs) {
        Iterator<Binding> it = new IteratorResultSetBinding(rs);
        Iterable<Binding> i = () -> it;

        List<Var> vars = VarUtils.toList(rs.getResultVars());

        Flowable<Binding> flowable = Flowable.fromIterable(i);
        Entry<List<Var>, Flowable<Binding>> result = new SimpleEntry<>(vars, flowable);
        return result;
    }

    public static Flowable<Binding> mapToBinding(ResultSet rs) {
        Entry<List<Var>, Flowable<Binding>> e = mapToFlowable(rs);
        Flowable<Binding> result = e.getValue();
        return result;
    }

//	public static Flowable<Binding> mapToBinding(ResultSet rs) {
//		Iterator<Binding> it = new IteratorResultSetBinding(rs);
//		Iterable<Binding> i = () -> it;
//		return Flowable.fromIterable(i);
//	}

    /**
     * Create a grouping function
     *
     * Usage:
     * flowable
     * 	.groupBy(createGrouper(Arrays.asList(... yourVars ...)))
     *
     * @param vars
     * @param retainNulls
     * @return
     */
    public static Function<Binding, Binding> createGrouper(Collection<Var> vars, boolean retainNulls) {
        return b -> {
            BindingHashMap groupKey = new BindingHashMap();
            for(Var k : vars) {
                Node v = b.get(k);
                if(v != null || retainNulls) {
                    groupKey.add(k, v);
                }
            }
            return groupKey;
        };
    }

    public static Function<Binding, Node> createGrouper(Var var) {
        return b -> {
            Node groupKey = b.get(var);
            return groupKey;
        };
    }

//	public static Flowable<Table> groupBy(Flowable<Binding> )

    // /**
    // * Mapping that includes
    // *
    // */
    // public static Flowable<Entry<List<Var>, Binding>>(List<Var> vars, ) {
    //
    // }

//	public static void main(String[] args) {
//		// Some tests for whether timeouts actually work - so far it worked...
//		String queryStr = "CONSTRUCT { ?p a ?c } { { SELECT ?p (COUNT(DISTINCT ?s) AS ?c) { ?s ?p ?o } GROUP BY ?p } }";
//		//String queryStr = "CONSTRUCT WHERE { ?s ?p ?o . ?x ?y ?z }";
//		Query query = QueryFactory.create(queryStr);
//
//		RDFConnection conn = RDFConnectionFactory.connect("http://localhost:8890/sparql");
//		List<RDFNode> rdfNodes = SparqlRx.execPartitioned(conn, Vars.p, query, true)
//			.timeout(300, TimeUnit.MILLISECONDS)
//			.toList()
//			.blockingGet();
//
//		System.out.println(rdfNodes);
//	}

    public static void main2(String[] args) {
//		List<Entry<Integer, List<Entry<Integer, Integer>>>> list = groupByOrdered(Flowable.range(0, 10).map(i -> Maps.immutableEntry((int)(i / 3), i)),
//		e -> e.getKey())
//		.toList().blockingGet();


        Integer currentValue[] = {null};
        boolean isCancelled[] = {false};

        Flowable<Entry<Integer, List<Integer>>> list = Flowable
                .range(0, 10)
                .doOnNext(i -> currentValue[0] = i)
                .doOnCancel(() -> isCancelled[0] = true)
                .map(i -> Maps.immutableEntry((int)(i / 3), i))
                .lift(new OperatorOrderedGroupBy<Entry<Integer, Integer>, Integer, List<Integer>>(Entry::getKey, groupKey -> new ArrayList<>(), (acc, e) -> acc.add(e.getValue())));

        Predicate<Entry<Integer, List<Integer>>> p = e -> e.getKey().equals(1);
        list.takeUntil(p).subscribe(x -> System.out.println("Item: " + x));

        System.out.println("Value = " + currentValue[0] + ", isCancelled = " + isCancelled[0]);

//		Disposable d = list.defe
//
//		Iterator<Entry<Integer, List<Integer>>> it = list.iterator();
//		for(int i = 0; i < 2 && it.hasNext(); ++i) {
//			Entry<Integer, List<Integer>> item = it.next();
//			System.out.println("Item: " + item);
//		}
//
//
//		System.out.println("List: " + list);


        PublishProcessor<String> queue = PublishProcessor.create();
        queue.buffer(3).subscribe(x -> System.out.println("Buffer: " + x));

        for(int i = 0; i < 10; ++i) {
            String item = "item" + i;
            System.out.println("Adding " + item);
            queue.onNext(item);
        }
        queue.onComplete();


        if(true) {
            return;
        }

        for(int j = 0; j < 10; ++j) {
            int i[] = { 0 };
            System.out.println("HERE");
            execSelectRaw(() -> org.apache.jena.query.QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql",
                    "SELECT * { ?s a <http://dbpedia.org/ontology/Person> }"))
                            .takeUntil(b -> i[0] == 10).subscribe(x -> {
                                i[0]++;
                                System.out.println("x: " + x);

                            });
        }
        // NOTE This way, the main thread will terminate before the queries are processed
    }

    public static Single<Number> fetchNumber(SparqlQueryConnection qef, Query query, Var var) {
        return SparqlRx.execSelectRaw(() -> qef.query(query))
                .map(b -> b.get(var))
                .map(countNode -> ((Number)countNode.getLiteralValue()))
                .map(Optional::ofNullable)
                .single(Optional.empty())
                .map(Optional::get); // Should never be null here
    }


    public static Single<Range<Long>> fetchCountConcept(SparqlQueryConnection qef, Concept concept, Long itemLimit, Long rowLimit) {

        Var outputVar = ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Query countQuery = ConceptUtils.createQueryCount(concept, outputVar, xitemLimit, xrowLimit);

        return SparqlRx.fetchNumber(qef, countQuery, outputVar)
                .map(count -> SparqlRx.toRange(count.longValue(), xitemLimit, xrowLimit));
    }
//	return ReactiveSparqlUtils.execSelect(() -> qef.createQueryExecution(countQuery))
//        	.map(b -> b.get(outputVar))
//        	.map(countNode -> ((Number)countNode.getLiteralValue()).longValue())
//        	.map(count -> {
//        		boolean mayHaveMoreItems = rowLimit != null
//        				? true
//        				: itemLimit != null && count > itemLimit;
//
//                Range<Long> r = mayHaveMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count);
//                return r;
//        	})
//        	.single(null);

//    public static Single<Range<Long>> fetchCountQuery(SparqlQueryConnection conn, Query query, Long itemLimit, Long rowLimit) {
//    	Single<Range<Long>> result = fetchCountQuery(conn, query, itemLimit, rowLimit);
//    	return result;
//    }

    public static Single<Range<Long>> fetchCountQueryPartition(SparqlQueryConnection qef, Query query, Collection<Var> partitionVars, Long itemLimit, Long rowLimit) {

        //Var outputVar = Var.alloc("_count_"); //ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Entry<Var, Query> countQuery = QueryGenerationUtils.createQueryCountPartition(query, partitionVars, xitemLimit, xrowLimit);
        Var v = countQuery.getKey();
        Query q = countQuery.getValue();

        return SparqlRx.fetchNumber(qef, q, v)
                .map(count -> SparqlRx.toRange(count.longValue(), xitemLimit, xrowLimit));
    }

    public static Single<Range<Long>> fetchCountQuery(SparqlQueryConnection qef, Query query, Long itemLimit, Long rowLimit) {
        Single<Range<Long>> result = fetchCountQueryPartition(qef, query, null, itemLimit, rowLimit);
        return result;
    }


    public static Range<Long> toRange(Long count, Long itemLimit, Long rowLimit) {
        boolean mayHaveMoreItems = rowLimit != null
                ? true
                : itemLimit != null && count > itemLimit;

        Range<Long> r = mayHaveMoreItems ? Range.atLeast(count) : Range.singleton(count);
        return r;
    }

    public static Flowable<RDFNode> execConstructGrouped(SparqlQueryConnection conn, Entry<? extends Node, Query> e) {
        return execConstructGrouped(conn, e, true);
    }

    public static Flowable<RDFNode> execConstructGrouped(SparqlQueryConnection conn, Entry<? extends Node, Query> e, boolean sortRowsByPartitionVar) {
        Node s = e.getKey();
        Query q = e.getValue();

        return execConstructGrouped(conn, q, s, sortRowsByPartitionVar);
    }

    public static Flowable<RDFNode> execPartitioned(SparqlQueryConnection conn, Entry<? extends Node, Query> e) {
        return execPartitioned(conn, e, true);
    }

    public static Flowable<RDFNode> execPartitioned(SparqlQueryConnection conn, Entry<? extends Node, Query> e, boolean sortRowsByPartitionVar) {
        Node s = e.getKey();
        Query q = e.getValue();

        return execPartitioned(conn, s, q, sortRowsByPartitionVar);
    }

    //public static Acc
    public static Flowable<RDFNode> execConstructGrouped(SparqlQueryConnection conn, Query query, Node s) {
        return execConstructGrouped(conn, query, s, true);
    }

    public static Flowable<RDFNode> execConstructGrouped(SparqlQueryConnection conn, Query query, Node s, boolean sortRowsByPartitionVar) {
        return execConstructGrouped(conn, query, Collections.singletonList((Var)s), s, sortRowsByPartitionVar);
    }

    public static Flowable<RDFNode> execConstructGrouped(SparqlQueryConnection conn, Query query, List<Var> primaryKeyVars, Node rootNode, boolean sortRowsByPartitionVar) {
        return execConstructGrouped(q -> conn.query(q), query, primaryKeyVars, rootNode, sortRowsByPartitionVar);
    }

    public static Flowable<RDFNode> execConstructGrouped(Function<Query, QueryExecution> qeSupp, Query query, List<Var> primaryKeyVars, Node rootNode, boolean sortRowsByPartitionVar) {
        if(rootNode.isVariable() && !primaryKeyVars.contains(rootNode)) {
            throw new RuntimeException("If the root node is a variable it must be among the primary key ones");
        }

        Template template = query.getConstructTemplate();
        Query clone = preprocessQueryForPartition(query, primaryKeyVars, sortRowsByPartitionVar);

        Function<Binding, Binding> grouper = createGrouper(primaryKeyVars, false);

        Flowable<RDFNode> result = SparqlRx
            // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
            .execSelectRaw(() -> qeSupp.apply(clone))
            //.groupBy(createGrouper(primaryKeyVars, false)::apply)
            .lift(new OperatorOrderedGroupBy<Binding, Binding, AccGraph>(
                    grouper::apply,
                    groupKey -> new AccGraph(template),
                    AccGraph::accumulate))
            .map(keyAndAcc -> {
                Binding groupKey = keyAndAcc.getKey();
                AccGraph accGraph = keyAndAcc.getValue();
                Map<Node, Node> bnodeMap = accGraph.getBnodeMap();

                // TODO The accumulator should manage a blank node label map which we re-use to map the rootNode

                Node effectiveRoot = rootNode.isVariable()
                        ? groupKey.get((Var)rootNode)
                        : rootNode.isBlank()
                            ? bnodeMap.get(rootNode)
                            : rootNode;

                Graph g = accGraph.getValue();
                Model m = ModelFactory.createModelForGraph(g);
                RDFNode r = m.asRDFNode(effectiveRoot);
                return r;
            });
            // Filter out null group keys; they can e.g. occur due to https://issues.apache.org/jira/browse/JENA-1487
            // .filter(group -> group.getKey() != null)
//            .map(group -> {
//                // Binding groupKey = group.getKey();
//                AccGraph acc = new AccGraph(template);
//                group.forEach(acc::accumulate);
//                Graph g = acc.getValue();
//                Model m = ModelFactory.createModelForGraph(g);
//                RDFNode r = m.asRDFNode(rootNode);
//                return r;
//            });
        return result;
    }


    public static Query preprocessQueryForPartition(Query q, List<Var> primaryKeyVars, boolean sortRowsByPartitionVar) {

        Template template = q.getConstructTemplate();
        Set<Var> projectVars = new LinkedHashSet<>();
        projectVars.addAll(primaryKeyVars);

        projectVars.addAll(QuadPatternUtils.getVarsMentioned(template.getQuads()));

        Query clone = q.cloneQuery();
        clone.setQuerySelectType();
        clone.getProject().clear();


        if(projectVars.isEmpty()) {
            // If the template is variable free then project the first variable of the query pattern
            // If the query pattern is variable free then just use the result star
            Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(q.getQueryPattern()));
            if(patternVars.isEmpty()) {
                clone.setQueryResultStar(true);
            } else {
                Var v = patternVars.iterator().next();
                clone.setQueryResultStar(false);
                clone.getProject().add(v);
            }
        } else {
            clone.setQueryResultStar(false);
            clone.addProjectVars(projectVars);
        }

        clone.setDistinct(true);

        if(sortRowsByPartitionVar) {
            // TODO Check that there is no prior sort condition already
            for(Var primaryKeyVar : primaryKeyVars) {
                clone.addOrderBy(new SortCondition(primaryKeyVar, Query.ORDER_ASCENDING));
            }
        }

        logger.debug("Converted query to: " + clone);
        return clone;
    }


    public static Flowable<RDFNode> execPartitioned(SparqlQueryConnection conn, Node s, Query q, boolean sortRowsByPartitionVar) {

        Template template = q.getConstructTemplate();
        Query clone = preprocessQueryForPartition(q, Collections.singletonList((Var)s), sortRowsByPartitionVar);

        Flowable<RDFNode> result = SparqlRx
                // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
                .execSelectRaw(() -> conn.query(clone))
                .map(b -> {
                    Graph graph = GraphFactory.createDefaultGraph();

                    // TODO Re-allocate blank nodes
                    if(template != null) {
                        Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(b));
                        while(it.hasNext()) {
                            Triple t = it.next();
                            graph.add(t);
                        }
                    }

                    Node rootNode = s.isVariable() ? b.get((Var)s) : s;

                    Model m = ModelFactory.createModelForGraph(graph);
                    RDFNode r = m.asRDFNode(rootNode);
                    //Resource r = n.asResource();
//					Resource r = m.createResource()
//					.addProperty(RDF.predicate, m.asRDFNode(valueNode))
//					.addProperty(Vocab.facetValueCount, );
//				//m.wrapAsResource(valueNode);
//				return r;

                    return r;
                });
        return result;
    }

}
