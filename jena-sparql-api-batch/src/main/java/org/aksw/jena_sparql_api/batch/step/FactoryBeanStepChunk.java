package org.aksw.jena_sparql_api.batch.step;


import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;


public class FactoryBeanStepChunk
    //extends AbstractFactoryBean<Step>
    extends FactoryBeanStepBase
{
    protected ItemReader itemReader;
    protected ItemProcessor itemProcessor;
    protected ItemWriter itemWriter;
    protected Integer chunkSize;

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public ItemReader getItemReader() {
        return itemReader;
    }

    public void setItemReader(ItemReader itemReader) {
        this.itemReader = itemReader;
    }

    public ItemProcessor getItemProcessor() {
        return itemProcessor;
    }

    public void setItemProcessor(ItemProcessor itemProcessor) {
        this.itemProcessor = itemProcessor;
    }

    public ItemWriter getItemWriter() {
        return itemWriter;
    }

    public void setItemWriter(ItemWriter itemWriter) {
        this.itemWriter = itemWriter;
    }

    @Override
    protected Step configureStep(StepBuilder stepBuilder) {

        int c = chunkSize == null ? 1000 : chunkSize;

        Step result = stepBuilder
                .chunk(c)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                //.throttleLimit(throttle)
                .build()
                ;



//
//        if(throttle != null) {
//            Step slaveStep = result;
//
//            Partitioner partitioner = new PartitionerSparqlSlice(source, query);
//            result = stepBuilder
//                .partitioner(slaveStep)
//                .partitioner(name, partitioner)
//                .taskExecutor(taskExecutor)
//                .gridSize(throttle)
//                .build()
//                ;
//
//
//            //.partitioner(name + "-partitioner", partitioner).
//        }

        return result;
    }
}
