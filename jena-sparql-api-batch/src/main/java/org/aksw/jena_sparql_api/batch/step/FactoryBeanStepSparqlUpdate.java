package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;

import com.hp.hpl.jena.update.UpdateRequest;

public class FactoryBeanStepSparqlUpdate
    extends FactoryBeanStepBase
{
    protected UpdateExecutionFactory target;
    protected UpdateRequest update;

    public UpdateExecutionFactory getTarget() {
        return target;
    }

    public void setTarget(UpdateExecutionFactory target) {
        this.target = target;
    }

    public UpdateRequest getUpdate() {
        return update;
    }

    public void setUpdate(UpdateRequest update) {
        this.update = update;
    }

    @Override
    protected Step createInstance(StepBuilder stepBuilder) {
        Tasklet tasklet = new TaskletSparqlUpdate(target, update);

        Step result = stepBuilder
                .tasklet(tasklet)
                .build();
        return result;
    }


}
