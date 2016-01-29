package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.atlas.web.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;


public class TaskletSparqlUpdate
    implements Tasklet
{
    private static final Logger logger = LoggerFactory.getLogger(TaskletSparqlUpdate.class);

    private UpdateExecutionFactory uef;
    private UpdateRequest updateRequest;

    public TaskletSparqlUpdate(UpdateExecutionFactory uef, UpdateRequest updateRequest) {
        this.uef = uef;
        this.updateRequest = updateRequest;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
        try {
            updateProcessor.execute();
        } catch(Exception e) {
            if(e instanceof HttpException) {
                HttpException x = (HttpException)e;
                logger.debug(x.getResponse());
            }
            throw e;
        }

        return RepeatStatus.FINISHED;
    }
}
