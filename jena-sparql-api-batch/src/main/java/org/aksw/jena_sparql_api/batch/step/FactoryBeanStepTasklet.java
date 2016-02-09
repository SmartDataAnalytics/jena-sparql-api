package org.aksw.jena_sparql_api.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;

public class FactoryBeanStepTasklet
    extends FactoryBeanStepBase
{
    protected Tasklet tasklet;

    public FactoryBeanStepTasklet() {
        super();
    }

    public Tasklet getTasklet() {
        return tasklet;
    }

    public void setTasklet(Tasklet tasklet) {
        this.tasklet = tasklet;
    }

    @Override
    protected Step configureStep(StepBuilder stepBuilder) {
        //Tasklet tasklet = new TaskletSparqlCountData(query, target, key);

        Step result = stepBuilder.tasklet(tasklet).build();
                //.build();
        return result;
    }


}
