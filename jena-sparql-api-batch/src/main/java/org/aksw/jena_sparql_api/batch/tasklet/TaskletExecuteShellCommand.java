package org.aksw.jena_sparql_api.batch.tasklet;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class TaskletExecuteShellCommand
    implements Tasklet
{    
    private static final Logger logger = LoggerFactory.getLogger(TaskletExecuteShellCommand.class);
    protected String command;
    
    public TaskletExecuteShellCommand(String command) {
        super();
        this.command = command;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
            throws Exception { 
            
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        
        // TODO Store process output in the step context
        StringBuilder out = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine ()) != null) {
                out.append(out);
                logger.debug(line);
            }
        }

        chunkContext.getStepContext().setAttribute("processOutput", out.toString());
        
        int exitValue = process.exitValue();
        if(exitValue != 0) {
            throw new RuntimeException();
        }
        
        return RepeatStatus.FINISHED;
    }
}
