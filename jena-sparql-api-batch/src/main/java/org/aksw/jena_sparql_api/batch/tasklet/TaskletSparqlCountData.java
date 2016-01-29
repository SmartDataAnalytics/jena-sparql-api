package org.aksw.jena_sparql_api.batch.tasklet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import org.apache.jena.query.Query;

public class TaskletSparqlCountData
    extends StepExecutionListenerSupport implements Tasklet, InitializingBean
{
    // We should abstract where to place the value with some kind of 'sink'
    // The sink could then be backed by the spring expression language, such that we could do
    // sink = new SpelSink("jobContext['TaskletSparqlCountData.count']);
    // sink.setValue(count);

    //public static final String DEFAULT_KEY = TaskletSparqlCountData.class.getSimpleName() + ".count";


    protected Query query;
    protected QueryExecutionFactory qef;
    protected String key;


    public TaskletSparqlCountData(Query query, QueryExecutionFactory qef, String key) {
        this.query = query;
        this.qef = qef;
        this.key = key;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        long count = QueryExecutionUtils.countQuery(query, qef);

        String k = key != null ? key : chunkContext.getStepContext().getStepName() + ".count";

        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(k, count);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(query);
        Assert.notNull(qef);
    }
}