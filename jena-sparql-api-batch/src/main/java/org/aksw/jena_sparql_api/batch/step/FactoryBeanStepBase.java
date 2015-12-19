package org.aksw.jena_sparql_api.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.task.TaskExecutor;

public abstract class FactoryBeanStepBase
    extends AbstractFactoryBean<Step>
{
    protected StepBuilderFactory stepBuilders;
    protected String name;

    protected TaskExecutor taskExecutor;
    protected Integer throttle;


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

    public AbstractFactoryBean<Step> setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return Step.class;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public Integer getThrottle() {
        return throttle;
    }

    public void setThrottle(Integer throttle) {
        this.throttle = throttle;
    }

    protected AbstractTaskletStepBuilder<?> applyDefaults(AbstractTaskletStepBuilder<?> base) {
        if(taskExecutor != null) {
            base = base.taskExecutor(taskExecutor);
        }

        if(throttle != null) {
            base = base.throttleLimit(throttle);
        }

        return base;
    }

    @Override
    protected Step createInstance() throws Exception {
        StepBuilder stepBuilder = stepBuilders.get(name);

        Step result = configureStep(stepBuilder);

        return result;
    }

    protected abstract Step configureStep(StepBuilder stepBuilder);

}
