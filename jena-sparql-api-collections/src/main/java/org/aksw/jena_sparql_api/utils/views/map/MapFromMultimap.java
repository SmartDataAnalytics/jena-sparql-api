package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.sets.SetFromCollection;

import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
/* This class is used to view a multimap as a map
/* This assumes, that no key of the multimap maps to multiple values
 * 
 * @author Claus Stadler, Feb 9, 2019
 *
 * @param <K>
 * @param <V>
 */
public class MapFromMultimap<K, V>
	extends AbstractMap<K, V>
{
	protected Multimap<K, V> multimap;
	
	public MapFromMultimap(Multimap<K, V> multimap) {
		super();
		this.multimap = multimap;
	}

	@Override
	public V get(Object k) {
		V result = null;
		if(multimap.containsKey(k)) {
			@SuppressWarnings("unchecked")
			Collection<V> col = multimap.get((K)k);
			if(col.size() > 1) {
				throw new IllegalStateException("Map wrapper for a multimap encountered multiple values");
			}
			
			result = col.isEmpty() ? null : col.iterator().next();
		}
		
		return result;
	}

	@Override
	public V put(K key, V value) {
		multimap.put(key, value);
		return value;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return multimap.containsKey(key);
	}
	
	@Override
	public V remove(Object key) {
		V result = get(key);
		multimap.removeAll(key);
		return result;
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new SetFromCollection<>(multimap.entries());
	}
	

	public static <K, V> Map<K, V> createView(Map<K, Collection<V>> map) {
		Multimap<K, V> multimap = createView(map, () -> null);
		Map<K, V> result = new MapFromMultimap<>(multimap);
		return result;
	}
	
	public static <K, V> Multimap<K, V> createView(Map<K, Collection<V>> map, Supplier<? extends Collection<V>> factory) {
		
		/* This is an ugly hack that fakes an empty map - regardless of the actual content - during constructing a guava multimap */
		boolean fakeEmpty[] = {true};
		Map<K, Collection<V>> wrapper = new ForwardingMap<K, Collection<V>>() {
			@Override
			protected Map<K, Collection<V>> delegate() {
				return map;
			}
			
			@Override
			public boolean isEmpty() {
				return fakeEmpty[0] ? true : super.isEmpty();
			}
		};
		
		Multimap<K, V> result = Multimaps.newMultimap(wrapper, factory);
		fakeEmpty[0] = false;
		
		return result;
	}
}
