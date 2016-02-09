package org.aksw.jena_sparql_api.batch.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.core.task.TaskExecutor;

public class FactoryBeanStepPartitioner
    extends FactoryBeanStepBase
{
    protected Step slaveStep;
    protected Partitioner partitioner;
    protected Integer throttle;
    protected TaskExecutor taskExecutor;

    public FactoryBeanStepPartitioner() {
        super();
    }

    public Step getSlaveStep() {
        return slaveStep;
    }

    public void setSlaveStep(Step slaveStep) {
        this.slaveStep = slaveStep;
    }

    public Partitioner getPartitioner() {
        return partitioner;
    }

    public void setPartitioner(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    public Integer getThrottle() {
        return throttle;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setThrottle(Integer throttle) {
        this.throttle = throttle;
    }



    @Override
    protected Step configureStep(StepBuilder stepBuilder) {
        //Partitioner partitioner = new PartitionerSparqlSlice(source, query);
        Step result = stepBuilder
            .partitioner(slaveStep)
            .partitioner(name, partitioner)
            .taskExecutor(taskExecutor)
            .gridSize(throttle)
            .build()
            ;

        return result;
    }

}
