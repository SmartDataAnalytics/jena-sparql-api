package org.aksw.jena_sparql_api.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;


public class TaskletLog
    implements Tasklet
{
    private static final Logger logger = LoggerFactory.getLogger(TaskletLog.class);

    protected String message;

    public TaskletLog(String message) {
        this.message = message;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(message);
        return RepeatStatus.FINISHED;
    }
}
