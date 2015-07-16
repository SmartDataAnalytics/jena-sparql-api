package org.aksw.jena_sparql_api.batch;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;

import com.hp.hpl.jena.query.Query;

public class TaskletSparqlCountData
    extends StepExecutionListenerSupport implements Tasklet, InitializingBean
{
    public static final String KEY = TaskletSparqlCountData.class.getSimpleName() + ".count";

    private Query query;
    private QueryExecutionFactory sparqlService;

    public TaskletSparqlCountData(Query query, QueryExecutionFactory sparqlService) {
        this.query = query;
        this.sparqlService = sparqlService;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        long count = QueryExecutionUtils.countQuery(query, sparqlService);

        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(KEY, count);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}