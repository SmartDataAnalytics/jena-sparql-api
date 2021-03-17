package org.aksw.jena_sparql_api.analytics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;


public class ResultSetAnalyticsSerializationTests {

	/**
	 * Serialize and deserialize an aggregator and check whether both aggregators then
	 * yield the same value on accumulation.
	 * Comparing aggregators is typically not meaningful because of the use of lambdas which
	 * are usually not comparable.
	 */
	@Test
	public void testSerialization1() throws IOException, ClassNotFoundException {
		ParallelAggregator<Binding, Map<Var, Entry<Set<String>, Long>>, ?> expectedAgg = ResultSetAnalytics.aggPerVar(
				Sets.newHashSet(Vars.s, Vars.p, Vars.o), NodeAnalytics.usedDatatypesAndNullCounts());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(expectedAgg);
		
		ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
		@SuppressWarnings("unchecked")
		ParallelAggregator<Binding, Map<Var, Entry<Multiset<String>, Long>>, ?> actualAgg =
				(ParallelAggregator<Binding, Map<Var, Entry<Multiset<String>, Long>>, ?>)oin.readObject();

		BindingMap b = BindingFactory.create();
		b.add(Vars.s, NodeFactory.createURI("http://www.example.org/Foo"));
		b.add(Vars.p, NodeFactory.createLiteral("bar"));
		
		Accumulator<Binding, Map<Var, Entry<Multiset<String>, Long>>> actualAcc = actualAgg.createAccumulator();
		actualAcc.accumulate(b);
		Map<Var, Entry<Multiset<String>, Long>> actualValue = actualAcc.getValue();

		Accumulator<Binding, Map<Var, Entry<Multiset<String>, Long>>> expectedAcc = actualAgg.createAccumulator();
		expectedAcc.accumulate(b);
		Map<Var, Entry<Multiset<String>, Long>> expectedValue = actualAcc.getValue();

		Assert.assertEquals(expectedValue, actualValue);
	}
}
