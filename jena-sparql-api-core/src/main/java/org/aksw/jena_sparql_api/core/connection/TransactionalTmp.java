package org.aksw.jena_sparql_api.core.connection;

import java.util.Optional;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.Transactional;

public interface TransactionalTmp
    extends Transactional
{
	/** This method needs to be overridden for transaction support */
	default Transactional getDelegate() {
		return null;
	}
	
    @Override
    default boolean isInTransaction() {
        return getDelegate().isInTransaction();
    }

    @Override
    default void begin(ReadWrite readWrite) {
    	Optional.ofNullable(getDelegate()).ifPresent(transactional -> transactional.begin(readWrite));
    }

    @Override
    default void commit() {
    	Optional.ofNullable(getDelegate()).ifPresent(Transactional::commit);
    }

    @Override
    default void abort() {
    	Optional.ofNullable(getDelegate()).ifPresent(Transactional::abort);
    }

    @Override
    default void end() {
    	Optional.ofNullable(getDelegate()).ifPresent(Transactional::end);
    }
    

	@Override
	default void begin(TxnType type) {
    	Optional.ofNullable(getDelegate()).ifPresent(transactional -> transactional.begin(type));
	}

	@Override
	default boolean promote(Promote mode) {
    	return Optional.ofNullable(getDelegate())
    			.map(transactional -> transactional.promote(mode))
    			.orElse(false);
	}

	@Override
	default ReadWrite transactionMode() {
    	return Optional.ofNullable(getDelegate())
    			.map(Transactional::transactionMode)
    			.orElse(null);
	}

	@Override
	default TxnType transactionType() {
    	return Optional.ofNullable(getDelegate())
    			.map(Transactional::transactionType)
    			.orElse(null);
	}
}
