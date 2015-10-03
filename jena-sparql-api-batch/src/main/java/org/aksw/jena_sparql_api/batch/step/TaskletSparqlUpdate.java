package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;


public class TaskletSparqlUpdate
    implements Tasklet
{
    private UpdateExecutionFactory uef;
    private UpdateRequest updateRequest;

    public TaskletSparqlUpdate(UpdateExecutionFactory uef, UpdateRequest updateRequest) {
        this.uef = uef;
        this.updateRequest = updateRequest;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
        updateProcessor.execute();

        return RepeatStatus.FINISHED;
    }
}
