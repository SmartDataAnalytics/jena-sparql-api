package org.aksw.jena_sparql_api.core.connection;

import java.util.function.Consumer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.system.Txn;

/**
 * Temporary interface; DO NOT reference it directly.
 * It will be removed once Jena moves these defaults to their own interface
 *
 * @author raven
 *
 */
public interface SparqlQueryConnectionTmp
    extends TransactionalTmp, SparqlQueryConnection
{

    // ---- SparqlQueryConnection

    default Query parse(String query) {
        return QueryFactory.create(query);
    }

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    public default void queryResultSet(String query, Consumer<ResultSet> resultSetAction) {
        queryResultSet(parse(query), resultSetAction);
    }

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    public default void queryResultSet(Query query, Consumer<ResultSet> resultSetAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");

        Txn.executeRead(this, ()-> {
            try (QueryExecution qExec = query(query) ) {
                ResultSet rs = qExec.execSelect();
                resultSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(String query, Consumer<QuerySolution> rowAction) {
        querySelect(parse(query), rowAction);
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(Query query, Consumer<QuerySolution> rowAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        Txn.executeRead(this, ()->{
            try ( QueryExecution qExec = query(query) ) {
                qExec.execSelect().forEachRemaining(rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Model queryConstruct(String query) {
        return queryConstruct(parse(query));
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Model queryConstruct(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execConstruct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Model queryDescribe(String query) {
        return queryDescribe(parse(query));
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Model queryDescribe(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execDescribe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(String query) {
        return queryAsk(parse(query));
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execAsk();
                }
            } );
    }

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(Query, Consumer)}, {@link #queryConstruct(Query)},
     *  {@link #queryDescribe(Query)}, {@link #queryAsk(Query)}
     *  for ways to execute queries for of a specific form.
     *
     * @param query
     * @return QueryExecution
     */
    @Override
    public QueryExecution query(Query query);

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     *  {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     *  for ways to execute queries for of a specific form.
     *
     * @param queryString
     * @return QueryExecution
     */
    @Override
    public default QueryExecution query(String queryString) {
        return query(parse(queryString));
    }

//
//	@Override
//	public default void querySelect(String query, Consumer<QuerySolution> rowAction) {
//		this.queryResultSet(query, rs -> {
//			while(rs.hasNext()) {
//				QuerySolution qs = rs.next();
//				rowAction.accept(qs);
//			}
//		});
//	}

}
