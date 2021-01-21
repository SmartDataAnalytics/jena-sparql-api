package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder.SerializableBiFunction;

/**
 * Accumulate mappings of least common ancestors
 * 
 * Assume a backing tree structure:
 * For each node passed to accumulate assemble a mapping to its LCA with any other lca found so far.
 * If for a node there is no lca with another entry then the node is mapped to itself.
 * Henc, the resulting map's key set contains the set of nodes passed to accumulate.
 *  
 *  
 * Note: The backing graph must form a tree (not a dag): There must be at most a single lca
 * for any two nodes. 
 *
 * TODO Extend with counting
 * TODO Add the AggLcaMap with the combine function
 * 
 * @author raven
 *
 */
public class AggLcaMap<T>
	implements ParallelAggregator<T, Map<T, T>, Accumulator<T, Map<T, T>>>, Serializable
{
	private static final long serialVersionUID = 6427765607106711627L;

	protected BiFunction<? super T, ? super T, ? extends T> lcaFinder;
	
	public AggLcaMap(BiFunction<? super T, ? super T, ? extends T> lcaFinder) {
		super();
		this.lcaFinder = lcaFinder;
	}


	@Override
	public Accumulator<T, Map<T, T>> createAccumulator() {
		return new AccLcaMap<T>(lcaFinder);
	}


	@Override
	public Accumulator<T, Map<T, T>> combine(Accumulator<T, Map<T, T>> a, Accumulator<T, Map<T, T>> b) {
		if (a.getValue().keySet().size() > b.getValue().keySet().size()) {
			Accumulator<T, Map<T, T>> tmp = a; a = b; b = tmp;
		}
		
		Map<T, T> bm = b.getValue();
		for (T item : bm.keySet()) {
			a.accumulate(item);
		}
		
		return a;
	}

	
	public static class AccLcaMap<T>
		implements Accumulator<T, Map<T, T>>, Serializable
	{
		private static final long serialVersionUID = -3849677045274730067L;

		protected BiFunction<? super T, ? super T, ? extends T> lcaFinder;

		// A map for e.g. int -> decimal - during addition transitivity is resolved
		// so {short -> decimal, int -> decimal, decimal -> decimal } rather than
		//    {short -> int, int -> decimal, decimal -> decimal }
		protected Map<T, T> childToAncestor;
	

		public AccLcaMap(BiFunction<? super T, ? super T, ? extends T> lcaFinder) {
			super();
			this.childToAncestor = new LinkedHashMap<>();
			this.lcaFinder = lcaFinder;
		}
		
		@Override
		public void accumulate(T input) {
	
			// Check whether the given input node is subsumed by any other node
	
			T target = input;
			
//			boolean changed = false;
			for (Entry<T, T> e : childToAncestor.entrySet()) {
				T currentRemap = e.getValue();
							
				// Example:
				// Given: {(short, long), (int, long), (long, long)}
				// On accumulate(decimal): all longs become decimal
				// On accumulate(int): nothing happens, because long
				T lca = lcaFinder.apply(currentRemap, input);
				if (lca != null) {
					if (!lca.equals(currentRemap)) {
						target = lca;
//						changed = true;
						childToAncestor.entrySet().forEach(f -> {
							if (f.getValue().equals(currentRemap)) {
								f.setValue(lca);
							}
						});
					} else {
						target = currentRemap;
						break;
					}
				}
			}
			
			childToAncestor.put(input, target);
		}
	
		@Override
		public Map<T, T> getValue() {
			return childToAncestor;
		}
		
		
		public static <T> AccLcaMap<T> create(SerializableBiFunction<? super T, ? super T, ? extends T> lcaFinder) {
			return new AccLcaMap<>(lcaFinder);
		}
	}

	public static <T> AggLcaMap<T> create(SerializableBiFunction<? super T, ? super T, ? extends T> lcaFinder) {
		return new AggLcaMap<>(lcaFinder);
	}

}
