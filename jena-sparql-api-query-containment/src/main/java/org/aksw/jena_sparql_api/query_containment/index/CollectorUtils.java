package org.aksw.jena_sparql_api.query_containment.index;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorUtils {

	
	public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper,
			Supplier<M> mapSupplier) {
		Collector<T, ?, M> result = Collectors.toMap(
				keyMapper,
				valueMapper,
				(u, v) -> { throw new RuntimeException("should not happen"); },
				mapSupplier);
		return result;
	}

//	public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedHashMap() {
//
//		Collector<T, ?, Map<K, U>> result = Collectors.toMap(Entry::<K, U>getKey, Entry::<K,U>getValue);
//				
//		return result;
//	}

	public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedHashMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {

		Collector<T, ?, Map<K, U>> result = toMap(keyMapper, valueMapper, LinkedHashMap::new);
				
		return result;
	}
	
}
