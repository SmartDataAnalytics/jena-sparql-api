package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.rx.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx.EntityQueryProcessed;
import org.aksw.jena_sparql_api.rx.entity.model.AttributeGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListServiceEntityQuery
    implements ListService<EntityBaseQuery, RDFNode>
{
    protected SparqlQueryConnection conn;
    protected AttributeGraphFragment attributePart;

    public ListServiceEntityQuery(SparqlQueryConnection conn, AttributeGraphFragment attributePart) {
        super();
        this.conn = conn;
        this.attributePart = attributePart;
    }


    @Override
    public ListPaginator<RDFNode> createPaginator(EntityBaseQuery baseQuery) {
        return new ListPaginatorEntityQuery(baseQuery);
    }


    public class ListPaginatorEntityQuery
        implements ListPaginator<RDFNode>
    {
        protected EntityBaseQuery baseQuery;

        public ListPaginatorEntityQuery(EntityBaseQuery baseQuery) {
            super();
            this.baseQuery = baseQuery;
        }

        @Override
        public Flowable<RDFNode> apply(Range<Long> t) {
            // TODO Ensure we create a deep clone
            EntityQueryImpl entityQuery = new EntityQueryImpl();
            entityQuery.setBaseQuery(baseQuery);
            entityQuery.setAttributePart(attributePart);



            // QueryUtils.applySlice(query, offset, limit, cloneOnChange)
            long limit = QueryUtils.rangeToLimit(t);
            long offset = QueryUtils.rangeToOffset(t);

            Flowable<RDFNode> result = EntityQueryRx.execConstructEntities(conn, entityQuery);
            return result;
        }

        @Override
        public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
            EntityQueryImpl entityQuery = new EntityQueryImpl();
            entityQuery.getAttributePart().getMandatoryJoins().addAll(attributePart.getMandatoryJoins());
            entityQuery.setBaseQuery(baseQuery);

            EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(entityQuery);
            EntityQueryProcessed processed = EntityQueryRx.processEntityQuery(basic, true);
            Query query = processed.getInnerSelect();

            // Entry<Var, Query> countData = QueryGenerationUtils.createQueryCount(query);

            Single<Range<Long>> result = SparqlRx.fetchCountQueryPartition(conn, query, processed.getPartitionVars(), itemLimit, rowLimit);
            return result;
        }

    }
}

