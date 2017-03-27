package org.aksw.jena_sparql_api.batch.tasklet;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class TaskletSparqlUpdate
    extends StepExecutionListenerSupport implements Tasklet, InitializingBean
{
    //public static final String KEY = TaskletSparqlUpdate.class.getSimpleName() + ".count";

    private UpdateRequest updateRequest;
    private UpdateExecutionFactory uef;

    public TaskletSparqlUpdate(UpdateExecutionFactory uef, UpdateRequest updateRequest) {
        this.uef = uef;
        this.updateRequest = updateRequest;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution,
            ChunkContext chunkContext) throws Exception {

        UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
        updateProcessor.execute();

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(uef);
        Assert.notNull(updateRequest);
    }
}
