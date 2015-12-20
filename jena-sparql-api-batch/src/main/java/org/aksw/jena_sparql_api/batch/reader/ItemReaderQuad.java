package org.aksw.jena_sparql_api.batch.reader;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.aksw.jena_sparql_api.batch.step.F_TripleToQuad;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.PagingQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.data.AbstractPaginatedDataItemReader;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Item reader that reads a SPARQL SELECT query using pagination
 *
 * @author raven
 *
 * @param <T>
 */
public class ItemReaderQuad
    extends AbstractPaginatedDataItemReader<Quad>
    implements InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(ItemReaderQuad.class);

    private Query query;
    private QueryExecutionFactory qef;
    // TODO Validation is not part of the reader but of the processor!
    //private Predicate<Quad> predicate;

    public static int nextId = 0;

    private int id;

    public ItemReaderQuad() {
        super();
        setName(this.getClass().getName());

        this.id = ++nextId;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public QueryExecutionFactory getQef() {
        return qef;
    }

    public void setQef(QueryExecutionFactory qef) {
        this.qef = qef;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Assert that properties are valid
        System.out.println("TODO validate settings");
    }


    public ItemReaderQuad(QueryExecutionFactory qef, Query query) { //, Predicate<Quad> predicate) {
        setName(this.getClass().getName());
        this.qef = qef;
        this.query = query;
        //this.predicate = predicate;
    }

    @Override
    protected Iterator<Quad> doPageRead() {
        //long limit = (long)this.pageSize;
        long offset = this.page * this.pageSize;

        logger.info("[START] ItemReader " + id + " on page " + this.page);

        PagingQuery pagingQuery = new PagingQuery(this.pageSize, this.query);
        Iterator<Query> itQuery = pagingQuery.createQueryIterator(offset);
        Query query = itQuery.next();

        Iterator<Quad> result;

        if(query == null) {
            Collection<Quad> tmp = Collections.emptyList();
            return tmp.iterator();
        } else {
            QueryExecution qe = qef.createQueryExecution(query);
            Iterator<Triple> triplesIt = qe.execConstructTriples();

            result = Iterators.transform(triplesIt, F_TripleToQuad.fn);
//            if(predicate != null) {
//                result = Iterators.filter(result, predicate);
//            }
        }
        logger.info("[DONE] ItemReader " + id + " on page " + this.page);

        return result;
    }

}
