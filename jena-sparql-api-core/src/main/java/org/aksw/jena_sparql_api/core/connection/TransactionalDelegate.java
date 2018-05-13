package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public class TransactionalDelegate
	implements Transactional
{
	protected Transactional delegate;
	
	public TransactionalDelegate(Transactional delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public void begin(ReadWrite readWrite) {
		delegate.begin(readWrite);
	}

	@Override
	public void commit() {
		delegate.commit();
	}

	@Override
	public void abort() {
		delegate.abort();
	}

	@Override
	public void end() {
		delegate.end();
	}

	@Override
	public boolean isInTransaction() {
		return delegate.isInTransaction();
	}

	@Override
	public void begin(TxnType type) {
		delegate.begin(type);
	}

	@Override
	public boolean promote(Promote mode) {
		return delegate.promote(mode);
	}

	@Override
	public ReadWrite transactionMode() {
		return delegate.transactionMode();
	}

	@Override
	public TxnType transactionType() {
		return delegate.transactionType();
	}
}
