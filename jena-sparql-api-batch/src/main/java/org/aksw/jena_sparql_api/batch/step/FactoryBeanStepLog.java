package org.aksw.jena_sparql_api.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;

public class FactoryBeanStepLog
    extends FactoryBeanStepBase
{
    protected String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected Step createInstance(StepBuilder stepBuilder) {
        Tasklet tasklet = new TaskletLog(message);
        Step result = stepBuilder
                .tasklet(tasklet)
                .build();
        return result;
    }


}
