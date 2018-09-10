package org.aksw.jena_sparql_api.beans.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Iterators;


public class CollectionOpsCollection
	implements CollectionOps
{
	@Override
	public Iterator<?> getItems(Object entity) {
		Collection c = (Collection)entity;
		Iterator<?> result = c.iterator();
		return result;
	}

	@Override
	public void setItems(Object entity, Iterator<?> items) {
		Collection c = (Collection)entity;
		Iterators.addAll(c, items);
	}
}
