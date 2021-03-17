package org.aksw.jena_sparql_api.utils;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;

public class CollectionFromTable<T>
	extends AbstractCollection<T>
{
	protected Table table;
	protected ExecutionContext execCxt;
	protected Function<Binding, T> mapper;

	public CollectionFromTable(Table table, ExecutionContext execCxt, Function<Binding, T> mapper) {
		super();
		this.table = table;
		this.execCxt = execCxt;
		this.mapper = mapper;
	}

	@Override
	public int size() {
		int result = table.size();
		return result;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> result = Streams.stream(table.iterator(execCxt))
				.map(mapper)
				.iterator();
		
		return result;
	}
}
