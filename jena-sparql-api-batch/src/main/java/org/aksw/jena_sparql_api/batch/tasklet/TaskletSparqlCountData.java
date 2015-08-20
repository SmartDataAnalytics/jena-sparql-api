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

import com.hp.hpl.jena.query.Query;

public class TaskletSparqlCountData
    extends StepExecutionListenerSupport implements Tasklet, InitializingBean
{
    // We should abstract where to place the value with some kind of 'sink'
    // The sink could then be backed by the spring expression language, such that we could do
    // sink = new SpelSink("jobContext['TaskletSparqlCountData.count']);
    // sink.setValue(count);
    
    public static final String KEY = TaskletSparqlCountData.class.getSimpleName() + ".count";

    
    private Query query;
    private QueryExecutionFactory qef;

    public TaskletSparqlCountData(Query query, QueryExecutionFactory qef) {
        this.query = query;
        this.qef = qef;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        long count = QueryExecutionUtils.countQuery(query, qef);

        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(KEY, count);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(query);
        Assert.notNull(qef);
    }
}