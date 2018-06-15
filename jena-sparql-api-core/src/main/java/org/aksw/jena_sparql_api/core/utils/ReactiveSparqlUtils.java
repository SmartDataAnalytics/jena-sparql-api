package org.aksw.jena_sparql_api.core.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

public class ReactiveSparqlUtils {
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

	public static void processExecSelect(FlowableEmitter<Binding> emitter, QueryExecution qe) {
		try {
			emitter.setCancellable(qe::abort);
			ResultSet rs = qe.execSelect();
			while(!emitter.isCancelled() && rs.hasNext()) {
				Binding binding = rs.nextBinding();
				emitter.onNext(binding);
			}
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(e);
		}
	}
	
	public static void processExecConstructTriples(FlowableEmitter<Triple> emitter, QueryExecution qe) {
		try {
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

	public static Flowable<Binding> execSelect(Supplier<QueryExecution> qes) {
		Flowable<Binding> result = Flowable.create(emitter -> {
			QueryExecution qe = qes.get();
			processExecSelect(emitter, qe);
			//new Thread(() -> process(emitter, qe)).start();
		}, BackpressureStrategy.BUFFER);

		return result;
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

	public static void main(String[] args) {
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
			execSelect(() -> QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql",
					"SELECT * { ?s a <http://dbpedia.org/ontology/Person> }"))
							.takeUntil(b -> i[0] == 10).subscribe(x -> {
								i[0]++;
								System.out.println("x: " + x);
	
							});
		}
		// NOTE This way, the main thread will terminate before the queries are processed
	}
	
    public static Single<Range<Long>> fetchCountConcept(org.aksw.jena_sparql_api.core.QueryExecutionFactory qef, Concept concept, Long itemLimit, Long rowLimit) {

        Var outputVar = ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Query countQuery = ConceptUtils.createQueryCount(concept, outputVar, xitemLimit, xrowLimit);

        //var qe = sparqlService.createQueryExecution(countQuery);

        //Integer count = ServiceUtils.fetchInteger(sparqlService, countQuery, outputVar);
    	return ReactiveSparqlUtils.execSelect(() -> qef.createQueryExecution(countQuery))
            	.map(b -> b.get(outputVar))
            	.map(countNode -> ((Number)countNode.getLiteralValue()).longValue())
            	.map(count -> {
            		boolean mayHaveMoreItems = rowLimit != null
            				? true
            				: itemLimit != null && count > itemLimit;

                    Range<Long> r = mayHaveMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count);        		
                    return r;
            	})
            	.single(null);
    }

}
