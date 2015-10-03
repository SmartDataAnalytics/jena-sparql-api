package org.aksw.jena_sparql_api.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public abstract class FactoryBeanStepBase
    extends AbstractFactoryBean<Step>
{
    protected StepBuilderFactory stepBuilders;
    protected String name;

    public FactoryBeanStepBase() {
        setSingleton(false);
    }

    public StepBuilderFactory getStepBuilders() {
        return stepBuilders;
    }

    @Autowired
    public void setStepBuilders(StepBuilderFactory stepBuilders) {
        this.stepBuilders = stepBuilders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<?> getObjectType() {
        return Step.class;
    }

    @Override
    protected Step createInstance() throws Exception {
        StepBuilder stepBuilder = stepBuilders.get(name);
        Step result = createInstance(stepBuilder);
        return result;
    }

    protected abstract Step createInstance(StepBuilder stepBuilder);

}
