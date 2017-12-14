package org.aksw.jena_sparql_api.jgrapht;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Multimap;

public class GreedySolver {
	/**
	 * Example:
	 * T is a set of expressions,
	 * taskToTaskResults yields for each expression
	 * 
	 * Result is ...
	 * 
	 * @param baseSolution
	 * @param tasks
	 * @param taskToTaskResults;
	 * @param taskResultToSolutionContribution
	 * @param solutionCombiner
	 * @return
	 */
//	public static <X, T, I, O, S, C> Stream<X> solve(
//			S baseSolution,
//			List<I> inputs,
//			Function<I, T> inputToTask,
//			Function<? super T, Multimap<O, C>> taskToTaskResults,
//			Function<? super C, T> taskResultToSolutionContribution,
//			BiFunction<S, C, Runnable> addContribution,
//			Predicate<? super C> isUnsatisfiable)
//	{
//		for(I input : inputs) {
//			T task = inputToTask.apply(input);
//			Multimap<O, C> taskResults = taskToTaskResults.apply(task);
//			
//			for(Entry<O, Collection<C>> e : taskResults.asMap().entrySet()) {
//				// Add the contribution to the base solution
//				for(C contribution : e.getValue()) {
//					Runnable undo = addContribution.apply(baseSolution, contribution);
//					
//					List<>
//					
//				}
//				
//			}
//		}
//	}
	
	
}
