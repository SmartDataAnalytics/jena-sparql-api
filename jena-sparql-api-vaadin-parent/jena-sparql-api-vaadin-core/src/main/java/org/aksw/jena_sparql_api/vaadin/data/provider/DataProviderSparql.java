package org.aksw.jena_sparql_api.vaadin.data.provider;

import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.rx.util.RxUtils;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;

import com.github.jsonldjava.shaded.com.google.common.primitives.Ints;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class DataProviderSparql
        extends AbstractBackEndDataProvider<Binding, Expr> {
    private static final long serialVersionUID = 1L;

    protected Relation relation;
    protected Function<? super org.apache.jena.query.Query, ? extends QueryExecution> qef;

    public int predefinedSize = -1;

    public DataProviderSparql(Relation relation,
                              Function<? super org.apache.jena.query.Query, ? extends QueryExecution> qef) {
        super();
        this.relation = relation;
        this.qef = qef;
    }

    @Override
    protected Stream<Binding> fetchFromBackEnd(Query<Binding, Expr> query) {
        org.apache.jena.query.Query baseQuery = createEffectiveQuery(relation, query);
        org.apache.jena.query.Query q = QueryUtils.applySlice(
                baseQuery,
                (long) query.getOffset(),
                (long) query.getLimit(),
                false);


        // Any sort conditions on the vaadin query override those of the SPARQL query
        if (!query.getSortOrders().isEmpty()) {
            if (q.hasOrderBy()) {
                q.getOrderBy().clear();
            }

            for (QuerySortOrder qso : Lists.reverse(query.getSortOrders())) {
                q.addOrderBy(convertSortCondition(qso));
            }
        }

        System.out.println(q);

        Stream<Binding> result = RxUtils.stream(SparqlRx.execSelectRaw(() -> qef.apply(q)));
//        Stream<Binding> debug = toStream(SparqlRx.execSelectRaw(() -> qef.apply(q)));
//        long s = System.currentTimeMillis();
//        debug.forEach(b -> {int i = 1;});
//        System.out.println(System.currentTimeMillis() - s + "ms: ");
//        result = result.peek(b -> System.out.println(b));
        return result;
    }

    @Override
    protected int sizeInBackEnd(Query<Binding, Expr> query) {
        if (predefinedSize != -1) {
            return predefinedSize;
        }

        org.apache.jena.query.Query baseQuery = createEffectiveQuery(relation, query);

        System.out.println("Computing resultset size for\n" + baseQuery);

        Range<Long> range = SparqlRx.fetchCountQuery(qef, baseQuery, null, null).blockingGet();
        CountInfo countInfo = RangeUtils.toCountInfo(range);
        long count = countInfo.getCount();

        int result = Ints.saturatedCast(count);
        System.out.println("Counted items: " + result);
        return result;
    }


    public static org.apache.jena.query.Query createEffectiveQuery(Relation relation, Query<Binding, Expr> query) {
        Expr expr = query.getFilter().orElse(null);

        org.apache.jena.query.Query result = relation.toQuery();
        result = result.cloneQuery();

        if (expr != null) {
            QueryUtils.injectFilter(result, expr);
        }

        return result;
    }

    public static int toJena(SortDirection sd) {
        int result = SortDirection.ASCENDING.equals(sd)
                ? org.apache.jena.query.Query.ORDER_ASCENDING
                : SortDirection.DESCENDING.equals(sd)
                ? org.apache.jena.query.Query.ORDER_DESCENDING
                : org.apache.jena.query.Query.ORDER_DEFAULT;

        return result;
    }

    public static SortCondition convertSortCondition(QuerySortOrder qso) {
        Var var = Var.alloc(qso.getSorted());
        int dir = toJena(qso.getDirection());

        return new SortCondition(var, dir);
    }

    public void setPredefinedSize(int predefinedSize) {
        this.predefinedSize = predefinedSize;
    }
}
