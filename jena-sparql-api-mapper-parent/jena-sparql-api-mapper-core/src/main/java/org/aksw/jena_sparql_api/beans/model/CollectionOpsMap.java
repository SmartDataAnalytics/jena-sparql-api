package org.aksw.jena_sparql_api.beans.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CollectionOpsMap
	implements CollectionOps
{
	@Override
	public Iterator<?> getItems(Object entity) {
		Map map = (Map)entity;
		Iterator<?> result = map.entrySet().iterator();
		return result;
	}

	@Override
	public void setItems(Object entity, Iterator<?> items) {
		Map map = (Map)entity;
		map.clear();
		items.forEachRemaining(i -> { 
			Entry e = (Entry)i;
			map.put(e.getKey(), e.getValue());
		});		
	}
}
