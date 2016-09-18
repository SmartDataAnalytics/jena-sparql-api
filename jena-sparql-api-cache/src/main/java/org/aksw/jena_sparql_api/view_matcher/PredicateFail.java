package org.aksw.jena_sparql_api.view_matcher;

import java.util.function.Predicate;

/**
 * A stateful predicate wrapper that allows testing
 * whether its delegate failed at least once (i.e. returned true)
 *
 * Can be used with streams and .takeWhile(predicateFail) in order to determine
 * whether any items in the stream caused the predicate to fail.
 *
 * @author raven
 *
 */
public class PredicateFail<T>
	implements Predicate<T>
{
	protected Predicate<T> delegate;
	protected boolean isFailed;

	public PredicateFail(Predicate<T> delegate) {
		super();
		this.delegate = delegate;
		this.isFailed = false;
	}

	@Override
	public boolean test(T t) {
		boolean result = delegate.test(t);

		if(result == false) {
			isFailed = true;
		}

		return result;
	}

	public void setFailed(boolean isFailed) {
		this.isFailed = isFailed;
	}

	public boolean isFailed() {
		return isFailed;
	}

	@Override
	public String toString() {
		return "PredicateFail[failed=" + isFailed + "]";
	}
}
