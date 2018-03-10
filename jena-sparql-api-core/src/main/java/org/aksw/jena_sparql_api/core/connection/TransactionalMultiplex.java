package org.aksw.jena_sparql_api.core.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public class TransactionalMultiplex<T extends Transactional>
	implements Transactional
{
	protected Collection<? extends T> delegates;

	public static <T> void forEach(Collection<? extends T> items, Consumer<? super T> handler) {
		List<Throwable> throwables = new ArrayList<>();
		for(T item : items) {
			try {
				handler.accept(item);
			} catch(Exception e) {
				throwables.add(e);
			}
		}

		// TODO Throw a multi exception
		if(!throwables.isEmpty()) {
			throw new RuntimeException(throwables.iterator().next());
		}
	}

	@SafeVarargs
	public TransactionalMultiplex(T ... delegates) {
		this(Arrays.asList(delegates));
	}

	public TransactionalMultiplex(Collection<? extends T> delegates) {
		super();
		this.delegates = delegates;
	}

	@Override
	public void begin(ReadWrite readWrite) {
		TransactionalMultiplex.forEach(delegates, d -> d.begin(readWrite));
	}

	@Override
	public void commit() {
		TransactionalMultiplex.forEach(delegates, Transactional::commit);
	}

	@Override
	public void abort() {
		TransactionalMultiplex.forEach(delegates, Transactional::abort);
	}

	@Override
	public void end() {
		TransactionalMultiplex.forEach(delegates, Transactional::end);
	}

	@Override
	public boolean isInTransaction() {
		boolean result = delegates.isEmpty() ? false : delegates.iterator().next().isInTransaction();
		return result;
	}

	@Override
	public void begin(TxnType type) {
		TransactionalMultiplex.forEach(delegates, Transactional::begin);
	}

	@Override
	public boolean promote(Promote mode) {
		TransactionalMultiplex.forEach(delegates, Transactional::promote);
		return false;
	}

	@Override
	public ReadWrite transactionMode() {
		TransactionalMultiplex.forEach(delegates, Transactional::transactionMode);
		return null;
	}

	@Override
	public TxnType transactionType() {
		TxnType result = delegates.isEmpty() ? null : delegates.iterator().next().transactionType();
		return result;
	}
	
	
	
}
