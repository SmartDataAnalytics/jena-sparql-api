package org.aksw.jena_sparql_api.rdf.collections.enhanced;

import java.util.Collection;
import java.util.function.Predicate;

import com.google.common.base.Converter;

public interface CollectionEx<T>
    extends Collection<T>
{
	Collection<T> filter(Predicate<? super T> predicate);
	<U> Collection<T> map(Converter<T, U> converter);

	
//	default Collection<T> filter(Converter<T, U> converter) {
//		Predicate<Object> predicate = ConverterUtils.createPredicate(converter);
//		Collection<T> 
//	}
}
