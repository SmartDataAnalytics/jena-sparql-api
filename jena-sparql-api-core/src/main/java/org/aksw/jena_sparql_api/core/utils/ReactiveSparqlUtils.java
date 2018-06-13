package org.aksw.jena_sparql_api.core.utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

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

	public static void process(FlowableEmitter<ResultSet> emitter, QueryExecution qe) {
		try {
			emitter.setCancellable(qe::abort);
			ResultSet rs = qe.execSelect();
			emitter.onNext(rs);
			emitter.onComplete();
		} catch (Exception e) {
			emitter.onError(e);
		}
	}

	public static Flowable<ResultSet> queryCore(Supplier<QueryExecution> qes) {
		Flowable<ResultSet> result = Flowable.create(emitter -> {
			QueryExecution qe = qes.get();
			new Thread(() -> process(emitter, qe)).start();
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
//	public static Flowable<Table> groupBy(Flowable<Binding> )
	
	// /**
	// * Mapping that includes
	// *
	// */
	// public static Flowable<Entry<List<Var>, Binding>>(List<Var> vars, ) {
	//
	// }

	public static void main(String[] args) {
		for(int j = 0; j < 10; ++j) {
			int i[] = { 0 };
			System.out.println("HERE");
			queryCore(() -> QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql",
					"SELECT * { ?s a <http://dbpedia.org/ontology/Person> }")).flatMap(ReactiveSparqlUtils::mapToBinding)
							.takeUntil(b -> i[0] == 10).subscribe(x -> {
								i[0]++;
								System.out.println("x: " + x);
	
							});
		}
		// NOTE This way, the main thread will terminate before the queries are processed
	}
}
