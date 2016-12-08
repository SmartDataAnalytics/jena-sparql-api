package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.beans.model.CollectionOps;
import org.springframework.batch.item.ExecutionContext;


public class CollectionOpsExecutionContext
	implements CollectionOps
{

	@Override
	public Iterator<?> getItems(Object entity) {
		ExecutionContext ec = (ExecutionContext)entity;

		return ec.entrySet().iterator();
	}

	@Override
	public void setItems(Object entity, Iterator<?> items) {
		ExecutionContext ec = (ExecutionContext)entity;

		Set<String> keySet = ec.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toSet());
		keySet.forEach(key -> ec.remove(key));
		
		items.forEachRemaining(i -> {
			Entry e = (Entry)i;
			ec.put((String)e.getKey(), e.getValue());
		});
	}
}
