package org.aksw.jena_sparql_api.batch;

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