package org.aksw.jena_sparql_api.mapper.context;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.util.Assert;

import com.google.common.collect.Sets;

public class FrontierImpl<T>
	implements Frontier<T>
{
	protected Collection<T> open;
	protected Collection<T> done;

	public FrontierImpl(Collection<T> open, Collection<T> done) {
		super();
		this.open = open;
		this.done = done;
	}

	@Override
	public void add(T item) {
		Assert.notNull(item);

		boolean isAlreadyDone = done.contains(item);
		if(!isAlreadyDone) {
			open.add(item);
		}
	}

	@Override
	public T next() {
		T result;

		Iterator<T> it = open.iterator();
		if(it.hasNext()) {
			result = it.next();
			done.add(result);
			it.remove();

		} else {
			result = null;
		}

		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result = open.isEmpty();
		return result;
	}

	public static <T> FrontierImpl<T> createIdentityFrontier() {
		FrontierImpl<T> result = new FrontierImpl<T>(Sets.<T>newIdentityHashSet(), Sets.<T>newIdentityHashSet());
		return result;
	}

	@Override
	public boolean isDone(T item) {
		boolean result = done.contains(item);
		return result;
	}

	@Override
	public void makeDone(T item) {
		open.remove(item);
		done.add(item);
	}
}
