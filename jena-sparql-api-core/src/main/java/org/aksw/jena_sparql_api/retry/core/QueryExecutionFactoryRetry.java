package org.aksw.jena_sparql_api.retry.core;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDecorator;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.nurkiewicz.asyncretry.backoff.Backoff;
import com.nurkiewicz.asyncretry.backoff.BoundedMaxBackoff;
import com.nurkiewicz.asyncretry.backoff.BoundedMinBackoff;
import com.nurkiewicz.asyncretry.backoff.ExponentialDelayBackoff;
import com.nurkiewicz.asyncretry.backoff.FixedIntervalBackoff;
import com.nurkiewicz.asyncretry.backoff.ProportionalRandomBackoff;
import com.nurkiewicz.asyncretry.backoff.UniformRandomBackoff;
import com.nurkiewicz.asyncretry.policy.RetryPolicy;

public class QueryExecutionFactoryRetry
    extends QueryExecutionFactoryDecorator
{
    private int retryCount;
    private long retryDelayInMs;

    private final boolean fixedDelay;
    private final RetryPolicy retryPolicy;
    private final Backoff backoff;
    protected ScheduledExecutorService scheduler;

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, int retryCount, long retryDelayInMs) {
        this(decoratee, new RetryPolicy().withMaxRetries(retryCount), new FixedIntervalBackoff(retryDelayInMs), true);

        //TODO remove the variables which are superseded by Async-Retry API
        this.retryCount = retryCount;
        this.retryDelayInMs = retryDelayInMs;
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, int retryCount, long retryDelayDuration, TimeUnit retryDelayTimeUnit) {
        this(decoratee, new RetryPolicy().withMaxRetries(retryCount), new FixedIntervalBackoff(retryDelayTimeUnit.toMillis(retryDelayDuration)), true);

        //TODO remove the variables which are superseded by Async-Retry API
        this.retryCount = retryCount;
        this.retryDelayInMs = retryDelayTimeUnit.toMillis(retryDelayDuration);
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee) {
        this(decoratee, RetryPolicy.DEFAULT, Backoff.DEFAULT);
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, Backoff backoff) {
        this(decoratee, RetryPolicy.DEFAULT, backoff);
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, RetryPolicy retryPolicy) {
        this(decoratee, retryPolicy, Backoff.DEFAULT);
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, RetryPolicy retryPolicy, Backoff backoff) {
        this(decoratee, retryPolicy, backoff, false);
    }

    public QueryExecutionFactoryRetry(QueryExecutionFactory decoratee, RetryPolicy retryPolicy, Backoff backoff, boolean fixedDelay) {
        super(decoratee);
        this.retryPolicy = Preconditions.checkNotNull(retryPolicy);
        this.backoff = Preconditions.checkNotNull(backoff);
        this.fixedDelay = fixedDelay;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryExecution result = new QueryExecutionRetry(() -> super.createQueryExecution(query), retryPolicy, backoff, fixedDelay, scheduler);
        return result;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {
        QueryExecution result = new QueryExecutionRetry(() -> super.createQueryExecution(queryString), retryPolicy, backoff, fixedDelay, scheduler);
        return result;
    }

    public QueryExecutionFactoryRetry withRetryPolicy(RetryPolicy retryPolicy) {
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, fixedDelay);
    }

    public QueryExecutionFactoryRetry withBackoff(Backoff backoff) {
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, fixedDelay);
    }

    public QueryExecutionFactoryRetry withExponentialBackoff(long initialDelayMillis, double multiplier) {
        final ExponentialDelayBackoff backoff = new ExponentialDelayBackoff(initialDelayMillis, multiplier);
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, fixedDelay);
    }

    public QueryExecutionFactoryRetry withFixedBackoff(long delayMillis) {
        final FixedIntervalBackoff backoff = new FixedIntervalBackoff(delayMillis);
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, fixedDelay);
    }

    public QueryExecutionFactoryRetry withFixedRate() {
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, true);
    }

    public QueryExecutionFactoryRetry withFixedRate(boolean fixedDelay) {
        return new QueryExecutionFactoryRetry(decoratee, retryPolicy, backoff, fixedDelay);
    }

    //@SafeVarargs
    public QueryExecutionFactoryRetry retryOn(Class<? extends Throwable> ... retryOnThrowable) {
        return this.withRetryPolicy(new RetryPolicy().retryOn(retryOnThrowable));
    }

    //@SafeVarargs
    public QueryExecutionFactoryRetry abortOn(Class<? extends Throwable> ... abortOnThrowable) {
        return this.withRetryPolicy(new RetryPolicy().abortOn(abortOnThrowable));
    }

    public QueryExecutionFactoryRetry abortIf(Predicate<Throwable> abortPredicate) {
        return this.withRetryPolicy(new RetryPolicy().abortIf(abortPredicate));
    }

    public QueryExecutionFactoryRetry withUniformJitter() {
        return this.withBackoff(new UniformRandomBackoff(backoff));
    }

    public QueryExecutionFactoryRetry withUniformJitter(long range) {
        return this.withBackoff(new UniformRandomBackoff(backoff, range));
    }

    public QueryExecutionFactoryRetry withProportionalJitter() {
        return this.withBackoff(new ProportionalRandomBackoff(backoff));
    }

    public QueryExecutionFactoryRetry withProportionalJitter(double multiplier) {
        return this.withBackoff(new ProportionalRandomBackoff(backoff, multiplier));
    }

    public QueryExecutionFactoryRetry withMinDelay(long minDelayMillis) {
        return this.withBackoff(new BoundedMinBackoff(backoff, minDelayMillis));
    }

    public QueryExecutionFactoryRetry withMaxDelay(long maxDelayMillis) {
        return this.withBackoff(new BoundedMaxBackoff(backoff, maxDelayMillis));
    }

    public QueryExecutionFactoryRetry withMaxRetries(int times) {
        return this.withRetryPolicy(new RetryPolicy().withMaxRetries(times));
    }

    public QueryExecutionFactoryRetry dontRetry() {
        return this.withRetryPolicy(new RetryPolicy().withMaxRetries(0));
    }

    public QueryExecutionFactoryRetry withNoDelay() {
        return this.withBackoff(new FixedIntervalBackoff(0));
    }

    public static void main(String[] args) throws Exception {
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
        qef = new QueryExecutionFactoryRetry(qef).retryOn(Exception.class).withMaxRetries(3).withMinDelay(500).withMaxDelay(1000);
        String query = "SELECT ?type (COUNT(?s) AS ?cnt) WHERE {?s a <http://dbpedia.org/ontology/Person> . ?s a ?type .} GROUP BY ?type ORDER BY DESC(?cnt)";
        QueryExecution qe = qef.createQueryExecution(query);
        qe.setTimeout(10000);
        ResultSet rs = qe.execSelect();
        System.out.println(ResultSetFormatter.asText(rs));
    }

}