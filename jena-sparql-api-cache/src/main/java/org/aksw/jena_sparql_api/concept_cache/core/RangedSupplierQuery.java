package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.function.Function;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public class RangedSupplierQuery
    implements RangedSupplier<Long, Binding>, OpAttribute
{
	/** Function that creates a QueryExecution from a Query */
    protected Function<Query, ? extends QueryExecution> qef;
    protected Query query;

    public RangedSupplierQuery(Function<Query, ? extends QueryExecution> qef, Query query) {
        super();
        this.qef = qef;
        this.query = query;
    }

    @Override
    public Flowable<Binding> apply(Range<Long> range) {
        Query clone = query.cloneQuery();
        QueryUtils.applyRange(clone, range);

        Flowable<Binding> result = SparqlRx.execSelectRaw(() -> qef.apply(clone));

        return result;
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
