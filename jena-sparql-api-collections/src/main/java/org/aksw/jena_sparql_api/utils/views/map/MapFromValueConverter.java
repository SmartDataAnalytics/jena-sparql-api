package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.sets.SetFromCollection;

import com.google.common.base.Converter;

public class MapFromValueConverter<K, U, V>
	extends AbstractMap<K, V>
{
	protected Map<K, U> map;
	protected Converter<U, V> converter;

	public MapFromValueConverter(Map<K, U> map, Converter<U, V> converter) {
		super();
		this.map = map;
		this.converter = converter;
	}
	
	@Override
	public V get(Object key) {
		V result = Optional.ofNullable(map.get(key)).map(converter::convert).orElse(null);
		return result;
	}

	@Override
	public boolean containsKey(Object key) {
		boolean result = map.containsKey(key);
		return result;
	}
	
	@Override
	public V put(K key, V value) {
		U val = converter.reverse().convert(value);
		map.put(key, val);
		return value;
	}

	@Override
	public boolean containsValue(Object value) {
		U val = converter.reverse().convert((V)value);
		boolean result = map.containsValue(val);
		return result;
	}
	
	@Override
	public Collection<V> values() {
		return new ConvertingCollection<>(map.values(), converter);
	}

	// public static <K, L> Entry<L, V> transform(Entry<K, V>, Function<? super K, ?
	// extends V> )

	@Override
	public Set<Entry<K, V>> entrySet() {
		return new SetFromCollection<>(
				new ConvertingCollection<>(map.entrySet(), EntryConverterByKey.converterByValue(converter)));
	}

}
