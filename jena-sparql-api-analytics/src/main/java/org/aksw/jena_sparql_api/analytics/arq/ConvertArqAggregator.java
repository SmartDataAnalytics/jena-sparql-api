package org.aksw.jena_sparql_api.analytics.arq;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.expr.aggregate.AggCountVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.nodevalue.NodeValueOps;

public class ConvertArqAggregator {
	
	
    /**
     * Return an aggregator that performs sub aggregation for each group key
     * derived form the VarExprList
     * 
     * @param <O>
     * @param vel
     * @param subAgg
     * @return
     */
	public static <O> ParallelAggregator<Binding, Map<Binding, O>, ?> group(
			VarExprList vel,
			ParallelAggregator<Binding, O, ?> subAgg) {
		return AggBuilder.inputSplit(b -> Collections.singleton(VarExprListUtils.copyProject(vel, b, null)), (b, key) -> key, subAgg);
	}

	
	public static ParallelAggregator<Binding, Node, ?> convert(AggSum arq) {
		// The expression for which to sum up its values
		Expr expr = arq.getExprList().get(0);

		ParallelAggregator<Binding, Node, ?> result =
			AggBuilder.outputTransform(
				AggBuilder.inputTransform(b -> expr.eval(b, null),
					AggBuilder.binaryOperator(() -> NodeValue.makeInteger(0l), (a, b) -> NodeValueOps.additionNV(a, b))),
				NodeValue::toNode);

		return result;
	}

	public static ParallelAggregator<Binding, Node, ?> counting() {
		ParallelAggregator<Binding, Node, ?> result =
			AggBuilder.outputTransform(
				AggBuilder.outputTransform(
					AggBuilder.counting(),
					NodeValue::makeInteger
				),
				NodeValue::toNode);
		return result;
	}

	public static ParallelAggregator<Binding, Node, ?> convert(AggCount arq) {
		return counting();
	}

	public static ParallelAggregator<Binding, Node, ?> convert(AggCountVar arq) {
		return counting();
	}

	public static ParallelAggregator<Binding, Node, ?> convert(AggCountVarDistinct arq) {
		Expr expr = arq.getExprList().get(0);
		
		ParallelAggregator<Binding, Node, ?> result =
			AggBuilder.outputTransform(
				AggBuilder.inputTransform((Binding b) -> expr.eval(b, null),
					AggBuilder.collectionSupplier(() -> new HashSet<NodeValue>())),
			c -> NodeValue.makeInteger(c.size()).asNode());
		
		return result;
	}
	
	// TODO Create some kind of converter registry
	// protected static Map<Class<? extends Aggregator>, Function<? super Aggregator, ? extends >> registry = ...
	
	public static ParallelAggregator<Binding, Node, ?> convert(Aggregator arq) {
		ParallelAggregator<Binding, Node, ?> result;
		if (arq instanceof AggCount) {
			result = convert((AggCount)arq);
		} else if (arq instanceof AggCountVar) {
			result = convert((AggCountVar)arq);			
		} else if (arq instanceof AggCountVarDistinct) {
			result = convert((AggCountVarDistinct)arq);			
		} else if (arq instanceof AggSum) {
			result = convert((AggSum)arq);			
		} else {
			throw new IllegalArgumentException("This type of aggregator is not supported: " + arq);
		}
		
		return result;
	}

}

