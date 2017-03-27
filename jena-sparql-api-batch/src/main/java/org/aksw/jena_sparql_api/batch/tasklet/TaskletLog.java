package org.aksw.jena_sparql_api.batch.tasklet;

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

    protected String text;

    public TaskletLog() {
    }

    public TaskletLog(String message) {
        this.text = message;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(text);
        return RepeatStatus.FINISHED;
    }
}
