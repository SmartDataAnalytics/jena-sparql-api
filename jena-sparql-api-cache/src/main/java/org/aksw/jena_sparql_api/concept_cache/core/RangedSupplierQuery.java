package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Iterator;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.util.collection.RangedSupplier;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Range;
import com.google.common.collect.Streams;

import io.reactivex.Flowable;

public class RangedSupplierQuery
    implements RangedSupplier<Long, Binding>, OpAttribute
{
    protected QueryExecutionFactory qef;
    protected Query query;

    public RangedSupplierQuery(QueryExecutionFactory qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    @Override
    public Flowable<Binding> apply(Range<Long> range) {
        Query clone = query.cloneQuery();
        QueryUtils.applyRange(clone, range);

        Flowable<Binding> result = ReactiveSparqlUtils.execSelect(() -> qef.createQueryExecution(clone));
//        QueryExecution qe = qef.createQueryExecution(clone);
//        ResultSet rs = qe.execSelect();
//
//
//        Iterator<Binding> it = new IteratorResultSetBinding(rs);
//        Stream<Binding> result = Streams.stream(it);
//        result.onClose(qe::close);

        return result;
//
//		ClosableIterator<Binding> result = new IteratorClosable<>(it, () -> qe.close());
//		return result;
    }

    @Override
    public Op getOp() {
        Op result = Algebra.compile(query);
        return result;
    }

//	@Override
//    public <X> X unwrap(Class<X> clazz, boolean reflexive) {
//    	@SuppressWarnings("unchecked")
//		X result = reflexive && this.getClass().isAssignableFrom(clazz)
//    		? (X)this
//    		: null;
//
//    	return result;
//    }

}
